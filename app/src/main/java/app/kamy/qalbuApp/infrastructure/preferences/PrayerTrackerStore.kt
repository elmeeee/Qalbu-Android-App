package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.domain.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PrayerDayProgress(
    val dayKey: String,
    val completedCount: Int,
    val totalCount: Int = PrayerType.ADZAN_NOTIFICATION_PRAYERS.size
) {
    val fraction: Float get() = completedCount.toFloat() / totalCount.coerceAtLeast(1)
}

object PrayerTrackerStore {
    private const val PREFS = "qalbu_prayer_tracker"
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val TRACKED_PRAYERS = PrayerType.ADZAN_NOTIFICATION_PRAYERS

    fun todayKey(): String = dayKeyFormat.format(Date())

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
    }

    fun toggle(context: Context, prayer: PrayerType, dayKey: String = todayKey()): Boolean {
        val next = !isCompleted(context, prayer, dayKey)
        setCompleted(context, prayer, next, dayKey)
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
            val key = dayKeyFormat.format(cal.time)
            dayProgress(context, key)
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
                // Today in progress — keep checking from yesterday
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                return streak
            }
        }
        return streak
    }

    private fun prefKey(prayer: PrayerType, dayKey: String): String =
        "${dayKey}_${prayer.name.lowercase()}"
}
