package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RamadanCountdownPreferencesStore {
    private const val PREFS = "saat_ramadan_countdown"
    private const val KEY_ENABLED = "countdown_enabled"
    private const val KEY_HOUR = "countdown_hour"
    private const val KEY_MINUTE = "countdown_minute"
    private const val KEY_LAST_SHOWN_DATE = "countdown_last_shown_date"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun morningHour(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_HOUR, 8)

    fun morningMinute(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_MINUTE, 30)

    fun setMorningTime(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }

    fun hasShownToday(context: Context): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val lastShown = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_SHOWN_DATE, null)
        return lastShown == todayStr
    }

    fun markShownToday(context: Context) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_SHOWN_DATE, todayStr)
            .apply()
    }
}
