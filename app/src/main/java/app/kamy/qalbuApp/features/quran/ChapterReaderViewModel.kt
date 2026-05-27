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
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReadingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChapterReaderUiState(
    val chapterNumber: Int = 1,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val verses: List<RandomAyahPayload> = emptyList(),
    val recitations: List<RecitationPayload> = emptyList(),
    val selectedRecitationId: Int = 6,
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
    val activeAyahKey: String? = null
)

/**
 * Mirrors iOS Features/Chapter/ViewModels/ChapterVersesViewModel.swift.
 *
 * Receives the chapter number via navigation arguments (SavedStateHandle).
 */
@HiltViewModel
class ChapterReaderViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val readingSessions: ReadingSessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChapterReaderUiState(
            chapterNumber = savedStateHandle.get<Int>("chapter") ?: 1
        )
    )
    val state: StateFlow<ChapterReaderUiState> = _state.asStateFlow()

    init {
        loadInitial()
        loadRecitations()
    }

    fun loadInitial() {
        _state.update { it.copy(isLoading = true, error = null, verses = emptyList(), currentPage = 0) }
        viewModelScope.launch {
            runCatching {
                contentRepository.getVersesByChapter(_state.value.chapterNumber, page = 1)
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
                contentRepository.getVersesByChapter(s.chapterNumber, page = s.currentPage + 1)
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
        _state.update { it.copy(selectedRecitationId = id) }
    }

    fun setFontScale(scale: Float) {
        _state.update { it.copy(fontScale = scale.coerceIn(0.85f, 1.35f)) }
    }

    fun toggleTranslation(enabled: Boolean) {
        _state.update { it.copy(showTranslation = enabled) }
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
