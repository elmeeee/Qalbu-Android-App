package app.kamy.qalbuApp.features.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.core.error.toAppError
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.ReadingSession
import app.kamy.qalbuApp.domain.model.SearchNavResult
import app.kamy.qalbuApp.domain.model.SearchVerseResult
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReadingSessionRepository
import app.kamy.qalbuApp.infrastructure.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChaptersUiState(
    val isLoading: Boolean = false,
    val chapters: List<QuranChapter> = emptyList(),
    val continueReading: ReadingSession? = null,
    val error: AppError? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val localSearchChapters: List<QuranChapter> = emptyList(),
    val remoteNavigation: List<SearchNavResult> = emptyList(),
    val remoteVerses: List<SearchVerseResult> = emptyList(),
    val verseRef: VerseReference? = null,
    val searchLoading: Boolean = false,
    val searchError: AppError? = null
)

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val searchRepository: SearchRepository,
    private val translationStore: TranslationPreferencesStore,
    private val readingSessions: ReadingSessionRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(ChaptersUiState())
    val state: StateFlow<ChaptersUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAll()
    }

    fun loadAll(force: Boolean = false) {
        viewModelScope.launch { refresh(force) }
    }

    suspend fun refresh(force: Boolean = true) {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            coroutineScope {
                val chaptersDeferred = async { contentRepository.getChapters(force) }
                val continueDeferred = async {
                    if (userSession.isSignedIn.value) {
                        runCatching { readingSessions.fetchMostRecent() }.getOrNull()
                    } else null
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        chapters = chaptersDeferred.await(),
                        continueReading = continueDeferred.await()
                    )
                }
                recomputeLocalSearch()
            }
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.toAppError()) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        recomputeLocalSearch()
        scheduleRemoteSearch()
    }

    fun onSearchActiveChange(active: Boolean) {
        _state.update { it.copy(isSearchActive = active) }
        if (!active) {
            searchJob?.cancel()
            _state.update {
                it.copy(
                    searchQuery = "",
                    localSearchChapters = emptyList(),
                    remoteNavigation = emptyList(),
                    remoteVerses = emptyList(),
                    verseRef = null,
                    searchLoading = false,
                    searchError = null
                )
            }
        } else {
            recomputeLocalSearch()
            scheduleRemoteSearch()
        }
    }

    fun clearSearch() {
        onSearchActiveChange(false)
    }

    private fun recomputeLocalSearch() {
        val s = _state.value
        if (!s.isSearchActive) return
        val query = s.searchQuery.normalizedSearchQuery()
        if (query.isEmpty()) {
            _state.update {
                it.copy(
                    localSearchChapters = emptyList(),
                    verseRef = null
                )
            }
            return
        }
        _state.update {
            it.copy(
                localSearchChapters = s.chapters.searchChapters(query),
                verseRef = parseVerseReference(query)
            )
        }
    }

    private fun scheduleRemoteSearch() {
        val s = _state.value
        searchJob?.cancel()
        if (!s.isSearchActive) return
        val query = s.searchQuery.normalizedSearchQuery()
        if (query.length < 2 || s.verseRef != null) {
            _state.update {
                it.copy(
                    remoteNavigation = emptyList(),
                    remoteVerses = emptyList(),
                    searchLoading = false,
                    searchError = null
                )
            }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _state.update { it.copy(searchLoading = true, searchError = null) }
            try {
                val result = searchRepository.quickSearch(
                    query = query,
                    translationId = translationStore.currentTranslationId()
                )
                val localChapterIds = _state.value.localSearchChapters.map { it.id }.toSet()
                val filteredNav = result.navigation.filter { nav ->
                    when (nav.type) {
                        "surah" -> nav.chapterNumber !in localChapterIds
                        "page", "juz" -> true
                        else -> true
                    }
                }
                _state.update {
                    it.copy(
                        searchLoading = false,
                        remoteNavigation = filteredNav,
                        remoteVerses = result.verses,
                        searchError = null
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        searchLoading = false,
                        searchError = t.toAppError()
                    )
                }
            }
        }
    }

    fun continueReadingTarget(): Pair<QuranChapter, Int>? {
        val s = _state.value.continueReading ?: return null
        val chapter = _state.value.chapters.firstOrNull { it.id == s.chapterNumber } ?: return null
        return chapter to s.verseNumber
    }

    fun chapterForNumber(number: Int): QuranChapter? =
        _state.value.chapters.firstOrNull { it.id == number }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
