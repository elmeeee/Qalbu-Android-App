package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.model.OptionalWorshipHabit
import app.kamy.saatApp.domain.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PrayerDayProgress(
    val dayKey: String,
    val completedCount: Int,
    val totalCount: Int = PrayerType.ADZAN_NOTIFICATION_PRAYERS.size,
    val optionalCompletedCount: Int = 0,
    val optionalTotalCount: Int = 0
) {
    val fraction: Float get() = completedCount.toFloat() / totalCount.coerceAtLeast(1)
    val isPerfectDay: Boolean get() = completedCount >= totalCount
}

object PrayerTrackerStore {
    private const val PREFS = "saat_prayer_tracker"
    private const val KEY_BEST_STREAK = "best_streak"
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val TRACKED_PRAYERS = PrayerType.ADZAN_NOTIFICATION_PRAYERS

    fun todayKey(): String = dayKeyFormat.format(Date())

    fun dayKeyFor(calendar: Calendar): String = dayKeyFormat.format(calendar.time)

    fun isCompleted(context: Context, prayer: PrayerType, dayKey: String = todayKey()): Boolean {
        val key = prefKey(prayer, dayKey)
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(key, false)
    }

    fun setCompleted(
        context: Context,
        prayer: PrayerType,
        completed: Boolean,
        dayKey: String = todayKey()
    ) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(prefKey(prayer, dayKey), completed)
            .apply()
        updateBestStreakIfNeeded(context)
    }

    fun toggle(context: Context, prayer: PrayerType, dayKey: String = todayKey()): Boolean {
        val next = !isCompleted(context, prayer, dayKey)
        setCompleted(context, prayer, next, dayKey)
        return next
    }

    fun isOptionalCompleted(
        context: Context,
        habit: OptionalWorshipHabit,
        dayKey: String = todayKey()
    ): Boolean {
        if (habit == OptionalWorshipHabit.QIYAMUL_LAIL) {
            if (QiyamTrackerStore.isLogged(context, dayKey)) return true
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val legacyKey = optionalPrefKey(habit, dayKey)
            if (prefs.getBoolean(legacyKey, false)) {
                QiyamTrackerStore.setLogged(context, true, dayKey)
                prefs.edit().remove(legacyKey).apply()
                return true
            }
            return false
        }
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(optionalPrefKey(habit, dayKey), false)
    }

    fun setOptionalCompleted(
        context: Context,
        habit: OptionalWorshipHabit,
        completed: Boolean,
        dayKey: String = todayKey()
    ) {
        if (habit == OptionalWorshipHabit.QIYAMUL_LAIL) {
            QiyamTrackerStore.setLogged(context, completed, dayKey)
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(optionalPrefKey(habit, dayKey))
                .apply()
            return
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(optionalPrefKey(habit, dayKey), completed)
            .apply()
    }

    fun toggleOptional(
        context: Context,
        habit: OptionalWorshipHabit,
        dayKey: String = todayKey()
    ): Boolean {
        val next = !isOptionalCompleted(context, habit, dayKey)
        setOptionalCompleted(context, habit, next, dayKey)
        return next
    }

    fun completedCount(context: Context, dayKey: String = todayKey()): Int =
        TRACKED_PRAYERS.count { isCompleted(context, it, dayKey) }

    fun dayProgress(context: Context, dayKey: String = todayKey()): PrayerDayProgress =
        PrayerDayProgress(
            dayKey = dayKey,
            completedCount = completedCount(context, dayKey)
        )

    fun weekProgress(context: Context): List<PrayerDayProgress> {
        val cal = Calendar.getInstance()
        return (6 downTo 0).map { offset ->
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            dayProgress(context, dayKeyFormat.format(cal.time))
        }
    }

    fun monthProgress(context: Context, year: Int, month: Int): List<PrayerDayProgress> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).map { day ->
            cal.set(Calendar.DAY_OF_MONTH, day)
            dayProgress(context, dayKeyFormat.format(cal.time))
        }
    }

    fun currentStreak(context: Context): Int {
        val cal = Calendar.getInstance()
        var streak = 0
        repeat(400) {
            val key = dayKeyFormat.format(cal.time)
            val count = completedCount(context, key)
            if (count >= TRACKED_PRAYERS.size) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else if (streak == 0 && key == todayKey() && count > 0) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                return streak
            }
        }
        return streak
    }

    fun bestStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_BEST_STREAK, 0).coerceAtLeast(currentStreak(context))
    }

    fun challengeTargetDays(streak: Int): Int = when {
        streak < 7 -> 7
        streak < 30 -> 30
        streak < 40 -> 40
        else -> ((streak / 10) + 1) * 10
    }

    private fun updateBestStreakIfNeeded(context: Context) {
        val current = currentStreak(context)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val best = prefs.getInt(KEY_BEST_STREAK, 0)
        if (current > best) {
            prefs.edit().putInt(KEY_BEST_STREAK, current).apply()
        }
    }

    private fun prefKey(prayer: PrayerType, dayKey: String): String =
        "${dayKey}_${prayer.name.lowercase()}"

    private fun optionalPrefKey(habit: OptionalWorshipHabit, dayKey: String): String =
        "${dayKey}_opt_${habit.prefKey}"
}
