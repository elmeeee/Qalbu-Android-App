package app.kamy.saatApp.infrastructure.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore

class PrayerCheckReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val kind = intent?.getStringExtra(EXTRA_KIND).orEmpty()
        val dayKey = intent?.getStringExtra(EXTRA_DAY_KEY) ?: PrayerTrackerStore.todayKey()
        val prayerName = intent?.getStringExtra(EXTRA_PRAYER)

        if (dayKey != PrayerTrackerStore.todayKey()) {
            PrayerCheckReminderScheduler.reschedule(appContext)
            return
        }

        when (kind) {
            PrayerCheckReminderScheduler.KIND_PRAYER -> {
                val prayer = PrayerType.fromAladhanKey(prayerName.orEmpty()) ?: return
                if (PrayerTrackerStore.isCompleted(appContext, prayer, dayKey)) return
                showPrayerReminder(appContext, prayer)
            }
            PrayerCheckReminderScheduler.KIND_WRAP_UP -> {
                if (PrayerTrackerStore.completedCount(appContext, dayKey) >=
                    PrayerTrackerStore.TRACKED_PRAYERS.size
                ) {
                    return
                }
                showWrapUpReminder(appContext)
            }
        }
        PrayerCheckReminderScheduler.reschedule(appContext)
    }

    private fun showPrayerReminder(context: Context, prayer: PrayerType) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        NotificationChannels.ensureAll(context)
        val prayerLabel = AppNotificationCopy.prayerDisplayName(context, prayer.aladhanKey)
        val title = context.getString(R.string.prayer_check_notif_title, prayerLabel)
        val body = context.getString(R.string.prayer_check_notif_body)
        val notificationId = NOTIFICATION_ID_BASE + prayer.ordinal

        val openIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.PRAYER_TRACKER)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun showWrapUpReminder(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        NotificationChannels.ensureAll(context)
        val remaining = PrayerTrackerStore.TRACKED_PRAYERS.size -
            PrayerTrackerStore.completedCount(context)
        val title = context.getString(R.string.prayer_wrapup_notif_title)
        val body = context.getString(R.string.prayer_wrapup_notif_body, remaining)

        val openIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_WRAP_UP,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.PRAYER_TRACKER)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_WRAP_UP, notification)
    }

    companion object {
        const val EXTRA_PRAYER = "prayer_name"
        const val EXTRA_DAY_KEY = "day_key"
        const val EXTRA_KIND = "kind"
        private const val NOTIFICATION_ID_BASE = 12_500
        private const val NOTIFICATION_ID_WRAP_UP = 12_599
    }
}
