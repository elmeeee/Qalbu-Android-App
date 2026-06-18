package app.kamy.saatApp.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppStrings
import app.kamy.saatApp.infrastructure.location.LocationProvider
import app.kamy.saatApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.saatApp.infrastructure.preferences.LocationMode
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.infrastructure.preferences.SavedManualLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME,
    LOCATION,
    NOTIFICATIONS,
    WIDGET
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val locationQuery: String = "",
    val savingLocation: Boolean = false,
    val locationError: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val strings: AppStrings,
    private val onboardingStore: OnboardingStore,
    private val locationProvider: LocationProvider,
    private val locationPrefs: LocationPreferencesStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun nextStep() {
        _state.update {
            val next = when (it.step) {
                OnboardingStep.WELCOME -> OnboardingStep.LOCATION
                OnboardingStep.LOCATION -> OnboardingStep.NOTIFICATIONS
                OnboardingStep.NOTIFICATIONS -> OnboardingStep.WIDGET
                OnboardingStep.WIDGET -> OnboardingStep.WIDGET
            }
            it.copy(step = next, locationError = null)
        }
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
                        locationError = strings.getString(R.string.location_search_not_found)
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
            viewModelScope.launch {
                val loc = locationProvider.currentLocation()
                if (loc != null) {
                    val geocode = locationProvider.reverseGeocode(loc.latitude, loc.longitude)
                    val label = geocode.cityName
                        ?: locationProvider.coordinateLabel(loc.latitude, loc.longitude)
                    locationPrefs.saveActiveLabel(label)
                }
                nextStep()
            }
        } else {
            _state.update {
                it.copy(locationError = strings.getString(R.string.onboarding_location_denied))
            }
        }
    }

    fun skipLocation() {
        nextStep()
    }

    fun onNotificationPermissionResult() {
        onboardingStore.markNotificationsHandled()
        DailyVerseNotificationScheduler.reschedule(appContext)
        nextStep()
    }

    fun skipNotifications() {
        onboardingStore.markNotificationsHandled()
        nextStep()
    }

    fun completeOnboarding() {
        onboardingStore.markComplete()
        DailyVerseNotificationScheduler.reschedule(appContext)
    }
}
