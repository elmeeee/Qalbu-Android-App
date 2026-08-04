package app.kamy.saatApp.infrastructure.audio

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.infrastructure.airplane.AirplaneModeReceiver
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
    val queue: List<AudioQueueItem> = emptyList(),
    val chapterNumber: Int? = null,
    val ayahNumber: Int? = null,
    val currentPositionMs: Long = 0L,
    val lastError: String? = null
) {
    val hasReaderNavigation: Boolean = chapterNumber != null && chapterNumber > 0
}

fun parseVerseKey(verseKey: String?): Pair<Int, Int>? {
    if (verseKey.isNullOrBlank()) return null
    val parts = verseKey.split(":")
    if (parts.size != 2) return null
    val chapter = parts[0].trim().toIntOrNull() ?: return null
    val ayah = parts[1].trim().toIntOrNull() ?: return null
    return chapter to ayah
}

@Singleton
class AudioPlayerController @OptIn(UnstableApi::class) @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playbackEngine: PlaybackEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val player get() = playbackEngine.player

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    /**
     * Called when the current single-track playback ends and the queue has no more items.
     * ViewModel subscribes to this to implement continuous playback / auto-advance logic.
     */
    var onTrackEnded: (() -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
                if (isPlaying) {
                    ensurePlaybackService()
                }
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
                    val parsed = parseVerseKey(item.verseKey)
                    _state.value = current.copy(
                        currentUrl = item.url,
                        trackSubtitle = item.label,
                        activeIndex = newIndex,
                        chapterNumber = parsed?.first ?: current.chapterNumber,
                        ayahNumber = parsed?.second ?: item.ayahNumber.takeIf { it > 0 }
                    )
                }
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("AudioPlayerController", "Playback error", error)
                val errorMsg = error.localizedMessage ?: "Gagal memutar audio"
                _state.value = _state.value.copy(isPlaying = false, lastError = errorMsg)
                runCatching { player.stop() }
                runCatching { player.clearMediaItems() }
                runCatching {
                    context.stopService(Intent(context, RecitationPlaybackService::class.java))
                }
            }
        })

        scope.launch {
            while (true) {
                if (player.isPlaying) {
                    val dur = player.duration.coerceAtLeast(0L)
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val progress = if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
                    _state.value = _state.value.copy(progress = progress, durationMs = dur, currentPositionMs = pos)
                }
                delay(100L)
            }
        }
    }

    fun playVerse(
        url: String,
        surahTitle: String,
        ayahLabel: String,
        reciterName: String,
        chapterNumber: Int? = null,
        ayahNumber: Int? = null
    ) {
        if (AirplaneModeReceiver.isAirplaneModeOn(context)) return
        val absolute = AppConfig.absoluteVerseMediaUrl(url)
        val parsed = parseVerseKey(ayahLabel)
        val chapter = chapterNumber ?: parsed?.first
        val ayah = ayahNumber ?: parsed?.second
        val queueItem = AudioQueueItem(
            verseKey = ayahLabel,
            ayahNumber = ayah ?: 0,
            url = absolute,
            label = ayahLabel
        )
        val mediaItem = buildMediaItem(absolute, surahTitle, ayahLabel, reciterName)
        _state.value = AudioPlaybackState(
            isPlaying = true,
            currentUrl = absolute,
            trackTitle = surahTitle,
            trackSubtitle = ayahLabel,
            reciterName = reciterName,
            queue = listOf(queueItem),
            activeIndex = 0,
            chapterNumber = chapter,
            ayahNumber = ayah,
            lastError = null
        )
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        ensurePlaybackService()
    }

    fun playSequence(
        items: List<AudioQueueItem>,
        surahTitle: String,
        reciterName: String,
        startIndex: Int = 0,
        chapterNumber: Int? = null
    ) {
        if (items.isEmpty()) return
        if (AirplaneModeReceiver.isAirplaneModeOn(context)) return
        val resolved = items.map { it.copy(url = AppConfig.absoluteVerseMediaUrl(it.url)) }
        val mediaItems = resolved.map { item ->
            buildMediaItem(item.url, surahTitle, item.label, reciterName)
        }
        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.playWhenReady = true
        val starting = resolved.getOrNull(startIndex)
        val parsed = parseVerseKey(starting?.verseKey)
        _state.value = AudioPlaybackState(
            isPlaying = true,
            currentUrl = starting?.url,
            trackTitle = surahTitle,
            trackSubtitle = starting?.label.orEmpty(),
            reciterName = reciterName,
            queue = resolved,
            activeIndex = startIndex,
            chapterNumber = chapterNumber ?: parsed?.first,
            ayahNumber = parsed?.second ?: starting?.ayahNumber?.takeIf { it > 0 },
            lastError = null
        )
        ensurePlaybackService()
    }

    fun toggle() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun pause() = player.pause()

    fun stop() {
        runCatching { player.stop() }
        runCatching { player.clearMediaItems() }
        _state.value = AudioPlaybackState(lastError = _state.value.lastError)
        runCatching {
            context.stopService(Intent(context, RecitationPlaybackService::class.java))
        }
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

    private fun buildMediaItem(
        url: String,
        surahTitle: String,
        ayahLabel: String,
        reciterName: String
    ): MediaItem = MediaItem.Builder()
        .setUri(url)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(surahTitle)
                .setArtist(reciterName)
                .setAlbumTitle(ayahLabel)
                .build()
        )
        .build()

    private fun ensurePlaybackService() {
        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, RecitationPlaybackService::class.java)
            )
        }
    }

    private fun advanceQueueOrStop() {
        val current = _state.value
        val next = (current.activeIndex ?: 0) + 1
        if (next < current.queue.size) {
            player.seekToNext()
            player.play()
        } else {
            // Notify ViewModel first so it can handle continuous-play advance.
            // If no listener is registered, stop immediately.
            val listener = onTrackEnded
            if (listener != null) {
                listener()
            } else {
                stop()
            }
        }
    }
}
