package app.kamy.qalbuApp.infrastructure.preferences

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

    companion object {
        private const val PREFS_NAME = "qalbu_notification_prefs"
        private const val KEY_ENABLED = "dailyVerseNotificationsEnabled"
        private const val KEY_HOUR = "dailyVerseNotificationHour"
        private const val KEY_MINUTE = "dailyVerseNotificationMinute"
    }
}
