package app.kamy.saatApp.infrastructure.preferences

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.kamy.saatApp.infrastructure.notifications.ExactAlarmScheduler
import app.kamy.saatApp.infrastructure.notifications.SurahReminderReceiver
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Serializable
data class SurahReminder(
    val id: String,
    val surahNumber: Int,
    val surahName: String,
    val weekday: Int, // Calendar.SUNDAY to Calendar.SATURDAY
    val hour: Int,
    val minute: Int,
    val enabled: Boolean
)

@Singleton
class SurahReminderStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("saat_surah_reminders", Context.MODE_PRIVATE)

    fun getReminders(): List<SurahReminder> {
        val raw = prefs.getString("reminders", null) ?: return seedDefaultReminders()
        return try {
            Json.decodeFromString<List<SurahReminder>>(raw)
        } catch (e: Exception) {
            seedDefaultReminders()
        }
    }

    private fun seedDefaultReminders(): List<SurahReminder> {
        val defaults = listOf(
            SurahReminder(
                id = "default_yasin",
                surahNumber = 36,
                surahName = "Yasin",
                weekday = Calendar.THURSDAY,
                hour = 20,
                minute = 0,
                enabled = true
            ),
            SurahReminder(
                id = "default_kahf",
                surahNumber = 18,
                surahName = "Al-Kahf",
                weekday = Calendar.FRIDAY,
                hour = 10,
                minute = 30,
                enabled = true
            )
        )
        saveReminders(defaults)
        return defaults
    }

    fun saveReminders(list: List<SurahReminder>) {
        prefs.edit().putString("reminders", Json.encodeToString(list)).apply()
        // Reschedule all alarms whenever saved
        rescheduleAlarms(list)
    }

    fun addReminder(reminder: SurahReminder) {
        val current = getReminders().toMutableList()
        current.add(reminder)
        saveReminders(current)
    }

    fun updateReminder(reminder: SurahReminder) {
        val current = getReminders().map { if (it.id == reminder.id) reminder else it }
        saveReminders(current)
    }

    fun deleteReminder(id: String) {
        // Cancel the alarm first
        cancelAlarm(id)
        val current = getReminders().filterNot { it.id == id }
        saveReminders(current)
    }

    fun rescheduleAlarms(list: List<SurahReminder>) {
        val prefs = PrayerNotificationPreferencesStore.from(context)
        // Cancel first
        list.forEach { cancelAlarm(it.id) }
        // Schedule if enabled both per-reminder and per-surah preference
        list.filter { it.enabled }.forEach { reminder ->
            val isGlobalEnabled = when (reminder.surahNumber) {
                36 -> prefs.isYasinReminderEnabled()
                18 -> prefs.isKahfReminderEnabled()
                else -> true
            }
            if (isGlobalEnabled) {
                scheduleAlarm(reminder)
            }
        }
    }

    fun scheduleAlarm(reminder: SurahReminder) {
        val now = System.currentTimeMillis()
        val fireAt = nextWeekdayTime(reminder.weekday, reminder.hour, reminder.minute, now)

        val intent = Intent(context, SurahReminderReceiver::class.java).apply {
            action = "app.kamy.saatApp.ACTION_SURAH_REMINDER"
            putExtra("reminder_id", reminder.id)
            putExtra("surah_number", reminder.surahNumber)
            putExtra("surah_name", reminder.surahName)
        }

        val requestCode = reminder.id.hashCode()
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        ExactAlarmScheduler.schedule(
            context = context,
            triggerAtMillis = fireAt,
            pending = pending,
            showIntentRequestCode = reminder.id.hashCode()
        )
    }

    fun cancelAlarm(id: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SurahReminderReceiver::class.java).apply {
            action = "app.kamy.saatApp.ACTION_SURAH_REMINDER"
        }
        val requestCode = id.hashCode()
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending != null) {
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }

    private fun nextWeekdayTime(weekday: Int, hour: Int, minute: Int, from: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = from }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val compareCal = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fromTime = compareCal.timeInMillis

        while (cal.get(Calendar.DAY_OF_WEEK) != weekday || cal.timeInMillis <= fromTime) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    companion object {
        fun from(context: Context): SurahReminderStore = SurahReminderStore(context.applicationContext)
    }
}
