package app.kamy.saatApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.domain.model.OptionalWorshipHabit
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.prayer.HijriDayHelper
import app.kamy.saatApp.domain.prayer.PrayerTrackerAvailability
import app.kamy.saatApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.saatApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class OptionalHabitUiItem(
    val habit: OptionalWorshipHabit,
    val labelRes: Int,
    val completed: Boolean,
    val applicableToday: Boolean
)

data class PrayerTrackerUiState(
    val todayProgress: PrayerDayProgress = PrayerDayProgress(PrayerTrackerStore.todayKey(), 0),
    val weekProgress: List<PrayerDayProgress> = emptyList(),
    val monthPreview: List<PrayerDayProgress> = emptyList(),
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val challengeTarget: Int = 7,
    val completedPrayers: Set<PrayerType> = emptySet(),
    val optionalHabits: List<OptionalHabitUiItem> = emptyList(),
    val availablePrayers: Set<PrayerType> = emptySet()
)

@HiltViewModel
class PrayerTrackerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val prefs = PrayerTrackerPreferencesStore.from(appContext)

    private val _state = MutableStateFlow(PrayerTrackerUiState())
    val state: StateFlow<PrayerTrackerUiState> = _state.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        val today = PrayerTrackerStore.todayKey()
        val completed = PrayerTrackerStore.TRACKED_PRAYERS
            .filter { PrayerTrackerStore.isCompleted(appContext, it, today) }
            .toSet()
        val streak = PrayerTrackerStore.currentStreak(appContext)
        val cal = Calendar.getInstance()
        val available = PrayerTrackerAvailability.availablePrayers(appContext)
        _state.update {
            it.copy(
                todayProgress = PrayerTrackerStore.dayProgress(appContext, today),
                weekProgress = PrayerTrackerStore.weekProgress(appContext),
                monthPreview = PrayerTrackerStore.monthProgress(
                    appContext,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1
                ),
                streak = streak,
                bestStreak = PrayerTrackerStore.bestStreak(appContext),
                challengeTarget = PrayerTrackerStore.challengeTargetDays(streak),
                completedPrayers = completed,
                optionalHabits = buildOptionalHabits(today),
                availablePrayers = available
            )
        }
    }

    fun togglePrayer(prayer: PrayerType) {
        val completed = _state.value.completedPrayers.contains(prayer)
        if (!PrayerTrackerAvailability.canToggle(appContext, prayer, completed)) return
        PrayerTrackerStore.toggle(appContext, prayer)
        val nowCompleted = PrayerTrackerStore.isCompleted(appContext, prayer)
        if (nowCompleted) {
            val label = AppNotificationCopy.prayerDisplayName(appContext, prayer.aladhanKey)
            viewModelScope.launch {
                _toastMessage.emit(
                    appContext.getString(R.string.prayer_marked_success, label)
                )
            }
        }
        refresh()
    }

    fun toggleDailyPrayer() {
        val today = PrayerTrackerStore.todayKey()
        val current = PrayerTrackerStore.isDailyPrayerCompleted(appContext, today)
        PrayerTrackerStore.setDailyPrayerCompleted(appContext, !current, today)
        refresh()
    }

    fun toggleOptionalHabit(habit: OptionalWorshipHabit) {
        PrayerTrackerStore.toggleOptional(appContext, habit)
        refresh()
    }

    fun isCompleted(prayer: PrayerType): Boolean =
        _state.value.completedPrayers.contains(prayer)

    private fun buildOptionalHabits(today: String): List<OptionalHabitUiItem> {
        val hijriDay = HijriDayHelper.todayHijriDay(appContext)
            ?: HijriDayHelper.parseHijriDayFromLabel(
                app.kamy.saatApp.infrastructure.cache.PrayerDayCache.load(appContext)?.hijriLabel
            )
        val monThu = HijriDayHelper.isMondayThursdayFastDay()
        val ayyamulBidh = HijriDayHelper.isAyyamulBidh(hijriDay)

        return OptionalWorshipHabit.ALL.mapNotNull { habit ->
            if (!prefs.isOptionalHabitEnabled(habit)) return@mapNotNull null
            val applicable = when (habit) {
                OptionalWorshipHabit.QIYAMUL_LAIL -> true
                OptionalWorshipHabit.MONDAY_THURSDAY_FAST -> monThu
                OptionalWorshipHabit.AYYAMUL_BIDH_SAHUR -> ayyamulBidh
                OptionalWorshipHabit.DHIKR_MORNING -> true
                OptionalWorshipHabit.DHIKR_EVENING -> true
                OptionalWorshipHabit.READ_QURAN -> true
                OptionalWorshipHabit.DAILY_CHARITY -> true
                OptionalWorshipHabit.DHUHA -> true
                OptionalWorshipHabit.RAWATIB -> true
            }
            if (!applicable) return@mapNotNull null
            OptionalHabitUiItem(
                habit = habit,
                labelRes = habit.labelRes(),
                completed = PrayerTrackerStore.isOptionalCompleted(appContext, habit, today),
                applicableToday = applicable
            )
        }
    }

    private fun OptionalWorshipHabit.labelRes(): Int = when (this) {
        OptionalWorshipHabit.QIYAMUL_LAIL -> app.kamy.saatApp.R.string.optional_qiyam
        OptionalWorshipHabit.MONDAY_THURSDAY_FAST -> app.kamy.saatApp.R.string.optional_mon_thu_fast
        OptionalWorshipHabit.AYYAMUL_BIDH_SAHUR -> app.kamy.saatApp.R.string.optional_ayyamul_bidh
        OptionalWorshipHabit.DHIKR_MORNING -> app.kamy.saatApp.R.string.optional_dhikr_morning
        OptionalWorshipHabit.DHIKR_EVENING -> app.kamy.saatApp.R.string.optional_dhikr_evening
        OptionalWorshipHabit.READ_QURAN -> app.kamy.saatApp.R.string.optional_read_quran
        OptionalWorshipHabit.DAILY_CHARITY -> app.kamy.saatApp.R.string.optional_daily_charity
        OptionalWorshipHabit.DHUHA -> app.kamy.saatApp.R.string.optional_dhuha
        OptionalWorshipHabit.RAWATIB -> app.kamy.saatApp.R.string.optional_rawatib
    }
}
