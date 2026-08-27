package app.kamy.saatApp.features.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.error.AppErrorKind
import app.kamy.saatApp.core.error.toAppError
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.infrastructure.network.NetworkMonitor
import app.kamy.saatApp.domain.quran.DailyVerseOccasion
import app.kamy.saatApp.infrastructure.quran.DailyVerseLoader
import app.kamy.saatApp.domain.model.RecitationPayload
import app.kamy.saatApp.domain.model.TafsirPayload
import app.kamy.saatApp.domain.share.VerseShareTextComposer
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.saatApp.infrastructure.repository.QuranRepository
import app.kamy.saatApp.infrastructure.repository.ReadingSessionRepository
import app.kamy.saatApp.domain.model.ReadingSession
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

import app.kamy.saatApp.infrastructure.repository.DailyQuoteRepository
import app.kamy.saatApp.infrastructure.repository.DailyQuoteItem
import app.kamy.saatApp.infrastructure.repository.KhgtCalendarRepository
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore

data class TodayUiState(
    val isLoading: Boolean = false,
    val verse: RandomAyahPayload? = null,
    val verseReferenceLabel: String? = null,
    val dailyQuote: DailyQuoteItem? = null,
    val recitations: List<RecitationPayload> = emptyList(),
    val selectedRecitationId: Int = 6,
    val translationId: Int = LocalQuranConfig.DEFAULT_TRANSLATION_ID,
    val error: AppError? = null,
    val tafsirLoading: Boolean = false,
    val selectedTafsirSource: String = LocalQuranConfig.TAFSIR_WAJIZ_ID,
    val tafsir: TafsirPayload? = null,
    val tafsirError: AppError? = null,
    val showTafsir: Boolean = false,
    val aiShareVisible: Boolean = false,
    val aiShareLoading: Boolean = false,
    val aiShareDraft: String = "",
    val aiShareError: AppError? = null,
    val showReciterSheet: Boolean = false,
    val isOfflineData: Boolean = false,
    val showTransliteration: Boolean = false,
    val showTranslation: Boolean = true,
    val verseOccasion: DailyVerseOccasion? = null,
    val continueReading: ReadingSession? = null,
    val continueReadingChapterName: String? = null,
    val continueReadingTotalVerses: Int? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val quranRepository: QuranRepository,
    private val shareComposer: VerseShareTextComposer,
    private val translationStore: TranslationPreferencesStore,
    private val dailyVerseLoader: DailyVerseLoader,
    private val readingSessions: ReadingSessionRepository,
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val khgtCalendarRepository: KhgtCalendarRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state.asStateFlow()

    init {
        _state.update {
            it.copy(
                translationId = translationStore.currentTranslationId(),
                selectedRecitationId = translationStore.currentRecitationId(),
                selectedTafsirSource = translationStore.currentTafsirSource(),
                showTranslation = translationStore.showTranslation.value,
                showTransliteration = translationStore.showTransliteration.value
            )
        }
        loadDailyAyahWithRecitations()
        loadContinueReading()
        viewModelScope.launch {
            val khgtInfo = runCatching { khgtCalendarRepository.todayInfo() }.getOrNull()
            val currentLang = AppLanguageStore.from(appContext).current()
            val initialQuote = dailyQuoteRepository.getTodayQuote(currentLang, khgtInfo?.eventTitle)
            _state.update { it.copy(dailyQuote = initialQuote) }

            AppLanguageStore.from(appContext).currentFlow.collect { lang ->
                val updatedQuote = dailyQuoteRepository.getTodayQuote(lang, khgtInfo?.eventTitle)
                _state.update { it.copy(dailyQuote = updatedQuote) }
            }
        }
        viewModelScope.launch {
            translationStore.translationId.drop(1).collect {
                _state.update { it.copy(translationId = translationStore.currentTranslationId()) }
                shareComposer.clearCaches()
                loadDailyAyahWithRecitations(refreshTranslation = true)
            }
        }
        viewModelScope.launch {
            translationStore.showTranslation.collect { enabled ->
                _state.update { it.copy(showTranslation = enabled) }
            }
        }
        viewModelScope.launch {
            translationStore.showTransliteration.collect { enabled ->
                _state.update { it.copy(showTransliteration = enabled) }
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



    fun loadDailyAyahWithRecitations(refreshTranslation: Boolean = false) {
        viewModelScope.launch { refreshContent(refreshTranslation) }
    }

    suspend fun refreshContent(refreshTranslation: Boolean = false) {
        val hasVerse = _state.value.verse != null
        _state.update { it.copy(isLoading = !hasVerse, error = null) }
        try {
            coroutineScope {
                val verseDeferred = async { dailyVerseLoader.loadForToday(refreshTranslation) }
                val recitationsDeferred = async {
                    if (_state.value.recitations.isEmpty()) {
                        quranRepository.getRecitations()
                    } else {
                        _state.value.recitations
                    }
                }
                val loaded = verseDeferred.await()
                val recitations = recitationsDeferred.await()
                if (loaded == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            verse = null,
                            verseReferenceLabel = null,
                            verseOccasion = null,
                            recitations = recitations,
                            error = AppError(AppErrorKind.NotFound)
                        )
                    }
                    return@coroutineScope
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        verse = loaded.verse,
                        verseReferenceLabel = loaded.referenceLabel,
                        verseOccasion = loaded.occasion,
                        recitations = recitations,
                        error = null,
                        isOfflineData = loaded.fromCache
                    )
                }
            }
            loadContinueReading()
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.toAppError(), isOfflineData = false) }
        }
    }

    fun openReciterSheet() {
        _state.update { it.copy(showReciterSheet = true) }
    }

    fun dismissReciterSheet() {
        _state.update { it.copy(showReciterSheet = false) }
    }

    fun selectRecitation(id: Int) {
        if (id <= 0 || id == _state.value.selectedRecitationId) {
            _state.update { it.copy(showReciterSheet = false) }
            return
        }
        translationStore.setRecitation(id)
        _state.update { it.copy(selectedRecitationId = id, showReciterSheet = false) }
        loadDailyAyahWithRecitations(refreshTranslation = true)
    }

    fun openTafsir() {
        val verse = _state.value.verse ?: return
        val verseKey = verse.verseKey ?: return
        _state.update {
            it.copy(showTafsir = true, tafsirLoading = true, tafsir = null, tafsirError = null)
        }
        viewModelScope.launch {
            loadTafsir(verseKey)
        }
    }

    fun reloadTafsir() {
        val verseKey = _state.value.verse?.verseKey ?: return
        _state.update { it.copy(tafsirLoading = true, tafsirError = null) }
        viewModelScope.launch { loadTafsir(verseKey) }
    }

    fun selectTafsirSource(sourceId: String) {
        translationStore.setTafsirSource(sourceId)
        _state.update { it.copy(selectedTafsirSource = sourceId, tafsirLoading = true, tafsirError = null) }
        val verseKey = _state.value.verse?.verseKey ?: return
        viewModelScope.launch { loadTafsir(verseKey) }
    }

    private suspend fun loadTafsir(verseKey: String) {
        val sourceId = translationStore.currentTafsirSource()
        _state.update { it.copy(selectedTafsirSource = sourceId) }
        try {
            val tafsir = quranRepository.getTafsirByAyah(ayahKey = verseKey, sourceId = sourceId)
            _state.update { it.copy(tafsir = tafsir, tafsirLoading = false, tafsirError = null) }
        } catch (t: Throwable) {
            _state.update {
                it.copy(tafsirLoading = false, tafsirError = t.toAppError())
            }
        }
    }

    fun dismissTafsir() {
        _state.update { it.copy(showTafsir = false, tafsirError = null) }
    }

    fun openAiShare() {
        if (_state.value.verse == null) return
        _state.update {
            it.copy(
                aiShareVisible = true,
                aiShareLoading = true,
                aiShareDraft = "",
                aiShareError = null
            )
        }
        loadAiShareDraft(forceRefresh = false)
    }

    fun dismissAiShare() {
        _state.update {
            it.copy(
                aiShareVisible = false,
                aiShareLoading = false,
                aiShareError = null
            )
        }
    }

    fun updateAiShareDraft(text: String) {
        _state.update { it.copy(aiShareDraft = text) }
    }

    fun regenerateAiShare() = loadAiShareDraft(forceRefresh = true)

    private fun loadAiShareDraft(forceRefresh: Boolean) {
        val verse = _state.value.verse ?: return
        viewModelScope.launch {
            _state.update { it.copy(aiShareLoading = true, aiShareError = null) }
            runCatching {
                shareComposer.prepareShareText(
                    verse,
                    _state.value.verseReferenceLabel,
                    forceRefresh = forceRefresh
                )
            }.onSuccess { text ->
                _state.update { it.copy(aiShareLoading = false, aiShareDraft = text) }
            }.onFailure { t ->
                _state.update {
                    it.copy(
                        aiShareLoading = false,
                        aiShareError = t.toAppError()
                    )
                }
            }
        }
    }
}
