package app.kamy.qalbuApp.features.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.domain.model.QFTranslation
import app.kamy.qalbuApp.domain.model.UserProfilePayload
import app.kamy.qalbuApp.domain.prayer.PrayerCalculationMethod
import app.kamy.qalbuApp.domain.prayer.PrayerMethodOption
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.qalbuApp.infrastructure.preferences.DailyVerseNotificationStore
import app.kamy.qalbuApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.qalbuApp.infrastructure.repository.AlAdhanRepository
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReflectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val selectedTranslationId: Int = 0,
    val selectedTranslationName: String = "",
    val fontScale: Float = 1.0f,
    val showTranslation: Boolean = true,
    val prayerMethod: PrayerCalculationMethod = PrayerCalculationMethod.defaultMethod,
    val prayerMethods: List<PrayerMethodOption> = emptyList(),
    val prayerMethodsLoading: Boolean = false,
    val dailyVerseEnabled: Boolean = true,
    val reminderHour: Int = DailyVerseNotificationStore.DEFAULT_HOUR,
    val reminderMinute: Int = DailyVerseNotificationStore.DEFAULT_MINUTE,
    val reminderTimeLabel: String = ""
)

/**
 * Mirrors iOS Features/Settings/ViewModels/ProfileViewModel.swift +
 * TranslatorSelectionViewModel.swift.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userSession: UserSession,
    private val reflectRepository: ReflectRepository,
    private val contentRepository: ContentRepository,
    private val alAdhanRepository: AlAdhanRepository,
    private val prayerMethodStore: PrayerCalculationStore,
    private val translationStore: TranslationPreferencesStore,
    private val notificationStore: DailyVerseNotificationStore
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState(isSignedIn = userSession.isSignedIn.value))
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        syncPreferencesIntoState()
        viewModelScope.launch {
            userSession.isSignedIn.collect { signedIn ->
                _state.update { it.copy(isSignedIn = signedIn) }
                if (signedIn) fetchProfile() else _state.update { it.copy(profile = null) }
            }
        }
        viewModelScope.launch {
            prayerMethodStore.method.collect { method ->
                _state.update { it.copy(prayerMethod = method) }
            }
        }
        viewModelScope.launch {
            translationStore.translationId.collect { id ->
                _state.update { it.copy(selectedTranslationId = id) }
            }
        }
        viewModelScope.launch {
            translationStore.translationName.collect { name ->
                _state.update { it.copy(selectedTranslationName = name) }
            }
        }
        viewModelScope.launch {
            translationStore.showTranslation.collect { show ->
                _state.update { it.copy(showTranslation = show) }
            }
        }
        viewModelScope.launch {
            notificationStore.enabled.collect { enabled ->
                _state.update { it.copy(dailyVerseEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            notificationStore.hour.collect {
                _state.update {
                    it.copy(
                        reminderHour = notificationStore.morningHour(),
                        reminderTimeLabel = notificationStore.formattedMorningTime()
                    )
                }
            }
        }
        viewModelScope.launch {
            notificationStore.minute.collect {
                _state.update {
                    it.copy(
                        reminderMinute = notificationStore.morningMinute(),
                        reminderTimeLabel = notificationStore.formattedMorningTime()
                    )
                }
            }
        }
    }

    private fun syncPreferencesIntoState() {
        _state.update {
            it.copy(
                prayerMethod = prayerMethodStore.current(),
                selectedTranslationId = translationStore.currentTranslationId(),
                selectedTranslationName = translationStore.translationName.value,
                showTranslation = translationStore.showTranslation.value,
                dailyVerseEnabled = notificationStore.isEnabled(),
                reminderHour = notificationStore.morningHour(),
                reminderMinute = notificationStore.morningMinute(),
                reminderTimeLabel = notificationStore.formattedMorningTime()
            )
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

    fun selectTranslation(translation: QFTranslation) {
        val label = translation.authorName.ifBlank { translation.name }
        translationStore.setTranslation(translation.id, label)
        _state.update { it.copy(showTranslatorSheet = false) }
    }

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

    fun togglePrayerSheet(show: Boolean) {
        _state.update { it.copy(showPrayerSheet = show) }
        if (show && _state.value.prayerMethods.isEmpty()) loadPrayerMethods()
    }

    private fun loadPrayerMethods() {
        _state.update { it.copy(prayerMethodsLoading = true) }
        viewModelScope.launch {
            runCatching { alAdhanRepository.fetchCalculationMethods() }
                .onSuccess { methods ->
                    _state.update { it.copy(prayerMethodsLoading = false, prayerMethods = methods) }
                }
                .onFailure {
                    _state.update { it.copy(prayerMethodsLoading = false) }
                }
        }
    }

    fun toggleNotifTimeSheet(show: Boolean) = _state.update { it.copy(showNotifTimeSheet = show) }

    fun setShowTranslation(enabled: Boolean) {
        translationStore.setShowTranslation(enabled)
    }

    fun setDailyVerseEnabled(enabled: Boolean) {
        notificationStore.setEnabled(enabled)
        DailyVerseNotificationScheduler.reschedule(appContext)
    }

    fun saveReminderTime(hour: Int, minute: Int) {
        notificationStore.setMorningTime(hour, minute)
        DailyVerseNotificationScheduler.reschedule(appContext)
        _state.update {
            it.copy(
                showNotifTimeSheet = false,
                reminderHour = notificationStore.morningHour(),
                reminderMinute = notificationStore.morningMinute(),
                reminderTimeLabel = notificationStore.formattedMorningTime()
            )
        }
    }

    fun setPrayerMethod(method: PrayerCalculationMethod) {
        prayerMethodStore.setMethod(method)
    }

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
