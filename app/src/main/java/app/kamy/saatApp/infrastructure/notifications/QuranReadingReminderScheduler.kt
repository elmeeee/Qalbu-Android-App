package app.kamy.saatApp.infrastructure.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.infrastructure.preferences.QuranReadingReminderStore
import java.util.Calendar

object QuranReadingReminderScheduler {
    const val CHANNEL_ID = NotificationChannels.QURAN_READING
    const val EXTRA_OPEN_QURAN = "open_quran_tab"

    private const val REQUEST_CODE = 7101
    private const val NOTIFICATION_ID = 7101
    private const val SHOW_ALARM_INTENT_REQUEST = 7_102

    fun reschedule(context: Context) {
        val store = QuranReadingReminderStore
        if (!store.isEnabled(context)) {
            cancel(context)
            return
        }
        scheduleAt(context, store.hour(context), store.minute(context))
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.cancel(pendingIntent(context))
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    fun scheduleAt(context: Context, hour: Int, minute: Int) {
        NotificationChannels.ensureAll(context)
        cancel(context)
        ExactAlarmScheduler.schedule(
            context = context,
            triggerAtMillis = nextTriggerMillis(hour, minute),
            pending = pendingIntent(context),
            showIntentRequestCode = SHOW_ALARM_INTENT_REQUEST
        )
    }

    fun showNotification(context: Context) {
        NotificationChannels.ensureAll(context)
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_QURAN, true)
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.quran_reading_reminder_title))
            .setContentText(context.getString(R.string.quran_reading_reminder_body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, QuranReadingReminderReceiver::class.java)
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
