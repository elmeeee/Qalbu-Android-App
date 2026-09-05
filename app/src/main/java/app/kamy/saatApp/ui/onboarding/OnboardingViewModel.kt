package app.kamy.saatApp.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
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
    PERMISSIONS,
    PRAYER_NOTIFICATIONS
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.LANGUAGE,
    val selectedLanguage: AppLanguage = AppLanguage.INDONESIAN,
    val locationGranted: Boolean = false,
    val notificationGranted: Boolean = false,
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

    private val _state = MutableStateFlow(
        OnboardingUiState(
            selectedLanguage = appLanguageStore.current(),
            locationGranted = checkLocationPermission(),
            notificationGranted = checkNotificationPermission()
        )
    )
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

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                appContext, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun refreshPermissions() {
        _state.update {
            it.copy(
                locationGranted = checkLocationPermission(),
                notificationGranted = checkNotificationPermission()
            )
        }
    }

    fun nextStep() {
        _state.update {
            val next = when (it.step) {
                OnboardingStep.LANGUAGE -> OnboardingStep.WELCOME
                OnboardingStep.WELCOME -> OnboardingStep.PERMISSIONS
                OnboardingStep.PERMISSIONS -> OnboardingStep.PRAYER_NOTIFICATIONS
                OnboardingStep.PRAYER_NOTIFICATIONS -> OnboardingStep.PRAYER_NOTIFICATIONS
            }
            it.copy(step = next, locationError = null)
        }
    }

    fun previousStep() {
        _state.update {
            val prev = when (it.step) {
                OnboardingStep.LANGUAGE -> OnboardingStep.LANGUAGE
                OnboardingStep.WELCOME -> OnboardingStep.LANGUAGE
                OnboardingStep.PERMISSIONS -> OnboardingStep.WELCOME
                OnboardingStep.PRAYER_NOTIFICATIONS -> OnboardingStep.PERMISSIONS
            }
            it.copy(step = prev, locationError = null)
        }
    }

    fun selectLanguage(language: AppLanguage) {
        appLanguageStore.set(language)
        _state.update { it.copy(selectedLanguage = language) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) {
            locationPrefs.setMode(LocationMode.GPS)
            _state.update { it.copy(locationGranted = true, locationError = null) }
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
                it.copy(
                    locationGranted = false,
                    locationError = appStrings.getString(R.string.onboarding_location_denied)
                )
            }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _state.update { it.copy(notificationGranted = granted) }
        onboardingStore.markNotificationsHandled()
        DailyVerseNotificationScheduler.reschedule(appContext)
        if (!appContext.canUseFullScreenIntent()) {
            runCatching { appContext.openFullScreenIntentSettings() }
        }
    }

    fun skipPermissions() {
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
