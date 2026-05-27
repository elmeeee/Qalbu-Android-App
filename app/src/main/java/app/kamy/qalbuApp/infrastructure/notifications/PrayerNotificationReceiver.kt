package app.kamy.qalbuApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.kamy.qalbuApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.qalbuApp.infrastructure.audio.AdhanPlaybackService
import app.kamy.qalbuApp.infrastructure.preferences.AdhanPreferencesStore

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: NotificationChannels.PRAYER
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val playAdhan = intent.getBooleanExtra(EXTRA_PLAY_ADHAN, false)
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME)

        if (title.isNotEmpty()) {
            PrayerNotificationScheduler.showNotification(
                context = context,
                notificationId = notificationId,
                channelId = channelId,
                title = title,
                body = body,
                silent = playAdhan
            )
        }

        if (playAdhan && !prayerName.isNullOrBlank()) {
            val voice = AdhanPreferencesStore.from(context).currentVoice()
            val rawRes = AdhanVoiceCatalog.rawResForPrayer(prayerName, voice)
            AdhanPlaybackService.start(context, rawRes, title, body)
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
        const val EXTRA_PLAY_ADHAN = "play_adhan"
        const val EXTRA_PRAYER_NAME = "prayer_name"
    }
}
