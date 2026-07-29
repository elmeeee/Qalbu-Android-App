package app.kamy.saatApp.infrastructure.preferences

import android.content.Context

/** Non-Hilt read access for alarm receivers and schedulers. */
internal class DailyVerseNotificationStoreReader(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean =
        if (!prefs.contains(KEY_ENABLED)) true else prefs.getBoolean(KEY_ENABLED, true)

    fun morningHour(): Int =
        prefs.getInt(KEY_HOUR, DailyVerseNotificationStore.DEFAULT_HOUR).coerceIn(0, 23)

    fun morningMinute(): Int =
        prefs.getInt(KEY_MINUTE, DailyVerseNotificationStore.DEFAULT_MINUTE).coerceIn(0, 59)

    fun days(): Set<Int> {
        val raw = prefs.getString(KEY_DAYS, "1,2,3,4,5,6,7")
        if (raw.isNullOrBlank()) return (1..7).toSet()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet().ifEmpty { (1..7).toSet() }
    }

    companion object {
        private const val PREFS_NAME = "saat_notification_prefs"
        private const val KEY_ENABLED = "dailyVerseNotificationsEnabled"
        private const val KEY_HOUR = "dailyVerseNotificationHour"
        private const val KEY_MINUTE = "dailyVerseNotificationMinute"
        private const val KEY_DAYS = "dailyVerseNotificationDays"
    }
}
