package app.kamy.qalbuApp.features.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.core.error.invalidateIfAuthenticationFailure
import app.kamy.qalbuApp.core.error.isAuthenticationFailure
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.infrastructure.preferences.DailyVerseSnapshotStore
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
    val error: String? = null,
    val tafsirLoading: Boolean = false,
    val tafsir: TafsirPayload? = null,
    val tafsirError: String? = null,
    val showTafsir: Boolean = false,
    val isPublishing: Boolean = false,
    val publishToast: String? = null,
    val publishToastIsError: Boolean = false,
    val profile: UserProfilePayload? = null,
    val profileLoading: Boolean = false,
    val aiShareVisible: Boolean = false,
    val aiShareLoading: Boolean = false,
    val aiShareDraft: String = "",
    val aiShareError: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository,
    private val reflectRepository: ReflectRepository,
    private val shareComposer: VerseShareTextComposer,
    private val userSession: UserSession,
    private val translationStore: TranslationPreferencesStore
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state.asStateFlow()

    init {
        _state.update { it.copy(translationId = translationStore.currentTranslationId()) }
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
                loadDailyAyahWithRecitations()
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

    fun loadDailyAyahWithRecitations() {
        viewModelScope.launch { refreshContent() }
    }

    suspend fun refreshContent() {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            coroutineScope {
                val verseDeferred = async { runCatching { contentRepository.getRandomAyah() }.getOrNull() }
                val chaptersDeferred = async { runCatching { contentRepository.getChapters() }.getOrDefault(emptyList()) }
                val recitationsDeferred = async {
                    if (_state.value.recitations.isEmpty()) {
                        runCatching { contentRepository.getRecitations() }.getOrDefault(emptyList())
                    } else {
                        _state.value.recitations
                    }
                }
                val verse = verseDeferred.await()
                val chapters = chaptersDeferred.await()
                val chapterName = verse?.chapterNumber?.let { num ->
                    chapters.find { it.id == num }?.displayComplexName
                }
                val recitations = recitationsDeferred.await()
                _state.update {
                    it.copy(
                        isLoading = false,
                        verse = verse,
                        verseReferenceLabel = verse?.referenceLabel(chapterName),
                        recitations = recitations,
                        error = if (verse == null) {
                            it.error ?: appContext.getString(R.string.verse_of_day_load_failed)
                        } else {
                            null
                        }
                    )
                }
                verse?.let { v ->
                    DailyVerseSnapshotStore.save(appContext, v, chapterName)
                    shareComposer.prefetchShareTextIfNeeded(v, _state.value.verseReferenceLabel)
                }
            }
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.message ?: appContext.getString(R.string.verse_load_failed)) }
        }
    }

    fun selectRecitation(id: Int) {
        _state.update { it.copy(selectedRecitationId = id) }
    }

    fun openTafsir() {
        val verse = _state.value.verse ?: return
        val verseKey = verse.verseKey ?: return
        _state.update {
            it.copy(showTafsir = true, tafsirLoading = true, tafsir = null, tafsirError = null)
        }
        viewModelScope.launch {
            shareComposer.prefetchShareTextIfNeeded(verse, _state.value.verseReferenceLabel)
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
            val tafsir = contentRepository.getTafsirByAyah(resourceId = "169", ayahKey = verseKey)
            _state.update { it.copy(tafsir = tafsir, tafsirLoading = false, tafsirError = null) }
        } catch (t: Throwable) {
            _state.update {
                it.copy(tafsirLoading = false, tafsirError = t.message ?: appContext.getString(R.string.tafsir_load_failed))
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
                        aiShareError = t.message ?: appContext.getString(R.string.share_generate_failed)
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
