package app.kamy.qalbuApp.features.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.infrastructure.location.LocationProvider
import app.kamy.qalbuApp.infrastructure.repository.AlAdhanRepository
import app.kamy.qalbuApp.infrastructure.repository.PrayerEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class PrayerTheme { DAYLIGHT, NIGHT }

data class PrayerUiState(
    val isLoading: Boolean = false,
    val timings: List<PrayerEntry> = emptyList(),
    val activePrayer: PrayerType? = null,
    val nextPrayer: PrayerType? = null,
    val countdown: String = "--:--:--",
    val cityName: String? = null,
    val hijriLabel: String? = null,
    val gregorianLabel: String? = null,
    val theme: PrayerTheme = PrayerTheme.DAYLIGHT,
    val needsPermission: Boolean = false,
    val error: String? = null
)

/**
 * Mirrors iOS Features/Discovery/ViewModels/PrayerDashboardViewModel.swift
 * + PrayerTimesViewModel.swift combined into one Compose-friendly state holder.
 */
@HiltViewModel
class PrayerDashboardViewModel @Inject constructor(
    private val repository: AlAdhanRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _state = MutableStateFlow(PrayerUiState())
    val state: StateFlow<PrayerUiState> = _state.asStateFlow()

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)

    init {
        viewModelScope.launch { refresh() }
        // 1s ticker recomputes active prayer + countdown every second.
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                recomputeActiveAndCountdown()
            }
        }
    }

    suspend fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        if (!locationProvider.hasAnyPermission()) {
            _state.update { it.copy(isLoading = false, needsPermission = true) }
            return
        }
        val loc = locationProvider.currentLocation()
        if (loc == null) {
            _state.update { it.copy(isLoading = false, error = "Could not determine location") }
            return
        }
        val cityLabel = locationProvider.reverseGeocodeCity(loc.latitude, loc.longitude)
            ?: locationProvider.coordinateLabel(loc.latitude, loc.longitude)
        try {
            val result = repository.fetchTimings(
                latitude = loc.latitude,
                longitude = loc.longitude,
                cityName = cityLabel
            )
            _state.update {
                it.copy(
                    isLoading = false,
                    timings = result.timings,
                    cityName = result.cityName ?: cityLabel,
                    hijriLabel = result.hijriLabel,
                    gregorianLabel = result.gregorianLabel,
                    needsPermission = false,
                    error = null
                )
            }
            recomputeActiveAndCountdown()
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.message ?: "Prayer fetch failed") }
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch { refresh() }
    }

    private fun recomputeActiveAndCountdown() {
        val now = Date()
        val timings = _state.value.timings.ifEmpty { return }
        val active = timings.lastOrNull { it.date.before(now) }?.type ?: timings.first().type
        val next = timings.firstOrNull { it.date.after(now) } ?: timings.first()
        val deltaMs = (next.date.time - now.time).coerceAtLeast(0L)
        val seconds = (deltaMs / 1000L) % 60
        val minutes = (deltaMs / (1000L * 60)) % 60
        val hours = (deltaMs / (1000L * 60 * 60))
        val countdown = "%02d:%02d:%02d".format(hours, minutes, seconds)
        val theme = if (active == PrayerType.MAGHRIB || active == PrayerType.ISHA)
            PrayerTheme.NIGHT else PrayerTheme.DAYLIGHT
        _state.update {
            it.copy(
                activePrayer = active,
                nextPrayer = next.type,
                countdown = countdown,
                theme = theme
            )
        }
    }

    fun formatPrayerTime(date: Date): String = timeFormatter.format(date)
}
