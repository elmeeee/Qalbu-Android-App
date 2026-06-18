package app.kamy.qalbuApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import app.kamy.qalbuApp.domain.model.OptionalWorshipHabit
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.domain.prayer.HijriDayHelper
import app.kamy.qalbuApp.domain.prayer.PrayerTrackerAvailability
import app.kamy.qalbuApp.infrastructure.notifications.PrayerCheckReminderScheduler
import app.kamy.qalbuApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.qalbuApp.infrastructure.preferences.PrayerTrackerPreferencesStore
import app.kamy.qalbuApp.infrastructure.preferences.PrayerTrackerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val availablePrayers: Set<PrayerType> = emptySet(),
    val checkRemindersEnabled: Boolean = true
)

@HiltViewModel
class PrayerTrackerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val prefs = PrayerTrackerPreferencesStore.from(appContext)

    private val _state = MutableStateFlow(PrayerTrackerUiState())
    val state: StateFlow<PrayerTrackerUiState> = _state.asStateFlow()

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
                availablePrayers = available,
                checkRemindersEnabled = prefs.checkRemindersEnabled()
            )
        }
    }

    fun togglePrayer(prayer: PrayerType) {
        val completed = _state.value.completedPrayers.contains(prayer)
        if (!PrayerTrackerAvailability.canToggle(appContext, prayer, completed)) return
        PrayerTrackerStore.toggle(appContext, prayer)
        if (PrayerTrackerStore.isCompleted(appContext, prayer)) {
            PrayerCheckReminderScheduler.onPrayerMarked(appContext, prayer)
        } else {
            PrayerCheckReminderScheduler.reschedule(appContext)
        }
        refresh()
    }

    fun toggleOptionalHabit(habit: OptionalWorshipHabit) {
        PrayerTrackerStore.toggleOptional(appContext, habit)
        refresh()
    }

    fun setCheckRemindersEnabled(enabled: Boolean) {
        prefs.setCheckRemindersEnabled(enabled)
        if (enabled) {
            PrayerCheckReminderScheduler.reschedule(appContext)
        } else {
            PrayerCheckReminderScheduler.cancelAll(appContext)
        }
        refresh()
    }

    fun isCompleted(prayer: PrayerType): Boolean =
        _state.value.completedPrayers.contains(prayer)

    private fun buildOptionalHabits(today: String): List<OptionalHabitUiItem> {
        val hijriDay = HijriDayHelper.todayHijriDay(appContext)
            ?: HijriDayHelper.parseHijriDayFromLabel(
                app.kamy.qalbuApp.infrastructure.cache.PrayerDayCache.load(appContext)?.hijriLabel
            )
        val monThu = HijriDayHelper.isMondayThursdayFastDay()
        val ayyamulBidh = HijriDayHelper.isAyyamulBidh(hijriDay)

        return OptionalWorshipHabit.ALL.mapNotNull { habit ->
            if (!prefs.isOptionalHabitEnabled(habit)) return@mapNotNull null
            val applicable = when (habit) {
                OptionalWorshipHabit.QIYAMUL_LAIL -> true
                OptionalWorshipHabit.MONDAY_THURSDAY_FAST -> monThu
                OptionalWorshipHabit.AYYAMUL_BIDH_SAHUR -> ayyamulBidh
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
        OptionalWorshipHabit.QIYAMUL_LAIL -> app.kamy.qalbuApp.R.string.optional_qiyam
        OptionalWorshipHabit.MONDAY_THURSDAY_FAST -> app.kamy.qalbuApp.R.string.optional_mon_thu_fast
        OptionalWorshipHabit.AYYAMUL_BIDH_SAHUR -> app.kamy.qalbuApp.R.string.optional_ayyamul_bidh
    }
}
