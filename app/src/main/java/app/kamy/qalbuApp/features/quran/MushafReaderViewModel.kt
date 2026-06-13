package app.kamy.qalbuApp.features.quran

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.core.error.toAppError
import app.kamy.qalbuApp.domain.model.MushafLine
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.domain.model.groupIntoMushafLines
import app.kamy.qalbuApp.infrastructure.preferences.MushafReadingStore
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReadingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MushafPageState(
    val lines: List<MushafLine> = emptyList(),
    val verses: List<RandomAyahPayload> = emptyList(),
    val isLoading: Boolean = false,
    val error: AppError? = null
)

data class MushafReaderUiState(
    val currentPage: Int = 1,
    val totalPages: Int = MushafReadingStore.totalPages,
    val showTranslation: Boolean = false,
    val pages: Map<Int, MushafPageState> = emptyMap(),
    val pageInfoLabel: String? = null,
    val isResolvingStartPage: Boolean = true,
    val showSwipeHint: Boolean = false
)

@HiltViewModel
class MushafReaderViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository,
    private val readingSessions: ReadingSessionRepository,
    private val translationStore: TranslationPreferencesStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routePage = savedStateHandle.get<Int>("page")
        ?.coerceIn(1, MushafReadingStore.totalPages)

    private val _state = MutableStateFlow(
        MushafReaderUiState(
            currentPage = routePage ?: MushafReadingStore.lastPage(appContext),
            showTranslation = translationStore.showTranslation.value,
            isResolvingStartPage = routePage == null,
            showSwipeHint = !MushafReadingStore.hasSeenSwipeHint(appContext)
        )
    )
    val state: StateFlow<MushafReaderUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val startPage = routePage ?: resolveCloudOrLocalPage()
            _state.update { it.copy(currentPage = startPage, isResolvingStartPage = false) }
            MushafReadingStore.saveLastPage(appContext, startPage, markRead = false)
            loadPage(startPage)
            refreshPageInfo(startPage)
        }
    }

    fun onPageChanged(page: Int) {
        val safe = page.coerceIn(1, MushafReadingStore.totalPages)
        if (_state.value.showSwipeHint) {
            dismissSwipeHint()
        }
        _state.update { it.copy(currentPage = safe) }
        MushafReadingStore.saveLastPage(appContext, safe)
        loadPage(safe)
        refreshPageInfo(safe)
    }

    fun dismissSwipeHint() {
        MushafReadingStore.markSwipeHintSeen(appContext)
        _state.update { it.copy(showSwipeHint = false) }
    }

    fun toggleTranslation() {
        val next = !_state.value.showTranslation
        translationStore.setShowTranslation(next)
        _state.update { it.copy(showTranslation = next) }
    }

    fun goToPage(page: Int) = onPageChanged(page)

    private suspend fun resolveCloudOrLocalPage(): Int {
        val local = MushafReadingStore.lastPage(appContext)
        val session = runCatching { readingSessions.fetchMostRecent() }.getOrNull() ?: return local
        val cloudPage = contentRepository.mushafPageForVerse(session.chapterNumber, session.verseNumber)
        return cloudPage ?: local
    }

    private fun loadPage(page: Int) {
        if (_state.value.pages[page]?.lines?.isNotEmpty() == true) return
        _state.update {
            val existing = it.pages[page] ?: MushafPageState()
            it.copy(pages = it.pages + (page to existing.copy(isLoading = true, error = null)))
        }
        viewModelScope.launch {
            try {
                val response = contentRepository.getVersesByMushafPage(mushafPage = page)
                val verses = response.verses
                val lines = verses.groupIntoMushafLines()
                _state.update {
                    it.copy(
                        pages = it.pages + (page to MushafPageState(
                            lines = lines,
                            verses = verses,
                            isLoading = false,
                            error = null
                        ))
                    )
                }
                logReadingPosition(verses)
                prefetchAdjacent(page)
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        pages = it.pages + (page to MushafPageState(
                            isLoading = false,
                            error = t.toAppError()
                        ))
                    )
                }
            }
        }
    }

    private fun logReadingPosition(verses: List<RandomAyahPayload>) {
        val verse = verses.firstOrNull() ?: return
        val chapter = verse.chapterNumber ?: return
        val ayah = verse.resolvedVerseNumber ?: return
        viewModelScope.launch {
            runCatching { readingSessions.logReadingSession(chapter, ayah) }
        }
    }

    private fun prefetchAdjacent(page: Int) {
        listOf(page - 1, page + 1).forEach { adjacent ->
            if (adjacent in 1..MushafReadingStore.totalPages) {
                loadPage(adjacent)
            }
        }
    }

    private fun refreshPageInfo(page: Int) {
        viewModelScope.launch {
            runCatching {
                contentRepository.getPagesLookup(pageNumber = page)
            }.onSuccess { lookup ->
                val info = lookup.pages?.get(page.toString())
                val label = info?.let { "${it.from} – ${it.to}" }
                _state.update { it.copy(pageInfoLabel = label) }
            }
        }
    }
}
