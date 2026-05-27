package app.kamy.qalbuApp.features.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.ReadingSession
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReadingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val error: String? = null
)

/**
 * Mirrors iOS Features/Chapter/ViewModels/QuranChaptersViewModel.swift.
 */
@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val readingSessions: ReadingSessionRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(ChaptersUiState())
    val state: StateFlow<ChaptersUiState> = _state.asStateFlow()

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
            }
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.message ?: "Failed to load chapters") }
        }
    }

    /** Resolves the continue-reading session into a chapter + verse for navigation. */
    fun continueReadingTarget(): Pair<QuranChapter, Int>? {
        val s = _state.value.continueReading ?: return null
        val chapter = _state.value.chapters.firstOrNull { it.id == s.chapterNumber } ?: return null
        return chapter to s.verseNumber
    }
}
