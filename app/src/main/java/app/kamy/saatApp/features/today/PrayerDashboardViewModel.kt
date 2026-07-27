package app.kamy.saatApp.features.today

import android.content.Context
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.R
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.error.AppErrorKind
import app.kamy.saatApp.core.error.toAppError
import app.kamy.saatApp.core.locale.AppStrings
import app.kamy.saatApp.domain.model.KhgtTodayInfo
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.infrastructure.location.LocationProvider
import app.kamy.saatApp.infrastructure.notifications.PrayerNotificationCoordinator
import app.kamy.saatApp.infrastructure.preferences.LocationMode
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.SavedManualLocation
import app.kamy.saatApp.infrastructure.repository.AlAdhanRepository
import app.kamy.saatApp.infrastructure.repository.KhgtCalendarRepository
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleCache
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import app.kamy.saatApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.saatApp.infrastructure.preferences.KhgtWidgetCache
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val isManualLocation: Boolean = false,
    val error: AppError? = null,
    val showLocationSheet: Boolean = false,
    val locationQuery: String = "",
    val locationSaving: Boolean = false,
    val locationSaveError: String? = null,
    val isOfflineData: Boolean = false,
    val khgtToday: KhgtTodayInfo? = null,
    val prayerLastSyncAt: Long? = null
)

private data class ResolvedPrayerLocation(
    val latitude: Double,
    val longitude: Double,
    val cityLabel: String,
    val countryCode: String?,
    val isManual: Boolean
)

@HiltViewModel
class PrayerDashboardViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val strings: AppStrings,
    private val repository: AlAdhanRepository,
    private val khgtCalendar: KhgtCalendarRepository,
    private val locationProvider: LocationProvider,
    private val locationPrefs: LocationPreferencesStore,
    private val prayerMethodStore: PrayerCalculationStore,
    private val prayerNotificationPrefs: PrayerNotificationPreferencesStore
) : ViewModel() {

    private val _state = MutableStateFlow(
        PrayerUiState(countdownSubtitle = strings.getString(R.string.prayer_schedule))
    )
    val state: StateFlow<PrayerUiState> = _state.asStateFlow()

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)
    private val dayKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var scheduleDayKey: String? = null
    private var dayRefreshInFlight = false
    private var lastResolvedLocation: ResolvedPrayerLocation? = null
    private val refreshMutex = Mutex()
    private var lastRefreshAtMs: Long = 0L

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
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                recomputeActiveAndCountdown()
            }
        }
    }

    suspend fun refresh(force: Boolean = false) {
        if (!force) {
            val now = System.currentTimeMillis()
            if (now - lastRefreshAtMs < REFRESH_COALESCE_MS) return
        }
        refreshMutex.withLock {
            if (!force) {
                val now = System.currentTimeMillis()
                if (now - lastRefreshAtMs < REFRESH_COALESCE_MS) return
                lastRefreshAtMs = now
            } else {
                lastRefreshAtMs = System.currentTimeMillis()
            }
            refreshInternal()
        }
    }

    private suspend fun refreshInternal() {
        if (_state.value.timings.isEmpty()) {
            _state.update { it.copy(isLoading = true, error = null) }
        }
        when (val resolved = resolveLocation()) {
            LocationResolveResult.NeedsPermission -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        needsPermission = true,
                        isManualLocation = false
                    )
                }
            }
            LocationResolveResult.Unavailable -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = AppError(AppErrorKind.Location),
                        needsPermission = false
                    )
                }
            }
            is LocationResolveResult.Success -> {
                lastResolvedLocation = resolved.location
                fetchTimings(resolved.location)
            }
        }
    }

    private suspend fun fetchTimings(location: ResolvedPrayerLocation) {
        if (!prayerMethodStore.hasSavedPreference) {
            location.countryCode?.let { code ->
                prayerMethodStore.setMethod(PrayerCalculationMethod.forCountryCode(code))
            }
        }
        val calculationMethod = prayerMethodStore.current()
        val madhab = prayerMethodStore.currentMadhab()
        try {
            val result = repository.fetchTimings(
                latitude = location.latitude,
                longitude = location.longitude,
                cityName = location.cityLabel,
                method = calculationMethod,
                madhab = madhab
            )
            val cityName = result.cityName ?: location.cityLabel
            locationPrefs.saveActiveLabel(cityName)
            val khgt = runCatching { khgtCalendar.todayInfo() }.getOrNull()
            val hijriLabel = khgt?.hijriLabel ?: result.hijriLabel
            val gregorianLabel = khgt?.gregorianLabel ?: result.gregorianLabel
            khgt?.let {
                KhgtWidgetCache.save(
                    appContext,
                    hijriLabel = it.hijriLabel,
                    pasaran = it.pasaran,
                    eventTitle = it.eventTitle
                )
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    timings = result.timings,
                    cityName = cityName,
                    hijriLabel = hijriLabel,
                    gregorianLabel = gregorianLabel,
                    needsPermission = false,
                    isManualLocation = location.isManual,
                    error = null,
                    isOfflineData = true,
                    khgtToday = khgt,
                    prayerLastSyncAt = System.currentTimeMillis()
                )
            }
            PrayerDayCache.save(appContext, result)
            scheduleDayKey = dayKey()
            recomputeActiveAndCountdown()
            result.scheduleBundle?.let { bundle ->
                PrayerNotificationCoordinator.onScheduleUpdated(
                    appContext,
                    bundle,
                    location.latitude,
                    location.longitude,
                    meta = buildWidgetMeta(
                        cityName = cityName,
                        hijriLabel = hijriLabel,
                        gregorianLabel = gregorianLabel,
                        timings = result.timings
                    )
                )
            }
        } catch (t: Throwable) {
            val cached = PrayerDayCache.load(appContext)
            if (cached != null && cached.timings.isNotEmpty()) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        timings = cached.timings,
                        cityName = cached.cityName ?: location.cityLabel,
                        hijriLabel = cached.hijriLabel,
                        gregorianLabel = cached.gregorianLabel,
                        needsPermission = false,
                        isManualLocation = location.isManual,
                        error = null,
                        isOfflineData = true,
                        khgtToday = runCatching { khgtCalendar.todayInfo() }.getOrNull(),
                        prayerLastSyncAt = PrayerDayCache.lastSavedAt(appContext)
                    )
                }
                scheduleDayKey = dayKey()
                recomputeActiveAndCountdown()
            } else {
                _state.update { it.copy(isLoading = false, error = t.toAppError(), isOfflineData = false) }
            }
        }
    }

    private suspend fun resolveLocation(): LocationResolveResult {
        if (locationPrefs.mode() == LocationMode.MANUAL) {
            val manual = locationPrefs.manualLocation()
                ?: return LocationResolveResult.Unavailable
            return LocationResolveResult.Success(
                ResolvedPrayerLocation(
                    latitude = manual.latitude,
                    longitude = manual.longitude,
                    cityLabel = manual.label,
                    countryCode = manual.countryCode,
                    isManual = true
                )
            )
        }
        if (!locationProvider.hasAnyPermission()) {
            return LocationResolveResult.NeedsPermission
        }
        val loc = locationProvider.currentLocation() ?: run {
            val cachedGps = locationPrefs.gpsLocation()
            if (cachedGps != null) {
                return LocationResolveResult.Success(
                    ResolvedPrayerLocation(
                        latitude = cachedGps.latitude,
                        longitude = cachedGps.longitude,
                        cityLabel = cachedGps.label,
                        countryCode = null,
                        isManual = false
                    )
                )
            }
            return LocationResolveResult.Unavailable
        }
        val geocode = locationProvider.reverseGeocode(loc.latitude, loc.longitude)
        val cityLabel = geocode.cityName
            ?: locationPrefs.gpsLocation()?.label
            ?: locationProvider.coordinateLabel(loc.latitude, loc.longitude)
        if (geocode.cityName != null) {
            locationPrefs.saveGpsLocation(loc.latitude, loc.longitude, cityLabel)
        }
        return LocationResolveResult.Success(
            ResolvedPrayerLocation(
                latitude = loc.latitude,
                longitude = loc.longitude,
                cityLabel = cityLabel,
                countryCode = geocode.countryCode,
                isManual = false
            )
        )
    }

    fun onPermissionGranted() {
        locationPrefs.setMode(LocationMode.GPS)
        if (_state.value.timings.isNotEmpty() && !_state.value.needsPermission) return
        viewModelScope.launch { refresh() }
    }

    fun openLocationSheet() {
        _state.update {
            it.copy(
                showLocationSheet = true,
                locationQuery = it.cityName.orEmpty(),
                locationSaveError = null
            )
        }
    }

    fun dismissLocationSheet() {
        _state.update { it.copy(showLocationSheet = false, locationSaveError = null) }
    }

    fun updateLocationQuery(query: String) {
        _state.update { it.copy(locationQuery = query, locationSaveError = null) }
    }

    fun saveManualLocation() {
        val query = _state.value.locationQuery.trim()
        if (query.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(locationSaving = true, locationSaveError = null) }
            val geocoded = locationProvider.forwardGeocode(query)
            if (geocoded?.latitude == null || geocoded.longitude == null) {
                _state.update {
                    it.copy(
                        locationSaving = false,
                        locationSaveError = strings.getString(R.string.location_search_not_found)
                    )
                }
                return@launch
            }
            val saved = SavedManualLocation(
                latitude = geocoded.latitude,
                longitude = geocoded.longitude,
                label = geocoded.cityName ?: query,
                countryCode = geocoded.countryCode
            )
            locationPrefs.saveManual(saved)
            _state.update {
                it.copy(
                    locationSaving = false,
                    showLocationSheet = false,
                    needsPermission = false
                )
            }
            refresh()
        }
    }

    fun useCurrentLocation() {
        locationPrefs.setMode(LocationMode.GPS)
        _state.update { it.copy(showLocationSheet = false, locationSaveError = null) }
        viewModelScope.launch { refresh() }
    }

    private fun buildWidgetMeta(
        cityName: String,
        hijriLabel: String?,
        gregorianLabel: String?,
        timings: List<PrayerEntry>
    ): PrayerScheduleCache.WidgetMeta {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return PrayerScheduleCache.WidgetMeta(
            cityLabel = cityName,
            hijriLabel = hijriLabel,
            gregorianLabel = gregorianLabel,
            timings = timings.associate { it.type.aladhanKey to formatter.format(it.date) }
        )
    }

    private sealed class LocationResolveResult {
        data object NeedsPermission : LocationResolveResult()
        data object Unavailable : LocationResolveResult()
        data class Success(val location: ResolvedPrayerLocation) : LocationResolveResult()
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

        val lastPassed = timings.lastOrNull { it.date.before(now) && it.type != PrayerType.SUNRISE }
        val active = lastPassed?.type ?: timings.first { it.type != PrayerType.SUNRISE }.type
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
                        countdownSubtitle = strings.getString(
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
                countdownSubtitle = strings.getString(
                    R.string.prayer_time_remaining,
                    prayerDisplayName(next.type)
                ),
                isGracePeriod = false,
                theme = theme
            )
        }
    }

    private fun resolveNextPrayerEntry(timings: List<PrayerEntry>, now: Date): PrayerEntry {
        // Skip SUNRISE — it is not a salah, so the next prayer after Fajr should be Dhuhr.
        timings.firstOrNull { it.date.after(now) && it.type != PrayerType.SUNRISE }?.let { return it }
        val anchor = timings.firstOrNull { it.type == PrayerType.FAJR } ?: timings.first { it.type != PrayerType.SUNRISE }
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

    private fun prayerDisplayName(type: PrayerType): String = when (type) {
        PrayerType.FAJR -> strings.getString(R.string.prayer_fajr)
        PrayerType.SUNRISE -> strings.getString(R.string.prayer_sunrise)
        PrayerType.DHUHR -> strings.getString(R.string.prayer_dhuhr)
        PrayerType.ASR -> strings.getString(R.string.prayer_asr)
        PrayerType.MAGHRIB -> strings.getString(R.string.prayer_maghrib)
        PrayerType.ISHA -> strings.getString(R.string.prayer_isha)
    }

    fun formatPrayerTime(date: Date): String {
        val is24Hour = DateFormat.is24HourFormat(appContext)
        val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    private companion object {
        private const val REFRESH_COALESCE_MS = 8_000L
    }
}
