package app.kamy.qalbuApp.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.domain.model.QFTranslation
import app.kamy.qalbuApp.domain.model.UserProfilePayload
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReflectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val isLoading: Boolean = false,
    val authBusy: Boolean = false,
    val profile: UserProfilePayload? = null,
    val isSignedIn: Boolean = false,
    val error: String? = null,
    val showFontScaleSheet: Boolean = false,
    val showTranslatorSheet: Boolean = false,
    val showPrayerSheet: Boolean = false,
    val showNotifTimeSheet: Boolean = false,
    val translations: List<QFTranslation> = emptyList(),
    val translationsLoading: Boolean = false,
    val translatorQuery: String = "",
    val fontScale: Float = 1.0f,
    val showTranslation: Boolean = true,
    val prayerMethod: Int = 20,
    val dailyVerseEnabled: Boolean = true
)

/**
 * Mirrors iOS Features/Settings/ViewModels/ProfileViewModel.swift +
 * TranslatorSelectionViewModel.swift.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userSession: UserSession,
    private val reflectRepository: ReflectRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState(isSignedIn = userSession.isSignedIn.value))
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userSession.isSignedIn.collect { signedIn ->
                _state.update { it.copy(isSignedIn = signedIn) }
                if (signedIn) fetchProfile() else _state.update { it.copy(profile = null) }
            }
        }
    }

    fun fetchProfile() {
        if (!userSession.isSignedIn.value) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { reflectRepository.fetchMyProfile() }
                .onSuccess { p -> _state.update { it.copy(isLoading = false, profile = p) } }
                .onFailure { t -> _state.update { it.copy(isLoading = false, error = t.message) } }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(authBusy = true) }
            userSession.clear()
            _state.update { it.copy(authBusy = false, profile = null) }
        }
    }

    fun openFontScale() = _state.update { it.copy(showFontScaleSheet = true) }
    fun closeFontScale() = _state.update { it.copy(showFontScaleSheet = false) }
    fun setFontScale(value: Float) = _state.update { it.copy(fontScale = value.coerceIn(0.85f, 1.35f)) }

    fun openTranslator() {
        _state.update { it.copy(showTranslatorSheet = true) }
        if (_state.value.translations.isEmpty()) loadTranslations()
    }
    fun closeTranslator() = _state.update { it.copy(showTranslatorSheet = false) }
    fun setTranslatorQuery(q: String) = _state.update { it.copy(translatorQuery = q) }

    private fun loadTranslations() {
        _state.update { it.copy(translationsLoading = true) }
        viewModelScope.launch {
            runCatching { contentRepository.getTranslations() }
                .onSuccess { list ->
                    val sorted = list.sortedWith(
                        compareByDescending<QFTranslation> { it.languageName.equals("English", true) }
                            .thenBy { it.languageName }
                            .thenBy { it.authorName }
                    )
                    _state.update { it.copy(translationsLoading = false, translations = sorted) }
                }
                .onFailure { _state.update { it.copy(translationsLoading = false) } }
        }
    }

    fun togglePrayerSheet(show: Boolean) = _state.update { it.copy(showPrayerSheet = show) }
    fun toggleNotifTimeSheet(show: Boolean) = _state.update { it.copy(showNotifTimeSheet = show) }
    fun setShowTranslation(enabled: Boolean) = _state.update { it.copy(showTranslation = enabled) }
    fun setDailyVerseEnabled(enabled: Boolean) = _state.update { it.copy(dailyVerseEnabled = enabled) }
    fun setPrayerMethod(method: Int) = _state.update { it.copy(prayerMethod = method) }

    val filteredTranslations: List<QFTranslation>
        get() {
            val q = _state.value.translatorQuery.trim().lowercase()
            if (q.isEmpty()) return _state.value.translations
            return _state.value.translations.filter {
                it.name.lowercase().contains(q) ||
                    it.authorName.lowercase().contains(q) ||
                    it.languageName.lowercase().contains(q)
            }
        }
}
