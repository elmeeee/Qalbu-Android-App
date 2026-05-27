package app.kamy.qalbuApp.features.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReflectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class TodayUiState(
    val isLoading: Boolean = false,
    val verse: RandomAyahPayload? = null,
    val recitations: List<RecitationPayload> = emptyList(),
    val selectedRecitationId: Int = 6,
    val translationId: Int = AppConfig.defaultTranslationId,
    val error: String? = null,
    val tafsirLoading: Boolean = false,
    val tafsir: TafsirPayload? = null,
    val showTafsir: Boolean = false,
    val isPublishing: Boolean = false,
    val publishToast: String? = null,
    val publishToastIsError: Boolean = false
)

/**
 * Mirrors iOS TodayDiscoveryViewModel + TodayVerseActionsViewModel +
 * TodayReflectionPublishViewModel combined.
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val reflectRepository: ReflectRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state.asStateFlow()

    init {
        loadDailyAyahWithRecitations()
    }

    fun loadDailyAyahWithRecitations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val verseDeferred = async { contentRepository.getRandomAyah(_state.value.translationId) }
                val recitationsDeferred = async {
                    if (_state.value.recitations.isEmpty()) contentRepository.getRecitations()
                    else _state.value.recitations
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        verse = verseDeferred.await(),
                        recitations = recitationsDeferred.await()
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = t.message ?: "Failed to load verse") }
            }
        }
    }

    fun selectRecitation(id: Int) {
        _state.update { it.copy(selectedRecitationId = id) }
    }

    fun openTafsir() {
        val verseKey = _state.value.verse?.verseKey ?: return
        _state.update { it.copy(showTafsir = true, tafsirLoading = true, tafsir = null) }
        viewModelScope.launch {
            try {
                val tafsir = contentRepository.getTafsirByAyah(resourceId = "169", ayahKey = verseKey)
                _state.update { it.copy(tafsir = tafsir, tafsirLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(tafsirLoading = false, error = t.message) }
            }
        }
    }

    fun dismissTafsir() {
        _state.update { it.copy(showTafsir = false) }
    }

    fun composeShareText(): String {
        val verse = _state.value.verse ?: return ""
        val translation = verse.translations?.firstOrNull()?.text.orEmpty()
        val arabicPlain = verse.textUthmani?.trim().orEmpty()
        val ref = verse.verseKey?.let { "— Quran $it" } ?: ""
        return buildString {
            if (arabicPlain.isNotEmpty()) {
                appendLine(arabicPlain)
                appendLine()
            }
            if (translation.isNotEmpty()) {
                appendLine(translation.replace(Regex("<[^>]+>"), "").trim())
                appendLine()
            }
            if (ref.isNotEmpty()) append(ref)
        }
    }

    fun publishReflectionToReflect(authorId: String, idempotencyKeyDate: Date = Date()) {
        val verse = _state.value.verse ?: return
        val verseKey = verse.verseKey ?: return
        val body = composeShareText().ifBlank { return }
        val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(idempotencyKeyDate)
        val idempotencyKey = "reflect:$verseKey:$dayKey"

        viewModelScope.launch {
            _state.update { it.copy(isPublishing = true, publishToast = null) }
            try {
                reflectRepository.createReflectionPost(body, verseKey, authorId, idempotencyKey)
                _state.update {
                    it.copy(
                        isPublishing = false,
                        publishToast = "Published to Reflect",
                        publishToastIsError = false
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isPublishing = false,
                        publishToast = t.message ?: "Publish failed",
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
