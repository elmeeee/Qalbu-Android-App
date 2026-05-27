package app.kamy.qalbuApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.kamy.qalbuApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.qalbuApp.infrastructure.audio.AdhanPlaybackService
import app.kamy.qalbuApp.infrastructure.preferences.AdhanPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val appContext = context.applicationContext
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: NotificationChannels.PRAYER
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val playAdhan = intent.getBooleanExtra(EXTRA_PLAY_ADHAN, false)
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME)

        // Play adhan even when notification permission is off (exact-alarm delivery).
        if (playAdhan && !prayerName.isNullOrBlank()) {
            val voice = AdhanPreferencesStore.from(appContext).currentVoice()
            val rawRes = AdhanVoiceCatalog.rawResForPrayer(prayerName, voice)
            runCatching {
                AdhanPlaybackService.start(appContext, rawRes, title, body)
            }
        }

        if (title.isNotEmpty()) {
            PrayerNotificationScheduler.showNotification(
                context = appContext,
                notificationId = notificationId,
                channelId = channelId,
                title = title,
                body = body,
                silent = playAdhan
            )
        }

        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                // Roll alarms forward only — avoid network + Hilt on every adhan fire.
                PrayerNotificationCoordinator.rescheduleFromCache(appContext)
            } finally {
                pending.finish()
            }
        }
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
