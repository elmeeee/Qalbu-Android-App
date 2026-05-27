package app.kamy.qalbuApp.infrastructure.audio

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.infrastructure.notifications.NotificationChannels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Keeps recitation alive in background / lock screen with system media controls.
 */
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
                .build()
        )
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
}
