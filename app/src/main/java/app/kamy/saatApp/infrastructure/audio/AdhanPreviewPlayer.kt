package app.kamy.saatApp.infrastructure.audio

import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdhanPreviewPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        stop()
                    }
                }
            })
        }
    }
    private val _previewingVoiceId = MutableStateFlow<String?>(null)
    val previewingVoiceId: StateFlow<String?> = _previewingVoiceId.asStateFlow()

    fun togglePreview(voiceId: String, @RawRes rawRes: Int) {
        if (_previewingVoiceId.value == voiceId && player.isPlaying) {
            stop()
            return
        }
        stop()
        val uri = Uri.parse("android.resource://${context.packageName}/$rawRes")
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
        _previewingVoiceId.value = voiceId
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        _previewingVoiceId.value = null
    }
}
