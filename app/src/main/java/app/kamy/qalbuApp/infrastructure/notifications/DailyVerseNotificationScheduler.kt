package app.kamy.qalbuApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.qalbuApp.MainActivity
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.infrastructure.preferences.DailyVerseNotificationStore
import java.util.Calendar

/**
 * Schedules a daily local notification — mirrors iOS DailyVerseNotificationScheduler.
 */
object DailyVerseNotificationScheduler {

    const val CHANNEL_ID = "daily_verse"
    private const val REQUEST_CODE = 7001
    private const val NOTIFICATION_ID = 7001

    fun ensureChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channel = android.app.NotificationChannel(
            CHANNEL_ID,
            "Daily verse",
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Morning Quran verse reminder"
        }
        manager.createNotificationChannel(channel)
    }

    fun reschedule(context: Context) {
        val prefs = context.getSharedPreferences("qalbu_notification_prefs", Context.MODE_PRIVATE)
        val enabled = if (!prefs.contains("dailyVerseNotificationsEnabled")) {
            true
        } else {
            prefs.getBoolean("dailyVerseNotificationsEnabled", true)
        }
        if (!enabled) {
            cancel(context)
            return
        }
        val hour = prefs.getInt("dailyVerseNotificationHour", DailyVerseNotificationStore.DEFAULT_HOUR)
        val minute = prefs.getInt("dailyVerseNotificationMinute", DailyVerseNotificationStore.DEFAULT_MINUTE)
        scheduleAt(context, hour, minute)
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    fun scheduleAt(context: Context, hour: Int, minute: Int) {
        ensureChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trigger = nextTriggerMillis(hour, minute)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            trigger,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context)
        )
    }

    fun scheduleNext(context: Context) {
        val prefs = context.getSharedPreferences("qalbu_notification_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("dailyVerseNotificationHour", DailyVerseNotificationStore.DEFAULT_HOUR)
        val minute = prefs.getInt("dailyVerseNotificationMinute", DailyVerseNotificationStore.DEFAULT_MINUTE)
        scheduleAt(context, hour, minute)
    }

    fun showNotification(context: Context) {
        ensureChannel(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your verse for today")
            .setContentText("Open Al-Khatib to read today's verse.")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyVerseNotificationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
