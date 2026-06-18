package app.kamy.saatApp.infrastructure.audio

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import kotlin.math.roundToInt
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.infrastructure.notifications.NotificationChannels

private const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
private const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
private const val EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE"
private const val EXTRA_PREV_VOLUME_STREAM_VALUE = "android.media.EXTRA_PREV_VOLUME_STREAM_VALUE"

/**
 * Foreground alarm playback for adhan. Uses a plain [Service] (not MediaSessionService) so
 * scheduled alarms can start playback reliably from a [BroadcastReceiver] in the background.
 */
class AdhanPlaybackService : Service() {

    private var player: ExoPlayer? = null
    private var playbackStarted = false
    private var volumeStopEnabled = false
    private var linkedNotificationId = -1
    private var volumeReceiver: BroadcastReceiver? = null
    private var savedAlarmVolume: Int? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val enableVolumeStopRunnable = Runnable { volumeStopEnabled = true }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent?.action == ACTION_STOP -> {
                linkedNotificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, linkedNotificationId)
                releaseAndStop()
                return START_NOT_STICKY
            }
            intent != null && intent.hasExtra(EXTRA_RAW_RES) -> {
                linkedNotificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
                val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
                val rawRes = intent.getIntExtra(EXTRA_RAW_RES, 0)
                NotificationChannels.ensureAll(this)
                startForeground(NOTIFICATION_ID, buildForegroundNotification(title, body))
                acquireWakeLock()
                startAdhan(rawRes, title, body)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releasePlayer()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun startAdhan(@RawRes rawRes: Int, title: String, body: String) {
        if (rawRes == 0) {
            stopSelf()
            return
        }
        releasePlayer()

        val exo = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_ALARM)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SONIFICATION)
                    .build(),
                // Alarms must not defer to media audio-focus while the device is locked.
                /* handleAudioFocus= */ false
            )
            .setHandleAudioBecomingNoisy(false)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        exo.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> releaseAndStop()
                    Player.STATE_IDLE -> if (playbackStarted) releaseAndStop()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    playbackStarted = true
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                releaseAndStop()
            }
        })

        val uri = Uri.parse("android.resource://$packageName/$rawRes")
        exo.setMediaItem(MediaItem.fromUri(uri))
        exo.volume = 1f
        boostAlarmVolume()
        exo.prepare()
        exo.play()

        player = exo
        scheduleVolumeStopListener()
    }

    private fun acquireWakeLock() {
        releaseWakeLock()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Saat:AdhanPlayback").apply {
            setReferenceCounted(false)
            acquire(10 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            runCatching { if (lock.isHeld) lock.release() }
        }
        wakeLock = null
    }

    private fun boostAlarmVolume() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        if (maxVolume <= 0) return
        val targetVolume = (maxVolume * ADHAN_VOLUME_FRACTION)
            .roundToInt()
            .coerceIn(1, maxVolume)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        savedAlarmVolume = currentVolume
        if (currentVolume < targetVolume) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                targetVolume,
                /* flags= */ 0
            )
        }
    }

    private fun restoreAlarmVolume() {
        val previousVolume = savedAlarmVolume ?: return
        savedAlarmVolume = null
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        if (maxVolume <= 0) return
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            previousVolume.coerceIn(0, maxVolume),
            /* flags= */ 0
        )
    }

    private fun scheduleVolumeStopListener() {
        unregisterVolumeStopListener()
        volumeStopEnabled = false
        mainHandler.removeCallbacks(enableVolumeStopRunnable)
        registerVolumeStopListener()
        mainHandler.postDelayed(enableVolumeStopRunnable, VOLUME_STOP_GRACE_MS)
    }

    private fun registerVolumeStopListener() {
        val filter = IntentFilter(VOLUME_CHANGED_ACTION)
        volumeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!playbackStarted || !volumeStopEnabled) return
                val streamType = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1)
                if (streamType != AudioManager.STREAM_ALARM && streamType != AudioManager.STREAM_MUSIC) {
                    return
                }
                val newVolume = intent.getIntExtra(EXTRA_VOLUME_STREAM_VALUE, -1)
                val oldVolume = intent.getIntExtra(EXTRA_PREV_VOLUME_STREAM_VALUE, -1)
                if (newVolume >= 0 && oldVolume >= 0 && newVolume != oldVolume) {
                    releaseAndStop()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(volumeReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(volumeReceiver, filter)
        }
    }

    private fun unregisterVolumeStopListener() {
        mainHandler.removeCallbacks(enableVolumeStopRunnable)
        volumeStopEnabled = false
        volumeReceiver?.let { receiver ->
            runCatching { unregisterReceiver(receiver) }
        }
        volumeReceiver = null
    }

    private fun releaseAndStop() {
        cancelLinkedNotifications()
        releasePlayer()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cancelLinkedNotifications() {
        if (linkedNotificationId >= 0) {
            NotificationManagerCompat.from(this).cancel(linkedNotificationId)
        }
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        linkedNotificationId = -1
    }

    private fun buildForegroundNotification(title: String, body: String) =
        NotificationCompat.Builder(this, NotificationChannels.ADHAN_PLAYBACK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title.ifBlank { getString(R.string.adhan_playback_title) })
            .setContentText(body.ifBlank { getString(R.string.adhan_playback_body) })
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .apply {
                val stopPending = PendingIntent.getBroadcast(
                    this@AdhanPlaybackService,
                    NOTIFICATION_ID,
                    AdhanStopReceiver.intent(
                        this@AdhanPlaybackService,
                        linkedNotificationId.takeIf { it >= 0 }
                    ),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setDeleteIntent(stopPending)
                addAction(
                    android.R.drawable.ic_media_pause,
                    getString(R.string.adhan_stop),
                    stopPending
                )
            }
            .build()

    private fun releasePlayer() {
        unregisterVolumeStopListener()
        restoreAlarmVolume()
        playbackStarted = false
        player?.release()
        player = null
    }

    companion object {
        private const val NOTIFICATION_ID = 12_001
        private const val VOLUME_STOP_GRACE_MS = 1_500L
        private const val ADHAN_VOLUME_FRACTION = 0.4f
        private const val EXTRA_RAW_RES = "raw_res"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_BODY = "body"
        private const val EXTRA_NOTIFICATION_ID = AdhanStopReceiver.EXTRA_NOTIFICATION_ID
        const val ACTION_STOP = AdhanStopReceiver.ACTION_STOP

        fun start(
            context: Context,
            @RawRes rawRes: Int,
            title: String,
            body: String,
            notificationId: Int = -1
        ): Boolean {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                putExtra(EXTRA_RAW_RES, rawRes)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                if (notificationId >= 0) {
                    putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                }
            }
            return runCatching {
                androidx.core.content.ContextCompat.startForegroundService(context, intent)
            }.isSuccess
        }

        fun stop(context: Context, notificationId: Int = -1) {
            context.startService(
                Intent(context, AdhanPlaybackService::class.java).apply {
                    action = ACTION_STOP
                    if (notificationId >= 0) {
                        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                    }
                }
            )
        }
    }
}
