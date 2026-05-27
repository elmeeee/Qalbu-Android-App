package app.kamy.qalbuApp.features.quran

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.domain.model.HadithReference
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.infrastructure.audio.AudioQueueItem
import app.kamy.qalbuApp.infrastructure.audio.AudioPlayerController
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReadingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChapterReaderUiState(
    val chapterNumber: Int = 1,
    val chapterDisplayName: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val verses: List<RandomAyahPayload> = emptyList(),
    val recitations: List<RecitationPayload> = emptyList(),
    val selectedRecitationId: Int = TranslationPreferencesStore.DEFAULT_RECITATION_ID,
    val playbackMode: AyahPlaybackMode = AyahPlaybackMode.CONTINUOUS,
    val fontScale: Float = 1.0f,
    val showTranslation: Boolean = true,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val error: String? = null,
    val tafsirVisible: Boolean = false,
    val tafsirLoading: Boolean = false,
    val tafsir: TafsirPayload? = null,
    val hadithVisible: Boolean = false,
    val hadithLoading: Boolean = false,
    val hadiths: List<HadithReference> = emptyList(),
    val activeAyahKey: String? = null,

    // When audio is paused/playing, keep current verse key so the UI can reflect it if needed.
    val currentlyPlayingVerseKey: String? = null
)

enum class AyahPlaybackMode {
    SINGLE,
    CONTINUOUS
}

/**
 * Mirrors iOS Features/Chapter/ViewModels/ChapterVersesViewModel.swift.
 *
 * Receives the chapter number via navigation arguments (SavedStateHandle).
 */
@HiltViewModel
class ChapterReaderViewModel @Inject constructor(
    private val audioPlayer: AudioPlayerController,
    private val contentRepository: ContentRepository,
    private val readingSessions: ReadingSessionRepository,
    private val translationStore: TranslationPreferencesStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChapterReaderUiState(
            chapterNumber = savedStateHandle.get<Int>("chapter") ?: 1
        )
    )
    val state: StateFlow<ChapterReaderUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ReaderEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var lastStartedVerseKey: String? = null
    private var wasPlaying: Boolean = false

    init {
        _state.update {
            it.copy(
                showTranslation = translationStore.showTranslation.value,
                selectedRecitationId = translationStore.currentRecitationId()
            )
        }
        loadChapterMeta()
        loadInitial()
        loadRecitations()
        viewModelScope.launch {
            translationStore.translationId.drop(1).collect { loadInitial() }
        }
        viewModelScope.launch {
            translationStore.showTranslation.collect { enabled ->
                _state.update { it.copy(showTranslation = enabled) }
            }
        }

        // Sync ViewModel with player state so we can auto-advance on completion.
        viewModelScope.launch {
            audioPlayer.state.collect { audio ->
                val nextPlayingVerseKey =
                    if (audio.currentUrl != null) audio.trackSubtitle.ifBlank { null } else null

                _state.update { it.copy(currentlyPlayingVerseKey = nextPlayingVerseKey) }

                val ended = wasPlaying && !audio.isPlaying && audio.currentUrl == null
                if (ended) {
                    maybeAutoAdvanceAfterCompletion()
                }
                wasPlaying = audio.isPlaying
            }
        }
    }

    private fun maybeAutoAdvanceAfterCompletion() {
        val s = _state.value
        if (s.playbackMode != AyahPlaybackMode.CONTINUOUS) return
        val lastKey = lastStartedVerseKey ?: return

        val verses = s.verses
        val lastIndex = verses.indexOfFirst { it.verseKey == lastKey }
        if (lastIndex < 0) return

        val nextIndex = lastIndex + 1
        if (nextIndex in verses.indices) {
            val nextPage = verses[nextIndex]
            playAyahAtIndex(nextIndex, nextPage)
            _events.tryEmit(ReaderEvent.AnimateToPage(nextIndex))
        }
    }

    private fun loadChapterMeta() {
        viewModelScope.launch {
            runCatching {
                val chapters = contentRepository.getChapters()
                val name = chapters.find { it.id == _state.value.chapterNumber }?.displayComplexName
                _state.update { it.copy(chapterDisplayName = name) }
            }
        }
    }

    fun loadInitial() {
        _state.update { it.copy(isLoading = true, error = null, verses = emptyList(), currentPage = 0) }
        viewModelScope.launch {
            runCatching {
                contentRepository.getVersesByChapter(
                    chapterNumber = _state.value.chapterNumber,
                    page = 1,
                    audioRecitationId = _state.value.selectedRecitationId
                )
            }.onSuccess { resp ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        verses = resp.verses,
                        currentPage = resp.pagination?.currentPage ?: 1,
                        hasMore = resp.pagination?.hasNextPage ?: false
                    )
                }
            }.onFailure { t ->
                _state.update { it.copy(isLoading = false, error = t.message ?: "Failed to load verses") }
            }
        }
    }

    fun loadMoreIfNeeded(currentIndex: Int) {
        val s = _state.value
        if (s.isLoadingMore || !s.hasMore) return
        if (currentIndex < s.verses.size - 3) return
        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            runCatching {
                contentRepository.getVersesByChapter(
                    chapterNumber = s.chapterNumber,
                    page = s.currentPage + 1,
                    audioRecitationId = s.selectedRecitationId
                )
            }.onSuccess { resp ->
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        verses = it.verses + resp.verses,
                        currentPage = resp.pagination?.currentPage ?: (it.currentPage + 1),
                        hasMore = resp.pagination?.hasNextPage ?: false
                    )
                }
            }.onFailure {
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private fun loadRecitations() {
        viewModelScope.launch {
            runCatching { contentRepository.getRecitations() }
                .onSuccess { rs -> _state.update { it.copy(recitations = rs) } }
        }
    }

    fun selectRecitation(id: Int) {
        if (id <= 0 || id == _state.value.selectedRecitationId) return
        translationStore.setRecitation(id)
        _state.update { it.copy(selectedRecitationId = id) }
        loadInitial()
    }

    fun setFontScale(scale: Float) {
        _state.update { it.copy(fontScale = scale.coerceIn(0.85f, 1.35f)) }
    }

    fun setPlaybackMode(mode: AyahPlaybackMode) {
        _state.update { it.copy(playbackMode = mode) }
    }

    fun toggleTranslation(enabled: Boolean) {
        translationStore.setShowTranslation(enabled)
    }

    fun onPageChanged(index: Int) {
        val s = _state.value
        if (index !in s.verses.indices) return
        val page = s.verses[index]
        _state.update { it.copy(currentPage = index) }
        page.resolvedVerseNumber?.let { logScrollPosition(it) }
    }

    fun onTapAyah(index: Int) {
        val s = _state.value
        if (index !in s.verses.indices) return

        val page = s.verses[index]
        val verseKey = page.verseKey
        val isSamePlaying = s.currentlyPlayingVerseKey != null && verseKey != null && verseKey == s.currentlyPlayingVerseKey

        val url = page.audio?.url
        if (url.isNullOrBlank()) return

        if (isSamePlaying) {
            audioPlayer.toggle()
        } else {
            playAyahAtIndex(index, page)
        }
    }

    private fun playAyahAtIndex(index: Int, page: RandomAyahPayload) {
        val s = _state.value
        val surahTitle = s.chapterDisplayName ?: "Surah ${s.chapterNumber}"
        val reciterName = s.recitations
            .firstOrNull { it.id == s.selectedRecitationId }
            ?.displayName.orEmpty()

        val url = page.audio?.url ?: return
        lastStartedVerseKey = page.verseKey
        _state.update { it.copy(currentPage = index, currentlyPlayingVerseKey = page.verseKey) }
        audioPlayer.playVerse(
            url = url,
            surahTitle = surahTitle,
            ayahLabel = page.verseKey.orEmpty(),
            reciterName = reciterName,
            chapterNumber = s.chapterNumber,
            ayahNumber = page.resolvedVerseNumber
        )
    }

    fun openTafsir(ayahKey: String) {
        _state.update { it.copy(tafsirVisible = true, tafsirLoading = true, tafsir = null, activeAyahKey = ayahKey) }
        viewModelScope.launch {
            runCatching { contentRepository.getTafsirByAyah("169", ayahKey) }
                .onSuccess { t -> _state.update { it.copy(tafsir = t, tafsirLoading = false) } }
                .onFailure { _state.update { it.copy(tafsirLoading = false) } }
        }
    }

    fun dismissTafsir() {
        _state.update { it.copy(tafsirVisible = false) }
    }

    fun openHadith(ayahKey: String) {
        _state.update { it.copy(hadithVisible = true, hadithLoading = true, hadiths = emptyList(), activeAyahKey = ayahKey) }
        viewModelScope.launch {
            runCatching { contentRepository.getHadithsByAyah(ayahKey, limit = 5) }
                .onSuccess { resp -> _state.update { it.copy(hadiths = resp.hadiths.orEmpty(), hadithLoading = false) } }
                .onFailure { _state.update { it.copy(hadithLoading = false) } }
        }
    }

    fun dismissHadith() {
        _state.update { it.copy(hadithVisible = false) }
    }

    /** Builds an [AudioQueueItem] list for playing the entire surah. */
    fun audioQueueItems(): List<AudioQueueItem> = _state.value.verses.mapNotNull { v ->
        val url = v.audio?.url ?: return@mapNotNull null
        AudioQueueItem(
            verseKey = v.verseKey.orEmpty(),
            ayahNumber = v.resolvedVerseNumber ?: 0,
            url = url,
            label = v.verseKey.orEmpty()
        )
    }

    /** Mirrors iOS ReadingSessionTracker — debounced "current scroll position" logger. */
    fun logScrollPosition(verseNumber: Int) {
        viewModelScope.launch {
            runCatching { readingSessions.logReadingSession(_state.value.chapterNumber, verseNumber) }
        }
    }
}

sealed interface ReaderEvent {
    data class AnimateToPage(val index: Int) : ReaderEvent
}
