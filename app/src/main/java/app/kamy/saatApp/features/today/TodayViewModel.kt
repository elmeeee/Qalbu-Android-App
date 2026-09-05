package app.kamy.saatApp.features.today

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.error.AppErrorKind
import app.kamy.saatApp.core.error.toAppError
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.ReadingSession
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.saatApp.infrastructure.quran.DailyVerseLoader
import app.kamy.saatApp.infrastructure.repository.DailyQuoteItem
import app.kamy.saatApp.infrastructure.repository.DailyQuoteRepository
import app.kamy.saatApp.infrastructure.repository.KhgtCalendarRepository
import app.kamy.saatApp.infrastructure.repository.QuranRepository
import app.kamy.saatApp.infrastructure.repository.ReadingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val isLoading: Boolean = false,
    val verse: RandomAyahPayload? = null,
    val verseReferenceLabel: String? = null,
    val dailyQuote: DailyQuoteItem? = null,
    val error: AppError? = null,
    val isOfflineData: Boolean = false,
    val continueReading: ReadingSession? = null,
    val continueReadingChapterName: String? = null,
    val continueReadingTotalVerses: Int? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val quranRepository: QuranRepository,
    private val translationStore: TranslationPreferencesStore,
    private val dailyVerseLoader: DailyVerseLoader,
    private val readingSessions: ReadingSessionRepository,
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val khgtCalendarRepository: KhgtCalendarRepository,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state.asStateFlow()

    init {
        loadDailyAyah()
        loadContinueReading()

        viewModelScope.launch {
            val khgtInfo = runCatching { khgtCalendarRepository.todayInfo() }.getOrNull()
            appLanguageStore.currentFlow.collect { lang ->
                val updatedQuote = dailyQuoteRepository.getTodayQuote(
                    language = lang,
                    eventTitle = khgtInfo?.eventTitle,
                    hijriLabel = khgtInfo?.hijriLabel
                )
                _state.update { it.copy(dailyQuote = updatedQuote) }
            }
        }

        viewModelScope.launch {
            translationStore.translationId.drop(1).collect {
                loadDailyAyah(refreshTranslation = true)
            }
        }
    }

    fun loadContinueReading() {
        viewModelScope.launch {
            try {
                val session = readingSessions.fetchMostRecent()
                val chapters = quranRepository.getChapters(force = false)
                val targetChapter = session?.let { s -> chapters.firstOrNull { it.id == s.chapterNumber } }
                val chapterName = targetChapter?.displayComplexName
                val totalVerses = targetChapter?.versesCount
                _state.update {
                    it.copy(
                        continueReading = session,
                        continueReadingChapterName = chapterName,
                        continueReadingTotalVerses = totalVerses
                    )
                }
            } catch (e: Throwable) {
                Log.e("TodayViewModel", "Failed to load continue reading session", e)
            }
        }
    }

    fun loadDailyAyah(refreshTranslation: Boolean = false) {
        viewModelScope.launch { refreshContent(refreshTranslation) }
    }

    suspend fun refreshContent(refreshTranslation: Boolean = false) {
        val hasVerse = _state.value.verse != null
        _state.update { it.copy(isLoading = !hasVerse, error = null) }
        try {
            val loaded = dailyVerseLoader.loadForToday(refreshTranslation)
            if (loaded == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        verse = null,
                        verseReferenceLabel = null,
                        error = AppError(AppErrorKind.NotFound)
                    )
                }
                return
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    verse = loaded.verse,
                    verseReferenceLabel = loaded.referenceLabel,
                    error = null,
                    isOfflineData = loaded.fromCache
                )
            }
            loadContinueReading()
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.toAppError(), isOfflineData = false) }
        }
    }
}
