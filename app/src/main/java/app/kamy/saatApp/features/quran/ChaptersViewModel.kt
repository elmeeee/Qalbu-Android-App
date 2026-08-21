@file:Suppress("SpellCheckingInspection")

package app.kamy.saatApp.features.quran

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.error.toAppError
import app.kamy.saatApp.domain.model.QuranChapter
import app.kamy.saatApp.domain.model.QuranJuz
import app.kamy.saatApp.domain.model.ReadingSession
import app.kamy.saatApp.domain.model.SearchVerseResult
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.saatApp.infrastructure.network.NetworkMonitor
import app.kamy.saatApp.infrastructure.repository.QuranRepository
import app.kamy.saatApp.infrastructure.repository.ReadingSessionRepository
import app.kamy.saatApp.infrastructure.repository.SearchRepository
import app.kamy.saatApp.infrastructure.preferences.LocalReadingProgressStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QuranBrowseMode { SURAH, JUZ }

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
    val remoteVerses: List<SearchVerseResult> = emptyList(),
    val verseRef: VerseReference? = null,
    val juzRef: Int? = null,
    val searchLoading: Boolean = false,
    val searchError: AppError? = null,
    val isOfflineData: Boolean = false,
    val readChapters: Set<Int> = emptySet(),
    val readJuzs: Set<Int> = emptySet(),
    val lastReadJuz: Int? = null,
    val lastReadVerseKey: String? = null,
    val hasBookmarks: Boolean = false
)

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val quranRepository: QuranRepository,
    private val searchRepository: SearchRepository,
    private val translationStore: TranslationPreferencesStore,
    private val appLanguageStore: AppLanguageStore,
    private val readingSessions: ReadingSessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChaptersUiState())
    val state: StateFlow<ChaptersUiState> = _state.asStateFlow()

    private val _reviewFlow = MutableSharedFlow<Unit>(replay = 0)
    val reviewFlow = _reviewFlow.asSharedFlow()

    private var searchJob: Job? = null

    init {
        loadAll()
        viewModelScope.launch {
            appLanguageStore.currentFlow.drop(1).collect {
                quranRepository.clearCache()
                refresh(force = true)
            }
        }
    }

    fun onScreenVisible() {
        // Immediately sync local reading progress (fast, no network) so continue-reading
        // card updates as soon as the screen is visible after the user returns from reading.
        syncLocalReadingProgress()
        viewModelScope.launch {
            refresh(force = false)
            if (app.kamy.saatApp.infrastructure.review.AppReviewManager.shouldRequestReview(appContext)) {
                _reviewFlow.emit(Unit)
            }
        }
    }

    fun loadAll(force: Boolean = false) {
        viewModelScope.launch { refresh(force) }
    }

    suspend fun refresh(force: Boolean = true) {
        if (_state.value.chapters.isEmpty()) {
            _state.update { it.copy(isLoading = true, error = null) }
        }
        try {
            val chapters = quranRepository.getChapters(force)
            val continueReading = runCatching { readingSessions.fetchMostRecent() }.getOrNull()
            val readChapters = app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore.readChapters(appContext)
            val readJuzs = app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore.readJuzs(appContext)
            val lastReadJuz = app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore.lastReadJuz(appContext)
            val lastReadVerseKey = app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore.lastReadVerseKey(appContext)
            val hasBookmarks = app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore.bookmarks(appContext).isNotEmpty() ||
                app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore.notes(appContext).isNotEmpty() ||
                app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore.hifzEntries(appContext).isNotEmpty()
            _state.update {
                it.copy(
                    isLoading = false,
                    chapters = chapters,
                    continueReading = continueReading,
                    error = null,
                    isOfflineData = true,
                    readChapters = readChapters,
                    readJuzs = readJuzs,
                    lastReadJuz = lastReadJuz,
                    lastReadVerseKey = lastReadVerseKey,
                    hasBookmarks = hasBookmarks
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
                    remoteVerses = emptyList(),
                    verseRef = null,
                    juzRef = null,
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
                    juzRef = null
                )
            }
            return
        }
        val verseRef = parseVerseReference(query)
        val juzRef = parseJuzReference(query).takeIf { verseRef == null }
        _state.update {
            it.copy(
                localSearchChapters = s.chapters.searchChapters(query).filter { chapter ->
                    verseRef == null || chapter.id != verseRef.chapter
                },
                verseRef = verseRef,
                juzRef = juzRef
            )
        }
    }

    private fun scheduleRemoteSearch() {
        val s = _state.value
        searchJob?.cancel()
        if (!s.isSearchActive) return
        val query = s.searchQuery.normalizedSearchQuery()
        if (query.length < 2 || s.verseRef != null || s.juzRef != null) {
            _state.update {
                it.copy(
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
                _state.update {
                    it.copy(
                        searchLoading = false,
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
        // Prefer the always-fresh local store (written directly on every page change)
        // over the network-backed continueReading session which may lag or require sign-in.
        val local = LocalReadingProgressStore.load(appContext)
        if (local != null && local.chapterNumber > 0 && local.verseNumber > 0) {
            val chapter = _state.value.chapters.firstOrNull { it.id == local.chapterNumber }
                ?: return null
            return chapter to local.verseNumber
        }
        val s = _state.value.continueReading ?: return null
        val chapter = _state.value.chapters.firstOrNull { it.id == s.chapterNumber } ?: return null
        return chapter to s.verseNumber
    }

    /** Sync LocalReadingProgressStore into the state so the UI reflects the latest position. */
    private fun syncLocalReadingProgress() {
        val local = LocalReadingProgressStore.load(appContext) ?: return
        if (local.chapterNumber <= 0 || local.verseNumber <= 0) return
        // Build a lightweight ReadingSession from local data so the card renders without network.
        val existing = _state.value.continueReading
        val localMs = local.updatedAtMillis
        val cloudMs = existing?.updatedAt?.toLongOrNull() ?: 0L
        if (existing == null || localMs >= cloudMs) {
            _state.update {
                it.copy(
                    continueReading = ReadingSession(
                        id = "local",
                        updatedAt = localMs.toString(),
                        chapterNumber = local.chapterNumber,
                        verseNumber = local.verseNumber
                    )
                )
            }
        }
    }

    fun chapterForNumber(number: Int): QuranChapter? =
        _state.value.chapters.firstOrNull { it.id == number }

    fun setBrowseMode(mode: QuranBrowseMode) {
        _state.update { it.copy(browseMode = mode) }
        if (mode == QuranBrowseMode.JUZ &&
            _state.value.juzs.isEmpty() &&
            !_state.value.juzsLoading &&
            _state.value.juzsError == null
        ) {
            viewModelScope.launch { loadJuzs() }
        }
    }

    fun reloadJuzs() {
        viewModelScope.launch { loadJuzs(force = true) }
    }

    private suspend fun loadJuzs(force: Boolean = false) {
        _state.update { it.copy(juzsLoading = true, juzsError = null) }
        val result = runCatching { quranRepository.getJuzs(force) }
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
        return quranRepository.getJuz(juzNumber)?.startChapterAndAyah()
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
