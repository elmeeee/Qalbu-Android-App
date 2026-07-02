package app.kamy.saatApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.error.AppErrorKind
import app.kamy.saatApp.core.error.toAppError
import app.kamy.saatApp.infrastructure.location.LocationProvider
import app.kamy.saatApp.infrastructure.preferences.LocationMode
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.saatApp.infrastructure.repository.AlAdhanRepository
import app.kamy.saatApp.infrastructure.repository.PrayerCalendarDay
import app.kamy.saatApp.infrastructure.repository.KhgtCalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class PrayerCalendarUiState(
    val isLoading: Boolean = false,
    val days: List<PrayerCalendarDay> = emptyList(),
    val loadedYear: Int? = null,
    val loadedMonth: Int? = null,
    val error: AppError? = null,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val cityName: String? = null,
    val needsLocation: Boolean = false
) {
    val daysReady: Boolean get() = loadedYear == year && loadedMonth == month
}

@HiltViewModel
class PrayerCalendarViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: AlAdhanRepository,
    private val locationProvider: LocationProvider,
    private val locationPrefs: LocationPreferencesStore,
    private val prayerMethodStore: PrayerCalculationStore,
    private val khgtCalendar: KhgtCalendarRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PrayerCalendarUiState())
    val state: StateFlow<PrayerCalendarUiState> = _state.asStateFlow()

    private var latitude: Double? = null
    private var longitude: Double? = null
    private var loadJob: Job? = null
    private var loadGeneration = 0

    init {
        loadJob = viewModelScope.launch { loadCurrentMonth() }
    }

    fun selectDay(day: Int) {
        _state.update { it.copy(selectedDay = day) }
    }

    fun shiftMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _state.value.year)
            set(Calendar.MONTH, _state.value.month - 1)
            add(Calendar.MONTH, delta)
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val today = Calendar.getInstance()
        val selectedDay = if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) + 1 == month) {
            today.get(Calendar.DAY_OF_MONTH)
        } else {
            1
        }
        loadJob?.cancel()
        _state.update {
            it.copy(
                year = year,
                month = month,
                selectedDay = selectedDay,
                days = emptyList(),
                loadedYear = null,
                loadedMonth = null,
                isLoading = true,
                error = null
            )
        }
        loadJob = viewModelScope.launch { loadMonth(year, month) }
    }

    fun retry() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadMonth(_state.value.year, _state.value.month) }
    }

    private suspend fun loadCurrentMonth() {
        val now = Calendar.getInstance()
        loadMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
    }

    private suspend fun loadMonth(year: Int, month: Int) {
        val generation = ++loadGeneration
        if (!resolveLocation()) {
            if (generation == loadGeneration) {
                _state.update { it.copy(isLoading = false) }
            }
            return
        }
        val lat = latitude ?: return
        val lon = longitude ?: return
        if (generation == loadGeneration) {
            _state.update { it.copy(isLoading = true, error = null) }
        }
        try {
            val days = repository.fetchMonthCalendar(
                year = year,
                month = month,
                latitude = lat,
                longitude = lon,
                method = prayerMethodStore.current(),
                madhab = prayerMethodStore.currentMadhab()
            )
            if (generation != loadGeneration) return
            
            // Enrich days with local KHGT calendar Hijri dates and important events
            val enrichedDays = days.map { day ->
                val dateCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, day.day)
                }
                val khgtInfo = runCatching { khgtCalendar.infoForDate(dateCal) }.getOrNull()
                day.copy(
                    hijriLabel = khgtInfo?.hijriLabel ?: day.hijriLabel,
                    khgtEventTitle = khgtInfo?.eventTitle,
                    isImportantDay = khgtInfo?.isImportantDay == true
                )
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    days = enrichedDays,
                    loadedYear = year,
                    loadedMonth = month,
                    error = null,
                    selectedDay = it.selectedDay.coerceIn(1, enrichedDays.size.coerceAtLeast(1))
                )
            }
        } catch (t: Throwable) {
            if (generation != loadGeneration) return
            _state.update { it.copy(isLoading = false, error = t.toAppError()) }
        }
    }

    private suspend fun resolveLocation(): Boolean {
        if (latitude != null && longitude != null) {
            return true
        }
        if (locationPrefs.mode() == LocationMode.MANUAL) {
            val manual = locationPrefs.manualLocation()
            if (manual == null) {
                _state.update { it.copy(needsLocation = true, error = AppError(AppErrorKind.Location)) }
                return false
            }
            latitude = manual.latitude
            longitude = manual.longitude
            _state.update { it.copy(cityName = manual.label, needsLocation = false) }
            return true
        }
        val cachedGps = locationPrefs.gpsLocation()
        if (cachedGps != null) {
            latitude = cachedGps.latitude
            longitude = cachedGps.longitude
            _state.update { it.copy(cityName = cachedGps.label, needsLocation = false) }
            return true
        }
        if (!locationProvider.hasAnyPermission()) {
            _state.update { it.copy(needsLocation = true, error = AppError(AppErrorKind.Location)) }
            return false
        }
        val loc = locationProvider.currentLocation()
        if (loc == null) {
            _state.update { it.copy(needsLocation = true, error = AppError(AppErrorKind.Location)) }
            return false
        }
        latitude = loc.latitude
        longitude = loc.longitude
        val geocode = locationProvider.reverseGeocode(loc.latitude, loc.longitude)
        val label = geocode.cityName
            ?: locationProvider.coordinateLabel(loc.latitude, loc.longitude)
        locationPrefs.saveGpsLocation(loc.latitude, loc.longitude, label)
        _state.update { it.copy(cityName = label, needsLocation = false) }
        return true
    }
}
