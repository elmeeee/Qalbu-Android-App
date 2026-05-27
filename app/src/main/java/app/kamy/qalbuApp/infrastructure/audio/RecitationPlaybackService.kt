package app.kamy.qalbuApp.infrastructure.audio

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
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
        setMediaNotificationProvider(DefaultMediaNotificationProvider(this))
    }

    @OptIn(UnstableApi::class)
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        playbackEngine.mediaSession
}
