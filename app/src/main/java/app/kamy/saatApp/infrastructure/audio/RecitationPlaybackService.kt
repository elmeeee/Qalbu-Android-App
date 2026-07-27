package app.kamy.saatApp.infrastructure.audio

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.infrastructure.notifications.NotificationChannels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecitationPlaybackService : MediaSessionService() {

    @Inject lateinit var playbackEngine: PlaybackEngine

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureAll(this)
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(NotificationChannels.MEDIA_PLAYBACK)
                .setChannelName(R.string.recitation_notification_channel)
                .setNotificationId(NOTIFICATION_ID)
                .build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val player = runCatching { playbackEngine.player }.getOrNull()
        val isPlaying = player?.playWhenReady == true || player?.isPlaying == true

        // On Android 15+, starting restricted foreground services from BOOT_COMPLETED
        // or background system restart (intent == null when idle) causes a crash.
        if (intent == null && !isPlaying) {
            stopSelf()
            return START_NOT_STICKY
        }

        // startForegroundService() requires startForeground() within ~5s. Media3 updates this
        // notification once playback metadata is available; until then, show a placeholder.
        val fgStarted = runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildPlaceholderNotification(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, buildPlaceholderNotification())
            }
        }.isSuccess
        if (!fgStarted && !isPlaying) {
            stopSelf()
            return START_NOT_STICKY
        }
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }


    @OptIn(UnstableApi::class)
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        playbackEngine.mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = playbackEngine.player
        // Keep playing when the user swipes the app away from recents.
        if (player.playWhenReady && player.playbackState != Player.STATE_IDLE) {
            return
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun buildPlaceholderNotification() = NotificationCompat.Builder(
        this,
        NotificationChannels.MEDIA_PLAYBACK
    )
        .setSmallIcon(R.drawable.ic_stat_notification)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(getString(R.string.recitation_notification_channel))
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
        .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
        .build()

    companion object {
        private const val NOTIFICATION_ID = 8401
    }
}
