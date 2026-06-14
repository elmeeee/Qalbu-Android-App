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
import app.kamy.qalbuApp.infrastructure.preferences.KhatamProgressStore
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    @param:ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository,
    private val readingSessions: ReadingSessionRepository,
    private val translationStore: TranslationPreferencesStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routePage = savedStateHandle.get<Int>("page")
        ?.coerceIn(1, MushafReadingStore.totalPages)

    private val pagesInFlight = mutableSetOf<Int>()
    private val loadMutex = Mutex()

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
            _state.update {
                it.copy(
                    currentPage = startPage,
                    isResolvingStartPage = false,
                    pageInfoLabel = pageInfoFromVerses(it.pages[startPage]?.verses.orEmpty())
                )
            }
            MushafReadingStore.saveLastPage(appContext, startPage, markRead = false)
            loadPage(startPage)
        }
    }

    fun onPageChanged(page: Int) {
        val safe = page.coerceIn(1, MushafReadingStore.totalPages)
        if (safe == _state.value.currentPage) {
            val cached = _state.value.pages[safe]
            if (cached?.lines?.isNotEmpty() == true || cached?.isLoading == true) return
        }
        if (_state.value.showSwipeHint) {
            dismissSwipeHint()
        }
        _state.update { it.copy(currentPage = safe) }
        MushafReadingStore.saveLastPage(appContext, safe)
        KhatamProgressStore.recordPageRead(appContext, safe)
        loadPage(safe)
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

    fun retryPage(page: Int) {
        val safe = page.coerceIn(1, MushafReadingStore.totalPages)
        viewModelScope.launch {
            loadMutex.withLock { pagesInFlight.remove(safe) }
            _state.update { state ->
                val existing = state.pages[safe]
                if (existing?.lines?.isNotEmpty() == true) return@update state
                state.copy(
                    pages = state.pages + (safe to MushafPageState(isLoading = true, error = null))
                )
            }
            loadPage(safe, forceRefresh = true)
        }
    }

    private suspend fun resolveCloudOrLocalPage(): Int {
        val local = MushafReadingStore.lastPage(appContext)
        val session = runCatching { readingSessions.fetchMostRecent() }.getOrNull() ?: return local
        val cloudPage = contentRepository.mushafPageForVerse(session.chapterNumber, session.verseNumber)
        return cloudPage ?: local
    }

    private fun loadPage(page: Int, forceRefresh: Boolean = false) {
        val cached = _state.value.pages[page]
        if (!forceRefresh && cached?.lines?.isNotEmpty() == true) {
            updatePageInfoLabel(page, cached.verses)
            prefetchAdjacent(page)
            return
        }
        viewModelScope.launch {
            val shouldFetch = loadMutex.withLock {
                if (page in pagesInFlight) return@withLock false
                if (!forceRefresh && _state.value.pages[page]?.lines?.isNotEmpty() == true) {
                    return@withLock false
                }
                pagesInFlight.add(page)
                true
            }
            if (!shouldFetch) return@launch

            _state.update {
                val existing = it.pages[page] ?: MushafPageState()
                it.copy(pages = it.pages + (page to existing.copy(isLoading = true, error = null)))
            }
            try {
                val response = contentRepository.getVersesByMushafPage(
                    mushafPage = page,
                    forceRefresh = forceRefresh
                )
                val verses = response.verses
                val lines = verses.groupIntoMushafLines(mushafPage = page)
                _state.update {
                    val updatedPages = trimPagesCache(
                        it.pages + (page to MushafPageState(
                            lines = lines,
                            verses = verses,
                            isLoading = false,
                            error = null
                        )),
                        currentPage = page
                    )
                    it.copy(
                        pages = updatedPages,
                        pageInfoLabel = if (it.currentPage == page) {
                            pageInfoFromVerses(verses) ?: it.pageInfoLabel
                        } else {
                            it.pageInfoLabel
                        }
                    )
                }
                if (_state.value.currentPage == page) {
                    logReadingPosition(verses)
                }
                prefetchAdjacent(page)
            } catch (t: Throwable) {
                _state.update { state ->
                    val existing = state.pages[page]
                    if (existing?.lines?.isNotEmpty() == true) return@update state
                    state.copy(
                        pages = state.pages + (page to MushafPageState(
                            isLoading = false,
                            error = t.toAppError()
                        ))
                    )
                }
            } finally {
                loadMutex.withLock { pagesInFlight.remove(page) }
            }
        }
    }

    private fun updatePageInfoLabel(page: Int, verses: List<RandomAyahPayload>) {
        if (_state.value.currentPage != page) return
        pageInfoFromVerses(verses)?.let { label ->
            _state.update { it.copy(pageInfoLabel = label) }
        }
    }

    private fun pageInfoFromVerses(verses: List<RandomAyahPayload>): String? {
        if (verses.isEmpty()) return null
        val first = verses.first().displayVerseReference ?: return null
        val last = verses.last().displayVerseReference ?: return null
        return if (first == last) first else "$first – $last"
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

    private fun trimPagesCache(
        pages: Map<Int, MushafPageState>,
        currentPage: Int,
        maxEntries: Int = 12
    ): Map<Int, MushafPageState> {
        if (pages.size <= maxEntries) return pages
        val keep = pages.keys.sortedBy { kotlin.math.abs(it - currentPage) }.take(maxEntries).toSet()
        return pages.filterKeys { it in keep }
    }
}
