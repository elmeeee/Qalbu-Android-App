package app.kamy.saatApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.domain.model.OptionalWorshipHabit
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import app.kamy.saatApp.infrastructure.preferences.RamadanPreferencesStore
import app.kamy.saatApp.infrastructure.repository.KhgtCalendarRepository
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import app.kamy.saatApp.infrastructure.repository.RamadanQuoteItem
import app.kamy.saatApp.infrastructure.repository.RamadanQuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class RamadanChecklistItem {
    PUASA,
    SUBUH,
    DZUHUR,
    ASHAR,
    MAGHRIB,
    ISYA,
    TARAWEEH,
    QURAN,
    DHIKR,
    DOA,
    SEDEKAH
}

data class RamadanDayProgressItem(
    val dayNumber: Int,
    val state: RamadanPreferencesStore.FastingDayState,
    val isToday: Boolean,
    val isPassed: Boolean,
    val isFuture: Boolean
)

data class RamadanFastingStats(
    val totalFasted: Int = 0,
    val totalMissed: Int = 0,
    val totalRemaining: Int = 0,
    val currentStreak: Int = 0
)

data class RamadanDetailUiState(
    val isLoading: Boolean = false,
    val dayNumber: Int = 12,
    val totalDays: Int = 30,
    val hijriYear: Int = 1448,
    val hijriLabel: String = "12 Ramadan 1448 H",
    val imsakTime: String = "04:35",
    val subuhTime: String = "04:43",
    val maghribTime: String = "18:11",
    val isyaTime: String = "19:23",
    val countdownIftar: String = "--:--:--",
    val isFastingCountdown: Boolean = true,
    val isFastingToday: Boolean = true,
    val fastingProgressFraction: Float = 0.5f,
    val quote: RamadanQuoteItem? = null,
    val currentReadingJuz: Int = 1,
    val completedJuzCount: Int = 0,
    val lastReadVerseKey: String? = null,
    val quranTotalJuz: Int = 30,
    val isTarawihDone: Boolean = false,
    val checklistDoneMap: Map<RamadanChecklistItem, Boolean> = emptyMap(),
    val isTenLastNightsVisible: Boolean = false,
    val calendarDays: List<RamadanDayProgressItem> = emptyList(),
    val fastingStats: RamadanFastingStats = RamadanFastingStats(),
    val selectedCalendarDay: RamadanDayProgressItem? = null
)

@HiltViewModel
class RamadanDetailViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val khgtCalendar: KhgtCalendarRepository,
    private val prayerMethodStore: PrayerCalculationStore,
    private val appLanguageStore: AppLanguageStore,
    private val ramadanQuoteRepo: RamadanQuoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RamadanDetailUiState())
    val state: StateFlow<RamadanDetailUiState> = _state.asStateFlow()

    init {
        loadData()
        startCountdownTicker()

        viewModelScope.launch {
            appLanguageStore.currentFlow.collect { lang ->
                val currentDay = _state.value.dayNumber
                val quote = ramadanQuoteRepo.getQuoteForDay(currentDay, lang)
                _state.update { it.copy(quote = quote) }
            }
        }
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val nowCal = Calendar.getInstance()
            val khgtToday = runCatching { khgtCalendar.todayInfo() }.getOrNull()
            val cachedTimings = PrayerDayCache.load(appContext)?.timings.orEmpty()
            val method = prayerMethodStore.current()

            val (imsakFormatted, subuhFormatted, maghribFormatted, isyaFormatted) = extractPrayerTimings(cachedTimings)

            // Resolve dynamic day of Ramadan from KHGT calendar
            val ramadanInfo = runCatching {
                khgtCalendar.ramadanInfo(
                    date = nowCal,
                    imsakTime = imsakFormatted,
                    iftarTime = maghribFormatted
                )
            }.getOrNull()

            val localDate = nowCal.toInstant().atZone(nowCal.timeZone.toZoneId()).toLocalDate()
            val hijrah = java.time.chrono.HijrahDate.from(localDate)
            val currentDay = ramadanInfo?.dayNumber
                ?: khgtToday?.hijriLabel?.split(" ")?.firstOrNull()?.toIntOrNull()
                ?: hijrah.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            val totalDays = ramadanInfo?.totalDays
                ?: hijrah.lengthOfMonth().coerceIn(29, 30)
            val hijriYear = ramadanInfo?.hijriYear
                ?: khgtToday?.hijriLabel?.split(" ")?.getOrNull(2)?.toIntOrNull()
                ?: hijrah.get(java.time.temporal.ChronoField.YEAR_OF_ERA)

            val quote = ramadanQuoteRepo.getQuoteForDay(currentDay, appLanguageStore.current())

            // Sync with Quran Khatam & Juz tracker
            val completedJuzCount = QuranPersonalStore.readJuzs(appContext).size
            val lastJuz = QuranPersonalStore.lastReadJuz(appContext)
            val lastVerseKey = QuranPersonalStore.lastReadVerseKey(appContext)
            val currentReadingJuz = lastJuz ?: 1

            val isTarawih = RamadanPreferencesStore.isTarawihDone(appContext)
            val isFasting = RamadanPreferencesStore.isFastingDone(appContext)
            val checklistMap = buildChecklistMap()

            val isForceTenNights = RamadanPreferencesStore.isForceTenLastNightsEnabled(appContext)
            val isTenLastNightsVisible = isForceTenNights || (currentDay in 21..totalDays)

            // Build 30-Day Ramadan Calendar Grid & Stats
            val calendarDays = (1..totalDays).map { day ->
                val state = RamadanPreferencesStore.getRamadanDayFastingState(appContext, hijriYear, day, currentDay)
                RamadanDayProgressItem(
                    dayNumber = day,
                    state = state,
                    isToday = day == currentDay,
                    isPassed = day < currentDay,
                    isFuture = day > currentDay
                )
            }
            val totalFasted = calendarDays.count { it.state == RamadanPreferencesStore.FastingDayState.FASTED }
            val totalMissed = calendarDays.count { it.state == RamadanPreferencesStore.FastingDayState.NOT_FASTED }
            val totalRemaining = calendarDays.count { it.isFuture }
            var streak = 0
            for (d in currentDay downTo 1) {
                val dayItem = calendarDays.firstOrNull { it.dayNumber == d }
                if (dayItem?.state == RamadanPreferencesStore.FastingDayState.FASTED) {
                    streak++
                } else if (dayItem?.state == RamadanPreferencesStore.FastingDayState.NOT_FASTED) {
                    break
                }
            }
            val stats = RamadanFastingStats(
                totalFasted = totalFasted,
                totalMissed = totalMissed,
                totalRemaining = totalRemaining,
                currentStreak = streak
            )

            _state.update {
                it.copy(
                    dayNumber = currentDay,
                    totalDays = totalDays,
                    hijriYear = hijriYear,
                    hijriLabel = "$currentDay Ramadan $hijriYear H",
                    imsakTime = imsakFormatted,
                    subuhTime = subuhFormatted,
                    maghribTime = maghribFormatted,
                    isyaTime = isyaFormatted,
                    isFastingToday = isFasting,
                    quote = quote,
                    currentReadingJuz = currentReadingJuz,
                    completedJuzCount = completedJuzCount,
                    lastReadVerseKey = lastVerseKey,
                    isTarawihDone = isTarawih,
                    checklistDoneMap = checklistMap,
                    isTenLastNightsVisible = isTenLastNightsVisible,
                    calendarDays = calendarDays,
                    fastingStats = stats
                )
            }
            recomputeCountdown(cachedTimings)
        }
    }

    private fun extractPrayerTimings(timings: List<PrayerEntry>): Array<String> {
        val fajrDate = timings.firstOrNull { it.type == PrayerType.FAJR }?.date
        val maghribDate = timings.firstOrNull { it.type == PrayerType.MAGHRIB }?.date
        val isyaDate = timings.firstOrNull { it.type == PrayerType.ISHA }?.date
        val imsakDate = fajrDate?.let { Date(it.time - 10 * 60 * 1000L) }

        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return arrayOf(
            imsakDate?.let { format.format(it) } ?: "04:35",
            fajrDate?.let { format.format(it) } ?: "04:43",
            maghribDate?.let { format.format(it) } ?: "18:11",
            isyaDate?.let { format.format(it) } ?: "19:23"
        )
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                val cachedTimings = PrayerDayCache.load(appContext)?.timings.orEmpty()
                recomputeCountdown(cachedTimings)
                delay(1000L)
            }
        }
    }

    private fun recomputeCountdown(timings: List<PrayerEntry>) {
        val now = Date()
        val fajrDate = timings.firstOrNull { it.type == PrayerType.FAJR }?.date
        val maghribDate = timings.firstOrNull { it.type == PrayerType.MAGHRIB }?.date

        if (fajrDate == null || maghribDate == null) {
            _state.update { it.copy(countdownIftar = "08:24:12", fastingProgressFraction = 0.5f) }
            return
        }

        val imsakDate = Date(fajrDate.time - 10 * 60 * 1000L)
        val isFastingPeriod = now.time >= imsakDate.time && now.time < maghribDate.time

        if (isFastingPeriod) {
            val deltaMs = (maghribDate.time - now.time).coerceAtLeast(0L)
            val totalDuration = (maghribDate.time - imsakDate.time).coerceAtLeast(1L)
            val elapsed = (now.time - imsakDate.time).coerceAtLeast(0L)
            val fraction = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0.02f, 1f)

            _state.update {
                it.copy(
                    countdownIftar = formatDurationMs(deltaMs),
                    isFastingCountdown = true,
                    fastingProgressFraction = fraction
                )
            }
        } else {
            // Countdown until next Imsak
            val nextFajr = if (now.after(maghribDate)) {
                Calendar.getInstance().apply {
                    time = fajrDate
                    add(Calendar.DAY_OF_YEAR, 1)
                }.time
            } else {
                fajrDate
            }
            val nextImsak = Date(nextFajr.time - 10 * 60 * 1000L)
            val deltaMs = (nextImsak.time - now.time).coerceAtLeast(0L)

            _state.update {
                it.copy(
                    countdownIftar = formatDurationMs(deltaMs),
                    isFastingCountdown = false,
                    fastingProgressFraction = if (now.after(maghribDate)) 1f else 0f
                )
            }
        }
    }

    private fun formatDurationMs(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    fun toggleChecklistItem(item: RamadanChecklistItem): Boolean {
        when (item) {
            RamadanChecklistItem.PUASA -> RamadanPreferencesStore.toggleFastingDone(appContext)
            RamadanChecklistItem.SUBUH -> PrayerTrackerStore.toggle(appContext, PrayerType.FAJR)
            RamadanChecklistItem.DZUHUR -> PrayerTrackerStore.toggle(appContext, PrayerType.DHUHR)
            RamadanChecklistItem.ASHAR -> PrayerTrackerStore.toggle(appContext, PrayerType.ASR)
            RamadanChecklistItem.MAGHRIB -> PrayerTrackerStore.toggle(appContext, PrayerType.MAGHRIB)
            RamadanChecklistItem.ISYA -> PrayerTrackerStore.toggle(appContext, PrayerType.ISHA)
            RamadanChecklistItem.TARAWEEH -> RamadanPreferencesStore.toggleTarawihDone(appContext)
            RamadanChecklistItem.QURAN -> PrayerTrackerStore.toggleOptional(appContext, OptionalWorshipHabit.READ_QURAN)
            RamadanChecklistItem.DHIKR -> PrayerTrackerStore.toggleOptional(appContext, OptionalWorshipHabit.DHIKR_MORNING)
            RamadanChecklistItem.DOA -> RamadanPreferencesStore.toggleDoaDone(appContext)
            RamadanChecklistItem.SEDEKAH -> PrayerTrackerStore.toggleOptional(appContext, OptionalWorshipHabit.DAILY_CHARITY)
        }

        loadData()
        return buildChecklistMap()[item] == true
    }

    fun toggleCalendarDayFasting(dayNumber: Int): RamadanPreferencesStore.FastingDayState {
        val currentYear = _state.value.hijriYear
        val currentDay = _state.value.dayNumber
        val newState = RamadanPreferencesStore.toggleRamadanDayFasting(appContext, currentYear, dayNumber, currentDay)
        loadData()
        return newState
    }

    fun selectCalendarDay(dayNumber: Int?) {
        _state.update { curr ->
            curr.copy(selectedCalendarDay = curr.calendarDays.firstOrNull { it.dayNumber == dayNumber })
        }
    }

    fun toggleTarawihDone(): Boolean {
        return toggleChecklistItem(RamadanChecklistItem.TARAWEEH)
    }

    private fun buildChecklistMap(): Map<RamadanChecklistItem, Boolean> {
        val day = PrayerTrackerStore.todayKey()
        return mapOf(
            RamadanChecklistItem.PUASA to RamadanPreferencesStore.isFastingDone(appContext, day),
            RamadanChecklistItem.SUBUH to PrayerTrackerStore.isCompleted(appContext, PrayerType.FAJR, day),
            RamadanChecklistItem.DZUHUR to PrayerTrackerStore.isCompleted(appContext, PrayerType.DHUHR, day),
            RamadanChecklistItem.ASHAR to PrayerTrackerStore.isCompleted(appContext, PrayerType.ASR, day),
            RamadanChecklistItem.MAGHRIB to PrayerTrackerStore.isCompleted(appContext, PrayerType.MAGHRIB, day),
            RamadanChecklistItem.ISYA to PrayerTrackerStore.isCompleted(appContext, PrayerType.ISHA, day),
            RamadanChecklistItem.TARAWEEH to RamadanPreferencesStore.isTarawihDone(appContext, day),
            RamadanChecklistItem.QURAN to PrayerTrackerStore.isOptionalCompleted(appContext, OptionalWorshipHabit.READ_QURAN, day),
            RamadanChecklistItem.DHIKR to (PrayerTrackerStore.isOptionalCompleted(appContext, OptionalWorshipHabit.DHIKR_MORNING, day) || PrayerTrackerStore.isOptionalCompleted(appContext, OptionalWorshipHabit.DHIKR_EVENING, day)),
            RamadanChecklistItem.DOA to RamadanPreferencesStore.isDoaDone(appContext, day),
            RamadanChecklistItem.SEDEKAH to PrayerTrackerStore.isOptionalCompleted(appContext, OptionalWorshipHabit.DAILY_CHARITY, day)
        )
    }
}
