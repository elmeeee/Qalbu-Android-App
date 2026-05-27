package app.kamy.qalbuApp.infrastructure.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Shared ExoPlayer + [MediaSession] for recitation (lock screen, background, notification). */
@Singleton
class PlaybackEngine @Inject constructor(
    @ApplicationContext context: Context
) {
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            /* handleAudioFocus= */ true
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()

    @OptIn(UnstableApi::class)
    val mediaSession: MediaSession = MediaSession.Builder(context, player)
        .setId("QalbuRecitation")
        .build()
}
