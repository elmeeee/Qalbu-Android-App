package app.kamy.qalbuApp.features.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.core.error.AppErrorKind
import app.kamy.qalbuApp.core.error.invalidateIfAuthenticationFailure
import app.kamy.qalbuApp.core.error.isAuthenticationFailure
import app.kamy.qalbuApp.core.error.toAppError
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.infrastructure.network.NetworkMonitor
import app.kamy.qalbuApp.domain.quran.DailyVerseOccasion
import app.kamy.qalbuApp.infrastructure.quran.DailyVerseLoader
import app.kamy.qalbuApp.domain.model.UserProfilePayload
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.domain.share.VerseShareTextComposer
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReflectRepository
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

data class TodayUiState(
    val isLoading: Boolean = false,
    val verse: RandomAyahPayload? = null,
    val verseReferenceLabel: String? = null,
    val recitations: List<RecitationPayload> = emptyList(),
    val selectedRecitationId: Int = 6,
    val translationId: Int = AppConfig.defaultTranslationId,
    val error: AppError? = null,
    val tafsirLoading: Boolean = false,
    val tafsir: TafsirPayload? = null,
    val tafsirError: AppError? = null,
    val showTafsir: Boolean = false,
    val isPublishing: Boolean = false,
    val publishToast: String? = null,
    val publishToastIsError: Boolean = false,
    val profile: UserProfilePayload? = null,
    val profileLoading: Boolean = false,
    val aiShareVisible: Boolean = false,
    val aiShareLoading: Boolean = false,
    val aiShareDraft: String = "",
    val aiShareError: AppError? = null,
    val showReciterSheet: Boolean = false,
    val isOfflineData: Boolean = false,
    val showTransliteration: Boolean = false,
    val showTranslation: Boolean = true,
    val verseOccasion: DailyVerseOccasion? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository,
    private val reflectRepository: ReflectRepository,
    private val shareComposer: VerseShareTextComposer,
    private val userSession: UserSession,
    private val translationStore: TranslationPreferencesStore,
    private val dailyVerseLoader: DailyVerseLoader
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state.asStateFlow()

    init {
        _state.update {
            it.copy(
                translationId = translationStore.currentTranslationId(),
                selectedRecitationId = translationStore.currentRecitationId(),
                showTranslation = translationStore.showTranslation.value,
                showTransliteration = translationStore.showTransliteration.value
            )
        }
        loadDailyAyahWithRecitations()
        loadProfile()
        viewModelScope.launch {
            userSession.isSignedIn.collect { signedIn ->
                if (signedIn) loadProfile() else _state.update { it.copy(profile = null) }
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

    private fun loadProfile() {
        if (!userSession.isSignedIn.value) return
        _state.update { it.copy(profileLoading = true) }
        viewModelScope.launch {
            try {
                val p = reflectRepository.fetchMyProfile()
                _state.update { it.copy(profile = p, profileLoading = false) }
            } catch (t: Throwable) {
                userSession.invalidateIfAuthenticationFailure(t)
                _state.update { it.copy(profileLoading = false, profile = null) }
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
                        contentRepository.getRecitations()
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
                        isOfflineData = true
                    )
                }
            }
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

    private suspend fun loadTafsir(verseKey: String) {
        try {
            val tafsir = contentRepository.getTafsirByAyah(ayahKey = verseKey)
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

    fun publishReflectionToReflect(authorId: String, idempotencyKeyDate: Date = Date()) {
        val verse = _state.value.verse ?: return
        val verseKey = verse.verseKey ?: return
        val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(idempotencyKeyDate)
        val idempotencyKey = "reflect:$verseKey:$dayKey"

        viewModelScope.launch {
            val body = _state.value.aiShareDraft.trim().ifBlank {
                shareComposer.quickReflectionText(verse, _state.value.verseReferenceLabel)
            }
            if (body.isBlank()) return@launch
            _state.update { it.copy(isPublishing = true, publishToast = null) }
            try {
                reflectRepository.createReflectionPost(body, verseKey, authorId, idempotencyKey)
                _state.update {
                    it.copy(
                        isPublishing = false,
                        publishToast = appContext.getString(R.string.published_to_reflect),
                        publishToastIsError = false,
                        aiShareVisible = false
                    )
                }
            } catch (t: Throwable) {
                userSession.invalidateIfAuthenticationFailure(t)
                _state.update {
                    it.copy(
                        isPublishing = false,
                        publishToast = if (t.isAuthenticationFailure()) appContext.getString(R.string.session_expired)
                        else t.message ?: appContext.getString(R.string.publish_failed),
                        publishToastIsError = true
                    )
                }
            }
        }
    }

    fun clearPublishToast() {
        _state.update { it.copy(publishToast = null) }
    }

    fun isSignedIn(): Boolean = userSession.isSignedIn.value
}
