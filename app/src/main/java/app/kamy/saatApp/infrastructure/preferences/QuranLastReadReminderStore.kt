package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import android.content.SharedPreferences

class QuranLastReadReminderStore(private val prefs: SharedPreferences) {

    fun is3DayReminderSent(lastReadTimestamp: Long): Boolean {
        val storedReadTimestamp = prefs.getLong(KEY_LAST_READ_TIMESTAMP_3D, 0L)
        return storedReadTimestamp == lastReadTimestamp && prefs.getBoolean(KEY_3D_SENT, false)
    }

    fun mark3DayReminderSent(lastReadTimestamp: Long, dateString: String) {
        prefs.edit()
            .putLong(KEY_LAST_READ_TIMESTAMP_3D, lastReadTimestamp)
            .putBoolean(KEY_3D_SENT, true)
            .putString(KEY_LAST_NOTIFICATION_DATE, dateString)
            .apply()
    }

    fun is7DayReminderSent(lastReadTimestamp: Long): Boolean {
        val storedReadTimestamp = prefs.getLong(KEY_LAST_READ_TIMESTAMP_7D, 0L)
        return storedReadTimestamp == lastReadTimestamp && prefs.getBoolean(KEY_7D_SENT, false)
    }

    fun mark7DayReminderSent(lastReadTimestamp: Long, dateString: String) {
        prefs.edit()
            .putLong(KEY_LAST_READ_TIMESTAMP_7D, lastReadTimestamp)
            .putBoolean(KEY_7D_SENT, true)
            .putString(KEY_LAST_NOTIFICATION_DATE, dateString)
            .apply()
    }

    fun lastNotificationDate(): String? = prefs.getString(KEY_LAST_NOTIFICATION_DATE, null)

    fun reset() {
        prefs.edit()
            .remove(KEY_LAST_READ_TIMESTAMP_3D)
            .remove(KEY_3D_SENT)
            .remove(KEY_LAST_READ_TIMESTAMP_7D)
            .remove(KEY_7D_SENT)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "quran_last_read_reminder_prefs"
        private const val KEY_LAST_READ_TIMESTAMP_3D = "last_read_ts_3d"
        private const val KEY_3D_SENT = "reminder_3d_sent"
        private const val KEY_LAST_READ_TIMESTAMP_7D = "last_read_ts_7d"
        private const val KEY_7D_SENT = "reminder_7d_sent"
        private const val KEY_LAST_NOTIFICATION_DATE = "last_notif_date"

        fun from(context: Context): QuranLastReadReminderStore =
            QuranLastReadReminderStore(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    }
}
