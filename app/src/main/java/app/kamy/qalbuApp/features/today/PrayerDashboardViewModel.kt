package app.kamy.qalbuApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.domain.prayer.PrayerCalculationMethod
import app.kamy.qalbuApp.infrastructure.location.LocationProvider
import app.kamy.qalbuApp.infrastructure.notifications.PrayerNotificationCoordinator
import app.kamy.qalbuApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.qalbuApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.qalbuApp.infrastructure.repository.AlAdhanRepository
import app.kamy.qalbuApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.qalbuApp.infrastructure.repository.PrayerEntry
import app.kamy.qalbuApp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class PrayerTheme { DAYLIGHT, NIGHT }

private const val PRAYER_GRACE_PERIOD_MS = 15 * 60 * 1000L

data class PrayerUiState(
    val isLoading: Boolean = false,
    val timings: List<PrayerEntry> = emptyList(),
    val activePrayer: PrayerType? = null,
    val nextPrayer: PrayerType? = null,
    val countdown: String = "--:--:--",
    val countdownSubtitle: String = "Prayer schedule",
    val isGracePeriod: Boolean = false,
    val cityName: String? = null,
    val hijriLabel: String? = null,
    val gregorianLabel: String? = null,
    val theme: PrayerTheme = PrayerTheme.DAYLIGHT,
    val needsPermission: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PrayerDashboardViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: AlAdhanRepository,
    private val locationProvider: LocationProvider,
    private val prayerMethodStore: PrayerCalculationStore,
    private val prayerNotificationPrefs: PrayerNotificationPreferencesStore
) : ViewModel() {

    private val _state = MutableStateFlow(PrayerUiState())
    val state: StateFlow<PrayerUiState> = _state.asStateFlow()

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)
    private val dayKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var scheduleDayKey: String? = null
    private var dayRefreshInFlight = false

    init {
        viewModelScope.launch { refresh() }
        viewModelScope.launch {
            prayerMethodStore.method.drop(1).collect { refresh() }
        }
        viewModelScope.launch {
            prayerNotificationPrefs.changeTick.drop(1).collect {
                PrayerNotificationCoordinator.rescheduleFromCache(appContext)
            }
        }
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
            _state.update { it.copy(isLoading = false, error = appContext.getString(R.string.location_failed)) }
            return
        }
        val geocode = locationProvider.reverseGeocode(loc.latitude, loc.longitude)
        if (!prayerMethodStore.hasSavedPreference) {
            geocode.countryCode?.let { code ->
                prayerMethodStore.setMethod(PrayerCalculationMethod.forCountryCode(code))
            }
        }
        val cityLabel = geocode.cityName
            ?: locationProvider.coordinateLabel(loc.latitude, loc.longitude)
        val calculationMethod = prayerMethodStore.current()
        try {
            val result = repository.fetchTimings(
                latitude = loc.latitude,
                longitude = loc.longitude,
                cityName = cityLabel,
                method = calculationMethod
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
            scheduleDayKey = dayKey()
            recomputeActiveAndCountdown()
            result.scheduleBundle?.let { bundle ->
                PrayerNotificationCoordinator.onScheduleUpdated(
                    appContext,
                    bundle,
                    loc.latitude,
                    loc.longitude
                )
            }
        } catch (t: Throwable) {
            _state.update { it.copy(isLoading = false, error = t.message ?: appContext.getString(R.string.prayer_fetch_failed)) }
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch { refresh() }
    }

    private fun recomputeActiveAndCountdown() {
        val now = Date()
        val timings = _state.value.timings.ifEmpty { return }

        val todayKey = dayKey()
        if (scheduleDayKey != null && scheduleDayKey != todayKey && !dayRefreshInFlight) {
            dayRefreshInFlight = true
            viewModelScope.launch {
                refresh()
                dayRefreshInFlight = false
            }
        }

        val lastPassed = timings.lastOrNull { it.date.before(now) }
        val active = lastPassed?.type ?: timings.first().type
        val theme = if (active == PrayerType.MAGHRIB || active == PrayerType.ISHA) {
            PrayerTheme.NIGHT
        } else {
            PrayerTheme.DAYLIGHT
        }

        if (lastPassed != null) {
            val elapsedMs = now.time - lastPassed.date.time
            if (elapsedMs in 0 until PRAYER_GRACE_PERIOD_MS) {
                _state.update {
                    it.copy(
                        activePrayer = active,
                        nextPrayer = null,
                        countdown = formatDurationMs(elapsedMs),
                        countdownSubtitle = appContext.getString(
                            R.string.prayer_grace_passed,
                            prayerDisplayName(lastPassed.type)
                        ),
                        isGracePeriod = true,
                        theme = theme
                    )
                }
                return
            }
        }

        val next = resolveNextPrayerEntry(timings, now)
        val deltaMs = (next.date.time - now.time).coerceAtLeast(0L)
        _state.update {
            it.copy(
                activePrayer = active,
                nextPrayer = next.type,
                countdown = formatDurationMs(deltaMs),
                countdownSubtitle = appContext.getString(
                    R.string.prayer_time_remaining,
                    prayerDisplayName(next.type)
                ),
                isGracePeriod = false,
                theme = theme
            )
        }
    }

    private fun resolveNextPrayerEntry(timings: List<PrayerEntry>, now: Date): PrayerEntry {
        timings.firstOrNull { it.date.after(now) }?.let { return it }
        val anchor = timings.firstOrNull { it.type == PrayerType.FAJR } ?: timings.first()
        val nextDay = Calendar.getInstance().apply {
            time = anchor.date
            add(Calendar.DAY_OF_YEAR, 1)
        }.time
        return anchor.copy(date = nextDay)
    }

    private fun formatDurationMs(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    private fun dayKey(): String = dayKeyFormatter.format(Date())

    private fun prayerDisplayName(type: PrayerType): String =
        AppNotificationCopy.prayerDisplayName(appContext, type.aladhanKey)

    fun formatPrayerTime(date: Date): String = timeFormatter.format(date)
}
