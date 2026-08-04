package app.kamy.saatApp.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.core.locale.AppStrings
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.location.LocationProvider
import app.kamy.saatApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.LocationMode
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.SavedManualLocation
import app.kamy.saatApp.ui.permissions.canUseFullScreenIntent
import app.kamy.saatApp.ui.permissions.openFullScreenIntentSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep {
    LANGUAGE,
    WELCOME,
    LOCATION,
    NOTIFICATIONS,
    PRAYER_NOTIFICATIONS
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.LANGUAGE,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val locationQuery: String = "",
    val savingLocation: Boolean = false,
    val locationError: String? = null,
    val prayerAdzanToggles: Map<PrayerType, Boolean> = emptyMap()
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appStrings: AppStrings,
    private val onboardingStore: OnboardingStore,
    private val locationProvider: LocationProvider,
    private val locationPrefs: LocationPreferencesStore,
    private val prayerPrefs: PrayerNotificationPreferencesStore,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState(selectedLanguage = appLanguageStore.current()))
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()
    val strings: AppStrings = appStrings

    init {
        _state.update {
            it.copy(
                prayerAdzanToggles = PrayerType.ADZAN_NOTIFICATION_PRAYERS.associateWith { type ->
                    prayerPrefs.isPrayerEnabled(type)
                }
            )
        }
    }

    fun nextStep() {
        _state.update {
            val next = when (it.step) {
                OnboardingStep.LANGUAGE -> OnboardingStep.WELCOME
                OnboardingStep.WELCOME -> OnboardingStep.LOCATION
                OnboardingStep.LOCATION -> OnboardingStep.NOTIFICATIONS
                OnboardingStep.NOTIFICATIONS -> OnboardingStep.PRAYER_NOTIFICATIONS
                OnboardingStep.PRAYER_NOTIFICATIONS -> OnboardingStep.PRAYER_NOTIFICATIONS
            }
            it.copy(step = next, locationError = null)
        }
    }

    fun selectLanguage(language: AppLanguage) {
        appLanguageStore.set(language)
        _state.update { it.copy(selectedLanguage = language) }
    }

    fun updateLocationQuery(query: String) {
        _state.update { it.copy(locationQuery = query, locationError = null) }
    }

    fun saveManualLocation() {
        val query = _state.value.locationQuery.trim()
        if (query.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(savingLocation = true, locationError = null) }
            val geocoded = locationProvider.forwardGeocode(query)
            if (geocoded?.latitude == null || geocoded.longitude == null) {
                _state.update {
                    it.copy(
                        savingLocation = false,
                        locationError = appStrings.getString(R.string.location_search_not_found)
                    )
                }
                return@launch
            }
            locationPrefs.saveManual(
                SavedManualLocation(
                    latitude = geocoded.latitude,
                    longitude = geocoded.longitude,
                    label = geocoded.cityName ?: query,
                    countryCode = geocoded.countryCode
                )
            )
            locationPrefs.saveActiveLabel(geocoded.cityName ?: query)
            _state.update { it.copy(savingLocation = false) }
            nextStep()
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) {
            locationPrefs.setMode(LocationMode.GPS)
            nextStep()
            viewModelScope.launch {
                try {
                    val loc = locationProvider.currentLocation()
                    if (loc != null) {
                        val geocode = locationProvider.reverseGeocode(loc.latitude, loc.longitude)
                        val label = geocode.cityName
                            ?: locationProvider.coordinateLabel(loc.latitude, loc.longitude)
                        locationPrefs.saveActiveLabel(label)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            _state.update {
                it.copy(locationError = appStrings.getString(R.string.onboarding_location_denied))
            }
        }
    }

    fun skipLocation() {
        nextStep()
    }

    fun onNotificationPermissionResult() {
        onboardingStore.markNotificationsHandled()
        DailyVerseNotificationScheduler.reschedule(appContext)
        if (!appContext.canUseFullScreenIntent()) {
            runCatching { appContext.openFullScreenIntentSettings() }
        }
        nextStep()
    }

    fun skipNotifications() {
        onboardingStore.markNotificationsHandled()
        nextStep()
    }
    
    fun togglePrayerAdzan(type: PrayerType, enabled: Boolean) {
        prayerPrefs.setPrayerEnabled(type, enabled)
        _state.update {
            it.copy(
                prayerAdzanToggles = it.prayerAdzanToggles.toMutableMap().apply { put(type, enabled) }
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch(Dispatchers.IO) {
            onboardingStore.markComplete()
            DailyVerseNotificationScheduler.reschedule(appContext)
        }
    }
}
