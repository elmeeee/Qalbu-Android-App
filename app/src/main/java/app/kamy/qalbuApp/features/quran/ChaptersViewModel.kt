@file:Suppress("SpellCheckingInspection")

package app.kamy.qalbuApp.features.quran

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.core.error.toAppError
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.QuranJuz
import app.kamy.qalbuApp.domain.model.ReadingSession
import app.kamy.qalbuApp.domain.model.SearchNavResult
import app.kamy.qalbuApp.domain.model.SearchVerseResult
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.qalbuApp.infrastructure.preferences.MushafReadingStore
import app.kamy.qalbuApp.infrastructure.network.NetworkMonitor
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReadingSessionRepository
import app.kamy.qalbuApp.infrastructure.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuranBrowseMode { SURAH, JUZ, MUSHAF }

data class MushafBrowseState(
    val lastPage: Int = 1,
    val pagesRead: Int = 0,
    val progressFraction: Float = 0f,
    val isCloudSynced: Boolean = false
)

data class ChaptersUiState(
    val isLoading: Boolean = false,
    val browseMode: QuranBrowseMode = QuranBrowseMode.SURAH,
    val chapters: List<QuranChapter> = emptyList(),
    val juzs: List<QuranJuz> = emptyList(),
    val juzsLoading: Boolean = false,
    val juzsError: AppError? = null,
    val continueReading: ReadingSession? = null,
    val error: AppError? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val localSearchChapters: List<QuranChapter> = emptyList(),
    val remoteNavigation: List<SearchNavResult> = emptyList(),
    val remoteVerses: List<SearchVerseResult> = emptyList(),
    val verseRef: VerseReference? = null,
    val mushafPageRef: Int? = null,
    val searchLoading: Boolean = false,
    val searchError: AppError? = null,
    val isOfflineData: Boolean = false,
    val mushafBrowse: MushafBrowseState = MushafBrowseState(),
    val openingMushafJuz: Int? = null,
    val mushafOpenFailed: Boolean = false
)

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository,
    private val searchRepository: SearchRepository,
    private val translationStore: TranslationPreferencesStore,
    private val readingSessions: ReadingSessionRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(ChaptersUiState())
    val state: StateFlow<ChaptersUiState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var lastCloudSyncKey: String? = null

    init {
        loadAll()
        refreshMushafBrowse()
        viewModelScope.launch {
            userSession.isSignedIn.collect { signedIn ->
                    if (signedIn) {
                        syncCloudReadingToMushaf()
                    } else {
                        lastCloudSyncKey = null
                        refreshMushafBrowse(isCloudSynced = false)
                    }
                    refresh(force = false)
                }
        }
    }

    private fun refreshMushafBrowse(isCloudSynced: Boolean = _state.value.mushafBrowse.isCloudSynced) {
        _state.update {
            it.copy(
                mushafBrowse = MushafBrowseState(
                    lastPage = MushafReadingStore.lastPage(appContext),
                    pagesRead = MushafReadingStore.pagesReadCount(appContext),
                    progressFraction = MushafReadingStore.progressFraction(appContext),
                    isCloudSynced = isCloudSynced
                )
            )
        }
    }

    private suspend fun syncCloudReadingToMushaf() {
        val session = runCatching { readingSessions.fetchMostRecent() }.getOrNull() ?: return
        val syncKey = "${session.chapterNumber}:${session.verseNumber}"
        if (syncKey == lastCloudSyncKey) return
        val page = contentRepository.mushafPageForVerse(session.chapterNumber, session.verseNumber) ?: return
        lastCloudSyncKey = syncKey
        MushafReadingStore.saveLastPage(appContext, page, markRead = false)
        refreshMushafBrowse(isCloudSynced = true)
    }

    fun onMushafVisible() {
        refreshMushafBrowse()
    }

    fun loadAll(force: Boolean = false) {
        viewModelScope.launch { refresh(force) }
    }

    suspend fun refresh(force: Boolean = true) {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val chapters = contentRepository.getChapters(force)
            val continueReading = if (userSession.isSignedIn.value) {
                runCatching { readingSessions.fetchMostRecent() }.getOrNull()
            } else null
            if (continueReading != null) {
                syncCloudReadingToMushaf()
            } else {
                refreshMushafBrowse(isCloudSynced = false)
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    chapters = chapters,
                    continueReading = continueReading,
                    error = null,
                    isOfflineData = !NetworkMonitor.isOnline(appContext)
                )
            }
            recomputeLocalSearch()
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
                    mushafPageRef = null,
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
                    verseRef = null,
                    mushafPageRef = null
                )
            }
            return
        }
        _state.update {
            it.copy(
                localSearchChapters = s.chapters.searchChapters(query),
                verseRef = parseVerseReference(query),
                mushafPageRef = parseMushafPageQuery(query).takeIf { parseVerseReference(query) == null }
            )
        }
    }

    private fun scheduleRemoteSearch() {
        val s = _state.value
        searchJob?.cancel()
        if (!s.isSearchActive) return
        val query = s.searchQuery.normalizedSearchQuery()
        if (query.length < 2 || s.verseRef != null || s.mushafPageRef != null) {
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
        if (!userSession.isSignedIn.value) {
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

    fun setBrowseMode(mode: QuranBrowseMode) {
        _state.update { it.copy(browseMode = mode) }
        when (mode) {
            QuranBrowseMode.JUZ -> if (_state.value.juzs.isEmpty() && !_state.value.juzsLoading && _state.value.juzsError == null) {
                viewModelScope.launch { loadJuzs() }
            }
            QuranBrowseMode.MUSHAF -> {
                refreshMushafBrowse()
                if (_state.value.juzs.isEmpty() && !_state.value.juzsLoading) {
                    viewModelScope.launch { loadJuzs() }
                }
            }
            QuranBrowseMode.SURAH -> Unit
        }
    }

    suspend fun resolveMushafPageForJuz(juzNumber: Int): Int? =
        contentRepository.firstMushafPageForJuz(juzNumber)
            ?: resolveJuzStart(juzNumber)?.let { (chapter, ayah) ->
                contentRepository.mushafPageForVerse(chapter, ayah)
            }

    fun openMushafAtJuz(juzNumber: Int, onOpen: (Int) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(openingMushafJuz = juzNumber, mushafOpenFailed = false) }
            val page = runCatching { resolveMushafPageForJuz(juzNumber) }.getOrNull()
            _state.update { it.copy(openingMushafJuz = null) }
            if (page != null) {
                onOpen(page)
                clearSearch()
            } else {
                _state.update { it.copy(mushafOpenFailed = true) }
            }
        }
    }

    fun clearMushafOpenFailed() {
        _state.update { it.copy(mushafOpenFailed = false) }
    }

    fun parseMushafPageQuery(query: String): Int? {
        val trimmed = query.trim()
        trimmed.toIntOrNull()?.let { return it.coerceIn(1, MushafReadingStore.totalPages) }
        Regex("""(?:page|halaman|p)\s*(\d+)""", RegexOption.IGNORE_CASE)
            .find(trimmed)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?.let { return it.coerceIn(1, MushafReadingStore.totalPages) }
        return null
    }

    fun reloadJuzs() {
        viewModelScope.launch { loadJuzs(force = true) }
    }

    private suspend fun loadJuzs(force: Boolean = false) {
        _state.update { it.copy(juzsLoading = true, juzsError = null) }
        val result = runCatching { contentRepository.getJuzs(force) }
        _state.update {
            it.copy(
                juzs = result.getOrDefault(emptyList()),
                juzsLoading = false,
                juzsError = result.exceptionOrNull()?.toAppError(),
                isOfflineData = result.isSuccess && !NetworkMonitor.isOnline(appContext)
            )
        }
    }

    suspend fun resolveJuzStart(juzNumber: Int): Pair<Int, Int>? {
        val cached = _state.value.juzs.find { it.juzNumber == juzNumber }
        cached?.startChapterAndAyah()?.let { return it }
        return contentRepository.getJuz(juzNumber)?.startChapterAndAyah()
    }

    fun openJuz(juzNumber: Int, onOpen: (juzNumber: Int, verseKey: String?) -> Unit) {
        viewModelScope.launch {
            val verseKey = resolveJuzStart(juzNumber)?.let { (chapter, ayah) -> "$chapter:$ayah" }
            onOpen(juzNumber, verseKey)
            clearSearch()
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
