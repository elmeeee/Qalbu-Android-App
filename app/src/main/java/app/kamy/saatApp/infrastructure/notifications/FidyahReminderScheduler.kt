package app.kamy.saatApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.FidyahStore
import java.util.Calendar

object FidyahReminderScheduler {

    private const val REQUEST_CODE_FIDYAH = 14_000
    private const val NOTIFICATION_ID_FIDYAH = 14_000

    fun scheduleWeeklyReminder(context: Context) {
        val appContext = context.applicationContext
        val store = FidyahStore.from(appContext)
        val unpaidRecords = store.getRecords().filter { !it.isFullyPaid }

        if (unpaidRecords.isEmpty()) {
            cancelReminder(appContext)
            return
        }

        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) return

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(appContext, FidyahReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE_FIDYAH,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        runCatching {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(appContext, FidyahReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE_FIDYAH,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun showNotification(context: Context) {
        val store = FidyahStore.from(context)
        val unpaid = store.getRecords().filter { !it.isFullyPaid }
        if (unpaid.isEmpty()) return

        val lang = AppLanguageStore.from(context).current()
        val totalUnpaidDays = unpaid.sumOf { it.missedDays }

        val title = when (lang) {
            AppLanguage.MALAY -> "Peringatan Fidyah & Qada Puasa"
            AppLanguage.ENGLISH -> "Fidyah & Qadha Fasting Reminder"
            else -> "Pengingat Fidyah & Qadha Puasa"
        }

        val content = when (lang) {
            AppLanguage.MALAY -> "Anda masih mempunyai $totalUnpaidDays hari rekod fidyah yang belum dijelaskan. Ketik untuk menyemak."
            AppLanguage.ENGLISH -> "You have $totalUnpaidDays missed fast days with pending fidyah. Tap to review."
            else -> "Kamu masih memiliki $totalUnpaidDays hari fidyah & qadha yang belum lunas. Ketik untuk memeriksa."
        }

        NotificationChannels.ensureAll(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_route", "tools/fidyah")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_FIDYAH,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/raw/off_toggle_adzan")

        val builder = NotificationCompat.Builder(context, NotificationChannels.PRAYER_TRACKER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_FIDYAH, builder.build())
        }
    }
}
