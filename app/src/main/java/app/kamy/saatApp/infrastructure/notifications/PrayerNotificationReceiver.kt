package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.saatApp.infrastructure.audio.AdhanPlaybackService
import app.kamy.saatApp.infrastructure.preferences.AdhanPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: NotificationChannels.PRAYER
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val playAdhan = intent.getBooleanExtra(EXTRA_PLAY_ADHAN, false)
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME)
        val kind = intent.getStringExtra(EXTRA_KIND)
        val fireAt = intent.getLongExtra(EXTRA_FIRE_AT, System.currentTimeMillis())
        val title = AppNotificationCopy.resolveTitle(appContext, kind, prayerName, fireAt)
            .ifBlank { intent.getStringExtra(EXTRA_TITLE).orEmpty() }
        val body = AppNotificationCopy.resolveBody(appContext, kind, prayerName)
            .ifBlank { intent.getStringExtra(EXTRA_BODY).orEmpty() }

        val shouldPlayAdhan = playAdhan

        var adhanPlaying = false
        var adhanRawRes: Int? = null
        if (shouldPlayAdhan && !prayerName.isNullOrBlank()) {
            val voice = AdhanPreferencesStore.from(appContext).currentVoice()
            val rawRes = AdhanVoiceCatalog.rawResForPrayer(prayerName, voice)
            adhanRawRes = rawRes
            adhanPlaying = AdhanPlaybackService.start(
                context = appContext,
                rawRes = rawRes,
                title = title,
                body = body,
                notificationId = notificationId
            )
        }

        if (title.isNotEmpty()) {
            if (shouldPlayAdhan && adhanPlaying) {
                // Foreground service already shows the adhan notification with stop action.
            } else {
                val alertChannel = when {
                    shouldPlayAdhan && adhanRawRes != null -> {
                        NotificationChannels.ensureAdhanAlert(appContext, adhanRawRes)
                        NotificationChannels.ADHAN_ALERT
                    }
                    kind?.startsWith("prayer_") == true || kind == "imsak" -> {
                        // Toggle OFF → default device sound via high-importance PRAYER_ALERT channel
                        NotificationChannels.PRAYER_ALERT
                    }
                    else -> channelId
                }
                PrayerNotificationScheduler.showNotification(
                    context = appContext,
                    notificationId = notificationId,
                    channelId = alertChannel,
                    title = title,
                    body = body,
                    silent = false,
                    showStopAdhan = shouldPlayAdhan && adhanRawRes != null,
                    adhanSoundRes = if (shouldPlayAdhan && adhanRawRes != null) adhanRawRes else null,
                    kind = kind
                )
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                // Roll alarms forward only — avoid network + Hilt on every adhan fire.
                PrayerNotificationCoordinator.rescheduleFromCache(appContext)
            } finally {
                pendingResult.finish()
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
        const val EXTRA_FIRE_AT = "fire_at"
    }
}
