package app.kamy.saatApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.NightWorshipItem
import app.kamy.saatApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.saatApp.infrastructure.preferences.RamadanPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.TenLastNightsStore
import app.kamy.saatApp.infrastructure.repository.KhgtCalendarRepository
import app.kamy.saatApp.infrastructure.repository.KimiNightQuoteItem
import app.kamy.saatApp.infrastructure.repository.LailatulQadrGuideItem
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import app.kamy.saatApp.infrastructure.repository.TenLastNightsDuaItem
import app.kamy.saatApp.infrastructure.repository.TenLastNightsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

data class TenLastNightsUiState(
    val isLoading: Boolean = false,
    val selectedNight: Int = 21,
    val currentNightNumber: Int = 21,
    val isRamadanActive: Boolean = true,
    val isyaTime: String = "19:23",
    val midnightTime: String = "00:00",
    val lastThirdTime: String = "02:45",
    val subuhTime: String = "04:43",
    val quote: KimiNightQuoteItem = KimiNightQuoteItem(
        night = 21,
        quote = "Pelan-pelan saja. Satu malam, satu langkah lebih dekat.",
        description = "Mari isi malam ini dengan ibadah dan doa. Setiap amal kecil sangat berarti di sisi Allah."
    ),
    val worshipDoneMap: Map<NightWorshipItem, Boolean> = emptyMap(),
    val guide: LailatulQadrGuideItem = LailatulQadrGuideItem("", "", emptyList()),
    val duas: List<TenLastNightsDuaItem> = emptyList()
)

@HiltViewModel
class TenLastNightsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: TenLastNightsRepository,
    private val appLanguageStore: AppLanguageStore,
    private val khgtCalendar: KhgtCalendarRepository,
    private val prayerMethodStore: PrayerCalculationStore
) : ViewModel() {

    private val _state = MutableStateFlow(TenLastNightsUiState())
    val state: StateFlow<TenLastNightsUiState> = _state.asStateFlow()

    init {
        loadData()

        viewModelScope.launch {
            appLanguageStore.currentFlow.collect { lang ->
                val night = _state.value.selectedNight
                val quote = repository.getQuoteForNight(night, lang)
                val guide = repository.getLailatulQadrGuide(lang)
                val duas = repository.getDuas(lang)
                _state.update {
                    it.copy(
                        quote = quote,
                        guide = guide,
                        duas = duas
                    )
                }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            val language = appLanguageStore.current()
            val nowCal = Calendar.getInstance()
            val khgtToday = runCatching { khgtCalendar.todayInfo() }.getOrNull()
            val cachedTimings = PrayerDayCache.load(appContext)?.timings.orEmpty()
            val method = prayerMethodStore.current()

            val timingsSchedule = extractNightPrayerSchedule(cachedTimings)

            val ramadanInfo = if (method == PrayerCalculationMethod.MUHAMMADIYAH) {
                runCatching {
                    khgtCalendar.ramadanInfo(
                        date = nowCal,
                        imsakTime = timingsSchedule.subuh,
                        iftarTime = timingsSchedule.isya
                    )
                }.getOrNull()
            } else {
                null
            }

            val localDate = nowCal.toInstant().atZone(nowCal.timeZone.toZoneId()).toLocalDate()
            val hijrah = java.time.chrono.HijrahDate.from(localDate)
            val currentDay = ramadanInfo?.dayNumber
                ?: khgtToday?.hijriLabel?.split(" ")?.firstOrNull()?.toIntOrNull()
                ?: hijrah.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

            val defaultSelectedNight = currentDay.coerceIn(21, 30)

            val quote = repository.getQuoteForNight(defaultSelectedNight, language)
            val guide = repository.getLailatulQadrGuide(language)
            val duas = repository.getDuas(language)
            val worshipMap = TenLastNightsStore.getWorshipMap(appContext, defaultSelectedNight)

            _state.update {
                it.copy(
                    selectedNight = defaultSelectedNight,
                    currentNightNumber = defaultSelectedNight,
                    isRamadanActive = true,
                    isyaTime = timingsSchedule.isya,
                    midnightTime = timingsSchedule.midnight,
                    lastThirdTime = timingsSchedule.lastThird,
                    subuhTime = timingsSchedule.subuh,
                    quote = quote,
                    worshipDoneMap = worshipMap,
                    guide = guide,
                    duas = duas
                )
            }
        }
    }

    fun selectNight(nightNumber: Int) {
        val effectiveNight = nightNumber.coerceIn(21, 30)
        val language = appLanguageStore.current()
        val quote = repository.getQuoteForNight(effectiveNight, language)
        val worshipMap = TenLastNightsStore.getWorshipMap(appContext, effectiveNight)

        _state.update {
            it.copy(
                selectedNight = effectiveNight,
                quote = quote,
                worshipDoneMap = worshipMap
            )
        }
    }

    fun toggleWorship(item: NightWorshipItem): Boolean {
        val selectedNight = _state.value.selectedNight
        val next = TenLastNightsStore.toggleWorshipDone(appContext, selectedNight, item)
        val updatedMap = TenLastNightsStore.getWorshipMap(appContext, selectedNight)
        _state.update {
            it.copy(worshipDoneMap = updatedMap)
        }
        return next
    }

    private fun extractNightPrayerSchedule(timings: List<PrayerEntry>): NightScheduleTimes {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val isyaDate = timings.firstOrNull { it.type == PrayerType.ISHA }?.date
        val subuhDate = timings.firstOrNull { it.type == PrayerType.FAJR }?.date
        val maghribDate = timings.firstOrNull { it.type == PrayerType.MAGHRIB }?.date

        val isyaStr = isyaDate?.let { format.format(it) } ?: "19:23"
        val subuhStr = subuhDate?.let { format.format(it) } ?: "04:43"

        var midnightStr = "00:00"
        var lastThirdStr = "02:45"

        if (maghribDate != null && subuhDate != null) {
            val maghribMillis = maghribDate.time
            val nextSubuhMillis = if (subuhDate.time <= maghribDate.time) {
                subuhDate.time + 24 * 60 * 60 * 1000L
            } else {
                subuhDate.time
            }
            val nightDuration = (nextSubuhMillis - maghribMillis).coerceAtLeast(1000L)
            val midnightDate = Date(maghribMillis + nightDuration / 2)
            val lastThirdDate = Date(nextSubuhMillis - nightDuration / 3)

            midnightStr = format.format(midnightDate)
            lastThirdStr = format.format(lastThirdDate)
        }

        return NightScheduleTimes(
            isya = isyaStr,
            midnight = midnightStr,
            lastThird = lastThirdStr,
            subuh = subuhStr
        )
    }

    private data class NightScheduleTimes(
        val isya: String,
        val midnight: String,
        val lastThird: String,
        val subuh: String
    )
}
