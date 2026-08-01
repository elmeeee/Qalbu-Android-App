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
import app.kamy.saatApp.AdhanAlarmActivity
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.infrastructure.notifications.NotificationChannels
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore

private const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
private const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
private const val EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE"
private const val EXTRA_PREV_VOLUME_STREAM_VALUE = "android.media.EXTRA_PREV_VOLUME_STREAM_VALUE"

/**
 * Foreground alarm playback for adhan. Uses a plain [Service] (not MediaSessionService) so
 * scheduled alarms can start playback reliably from a [BroadcastReceiver] in the background.
 */

class AdhanPlaybackService : Service() {

    override fun attachBaseContext(newBase: Context) {
        val language = AppLanguageStore.from(newBase).current()
        super.attachBaseContext(AppLocale.wrap(newBase, language))
    }

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
            intent != null && (intent.hasExtra(EXTRA_RAW_RES) || intent.hasExtra(EXTRA_SOUND_URI) || intent.getBooleanExtra(EXTRA_USE_SYSTEM_ALARM, false)) -> {
                linkedNotificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
                val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME)
                val rawRes = intent.getIntExtra(EXTRA_RAW_RES, 0)
                val soundUriStr = intent.getStringExtra(EXTRA_SOUND_URI)
                val useSystemAlarm = intent.getBooleanExtra(EXTRA_USE_SYSTEM_ALARM, false)
                NotificationChannels.ensureAll(this)
                val fgSuccess = runCatching {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        startForeground(
                            NOTIFICATION_ID,
                            buildForegroundNotification(title, body, prayerName),
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, buildForegroundNotification(title, body, prayerName))
                    }
                }.isSuccess
                if (!fgSuccess) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                acquireWakeLock()
                startPlayback(rawRes, soundUriStr, useSystemAlarm, title, body, prayerName)
                runCatching {
                    startActivity(
                        AdhanAlarmActivity.intent(
                            context = this,
                            title = title.ifBlank { getString(R.string.adhan_playback_title) },
                            body = body.ifBlank { getString(R.string.adhan_playback_body) },
                            prayerName = prayerName
                        )
                    )
                }
            }
            else -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releasePlayer()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun startPlayback(
        @RawRes rawRes: Int,
        soundUriStr: String?,
        useSystemAlarm: Boolean,
        title: String,
        body: String,
        prayerName: String? = null
    ) {
        val fallbackRawUri = Uri.parse("android.resource://$packageName/${R.raw.tahajud_alarm}")
        val primaryUri: Uri? = when {
            rawRes != 0 -> Uri.parse("android.resource://$packageName/$rawRes")
            !soundUriStr.isNullOrBlank() -> Uri.parse(soundUriStr)
            useSystemAlarm -> fallbackRawUri
            else -> fallbackRawUri
        }
        val mediaUri = primaryUri ?: fallbackRawUri
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

        val isTahajudAlarm = prayerName?.equals("tahajud", ignoreCase = true) == true ||
            rawRes == R.raw.tahajud_alarm
        if (isTahajudAlarm) {
            exo.repeatMode = Player.REPEAT_MODE_ONE
        }

        var triedFallback = false
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
                if (!triedFallback) {
                    triedFallback = true
                    runCatching {
                        exo.setMediaItem(MediaItem.fromUri(fallbackRawUri))
                        exo.prepare()
                        exo.play()
                    }.onFailure {
                        releaseAndStop()
                    }
                } else {
                    releaseAndStop()
                }
            }
        })

        exo.setMediaItem(MediaItem.fromUri(mediaUri))
        exo.volume = 1f
        runCatching { boostAlarmVolume() }
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
        runCatching { sendBroadcast(Intent(ACTION_ADHAN_STOPPED).setPackage(packageName)) }
        cancelLinkedNotifications()
        releasePlayer()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cancelLinkedNotifications() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        linkedNotificationId = -1
    }

    private fun buildForegroundNotification(title: String, body: String, prayerName: String? = null) =
        NotificationCompat.Builder(this, NotificationChannels.ADHAN_PLAYBACK)
            .setSmallIcon(R.drawable.ic_stat_notification)
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
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .apply {
                // Use AdhanAlarmActivity — a lightweight dedicated alarm screen.
                // Request code must differ from the contentIntent above (which uses 0)
                // to prevent Android from deduplicating the PendingIntents and stripping extras.
                val fullScreenPendingIntent = PendingIntent.getActivity(
                    this@AdhanPlaybackService,
                    NOTIFICATION_ID + 1_000, // unique request code — NEVER 0
                    AdhanAlarmActivity.intent(
                        context = this@AdhanPlaybackService,
                        title = title.ifBlank { getString(R.string.adhan_playback_title) },
                        body = body.ifBlank { getString(R.string.adhan_playback_body) },
                        prayerName = prayerName
                    ),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setFullScreenIntent(fullScreenPendingIntent, true)

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
        private const val ADHAN_VOLUME_FRACTION = 0.95f
        private const val EXTRA_RAW_RES = "raw_res"
        private const val EXTRA_SOUND_URI = "sound_uri"
        private const val EXTRA_USE_SYSTEM_ALARM = "use_system_alarm"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_BODY = "body"
        private const val EXTRA_PRAYER_NAME = "prayer_name"
        private const val EXTRA_NOTIFICATION_ID = AdhanStopReceiver.EXTRA_NOTIFICATION_ID
        const val ACTION_STOP = AdhanStopReceiver.ACTION_STOP
        const val ACTION_ADHAN_STOPPED = "app.kamy.saatApp.action.ADHAN_STOPPED"

        fun start(
            context: Context,
            @RawRes rawRes: Int = 0,
            soundUri: Uri? = null,
            useSystemAlarm: Boolean = false,
            title: String,
            body: String,
            notificationId: Int = -1,
            prayerName: String? = null
        ): Boolean {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                putExtra(EXTRA_RAW_RES, rawRes)
                if (soundUri != null) {
                    putExtra(EXTRA_SOUND_URI, soundUri.toString())
                }
                putExtra(EXTRA_USE_SYSTEM_ALARM, useSystemAlarm)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                if (!prayerName.isNullOrBlank()) {
                    putExtra(EXTRA_PRAYER_NAME, prayerName)
                }
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
