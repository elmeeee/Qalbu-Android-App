package app.kamy.qalbuApp.infrastructure.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import app.kamy.qalbuApp.core.config.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class AudioQueueItem(
    val verseKey: String,
    val ayahNumber: Int,
    val url: String,
    val label: String
)

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val currentUrl: String? = null,
    val progress: Float = 0f,
    val durationMs: Long = 0L,
    val trackTitle: String = "",
    val trackSubtitle: String = "",
    val reciterName: String = "",
    val activeIndex: Int? = null,
    val queue: List<AudioQueueItem> = emptyList()
)

/**
 * Mirrors iOS Features/Discovery/ViewModels/AudioPlayerViewModel.swift.
 *
 * Singleton-scoped ExoPlayer wrapper that exposes a [StateFlow] for Compose UIs.
 * Survives configuration changes; tied to the application process lifetime.
 *
 * iOS uses AVPlayer with a 0.5s timeObserver; we emit progress every 250ms
 * while playing so the UI stays smooth.
 */
@Singleton
class AudioPlayerController @OptIn(UnstableApi::class) @Inject constructor(
    @ApplicationContext context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    advanceQueueOrStop()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val current = _state.value
                val newIndex = player.currentMediaItemIndex
                val item = current.queue.getOrNull(newIndex)
                if (item != null) {
                    _state.value = current.copy(
                        currentUrl = item.url,
                        trackSubtitle = item.label,
                        activeIndex = newIndex
                    )
                }
            }
        })

        // Progress emitter.
        scope.launch {
            while (true) {
                if (player.isPlaying) {
                    val dur = player.duration.coerceAtLeast(0L)
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val progress = if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
                    _state.value = _state.value.copy(progress = progress, durationMs = dur)
                }
                delay(250L)
            }
        }
    }

    /** Single-verse playback (Today screen + per-ayah taps). */
    fun playVerse(url: String, surahTitle: String, ayahLabel: String, reciterName: String) {
        val absolute = AppConfig.absoluteVerseMediaUrl(url)
        val queueItem = AudioQueueItem(
            verseKey = "$surahTitle-$ayahLabel",
            ayahNumber = 0,
            url = absolute,
            label = ayahLabel
        )
        _state.value = AudioPlaybackState(
            isPlaying = true,
            currentUrl = absolute,
            trackTitle = surahTitle,
            trackSubtitle = ayahLabel,
            reciterName = reciterName,
            queue = listOf(queueItem),
            activeIndex = 0
        )
        player.setMediaItem(MediaItem.fromUri(absolute))
        player.prepare()
        player.playWhenReady = true
    }

    /** Continuous queue playback (Quran intro tap → play entire surah). */
    fun playSequence(
        items: List<AudioQueueItem>,
        surahTitle: String,
        reciterName: String,
        startIndex: Int = 0
    ) {
        if (items.isEmpty()) return
        val resolved = items.map { it.copy(url = AppConfig.absoluteVerseMediaUrl(it.url)) }
        player.setMediaItems(resolved.map { MediaItem.fromUri(it.url) }, startIndex, 0L)
        player.prepare()
        player.playWhenReady = true
        val starting = resolved.getOrNull(startIndex)
        _state.value = AudioPlaybackState(
            isPlaying = true,
            currentUrl = starting?.url,
            trackTitle = surahTitle,
            trackSubtitle = starting?.label.orEmpty(),
            reciterName = reciterName,
            queue = resolved,
            activeIndex = startIndex
        )
    }

    fun toggle() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun pause() = player.pause()

    fun stop() {
        player.stop()
        player.clearMediaItems()
        _state.value = AudioPlaybackState()
    }

    fun seekTo(progress: Float) {
        val dur = player.duration.coerceAtLeast(0L)
        if (dur > 0L) {
            player.seekTo((dur * progress.coerceIn(0f, 1f)).toLong())
        }
    }

    fun isPlayingUrl(url: String?): Boolean {
        if (url == null) return false
        val absolute = AppConfig.absoluteVerseMediaUrl(url)
        return _state.value.currentUrl == absolute && _state.value.isPlaying
    }

    private fun advanceQueueOrStop() {
        val current = _state.value
        val next = (current.activeIndex ?: 0) + 1
        if (next < current.queue.size) {
            // ExoPlayer handles its own transition for queues; this is the fallback
            // for single-item playback that just ended.
            player.seekToNext()
            player.play()
        } else {
            stop()
        }
    }
}
