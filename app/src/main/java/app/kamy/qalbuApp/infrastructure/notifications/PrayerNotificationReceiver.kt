package app.kamy.qalbuApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: NotificationChannels.PRAYER
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        if (title.isNotEmpty()) {
            PrayerNotificationScheduler.showNotification(
                context = context,
                notificationId = notificationId,
                channelId = channelId,
                title = title,
                body = body
            )
        }
        // Re-schedule weekly sunnah + refresh prayer alarms from cache after each fire.
        PrayerNotificationCoordinator.rescheduleFromCache(context)
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_KIND = "kind"
    }
}
