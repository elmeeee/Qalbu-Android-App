package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class QiyamMonthSnapshot(
    val nightsThisMonth: Int,
    val nightsLast7Days: Int,
    val streak: Int,
    val isLoggedTonight: Boolean
)

object QiyamTrackerStore {
    private const val PREFS = "saat_qiyam_tracker"
    private const val KEY_NIGHTS_PREFIX = "night_"
    private const val KEY_STREAK = "streak"
    private const val KEY_LAST_NIGHT = "last_night"
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun todayKey(): String = dayKeyFormat.format(Date())

    fun isLogged(context: Context, dayKey: String = todayKey()): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_NIGHTS_PREFIX + dayKey, false)

    fun setLogged(context: Context, logged: Boolean, dayKey: String = todayKey()) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NIGHTS_PREFIX + dayKey, logged)
            .apply()
        if (logged) updateStreak(context, dayKey)
    }

    fun toggleTonight(context: Context): Boolean {
        val today = todayKey()
        val next = !isLogged(context, today)
        setLogged(context, next, today)
        return next
    }

    private fun updateStreak(context: Context, today: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val last = prefs.getString(KEY_LAST_NIGHT, null)
        val streak = when (last) {
            null, today -> prefs.getInt(KEY_STREAK, 0).coerceAtLeast(1)
            previousDay(today) -> prefs.getInt(KEY_STREAK, 0) + 1
            else -> 1
        }
        prefs.edit()
            .putString(KEY_LAST_NIGHT, today)
            .putInt(KEY_STREAK, streak.coerceAtLeast(1))
            .apply()
    }

    private fun previousDay(today: String): String? = runCatching {
        val cal = Calendar.getInstance()
        cal.time = dayKeyFormat.parse(today) ?: return null
        cal.add(Calendar.DAY_OF_YEAR, -1)
        dayKeyFormat.format(cal.time)
    }.getOrNull()

    fun snapshot(context: Context): QiyamMonthSnapshot {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = todayKey()
        val cal = Calendar.getInstance()
        val monthPrefix = SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time)
        var monthCount = 0
        var last7 = 0
        prefs.all.keys.filter { it.startsWith(KEY_NIGHTS_PREFIX) }.forEach { key ->
            if (prefs.getBoolean(key, false)) {
                val day = key.removePrefix(KEY_NIGHTS_PREFIX)
                if (day.startsWith(monthPrefix)) monthCount++
                if (isWithinLastDays(day, 7)) last7++
            }
        }
        return QiyamMonthSnapshot(
            nightsThisMonth = monthCount,
            nightsLast7Days = last7,
            streak = prefs.getInt(KEY_STREAK, 0),
            isLoggedTonight = isLogged(context, today)
        )
    }

    private fun isWithinLastDays(dayKey: String, days: Int): Boolean = runCatching {
        val cal = Calendar.getInstance()
        val end = cal.time
        cal.add(Calendar.DAY_OF_YEAR, -days)
        val start = cal.time
        val parsed = dayKeyFormat.parse(dayKey) ?: return false
        !parsed.before(start) && !parsed.after(end)
    }.getOrDefault(false)
}
