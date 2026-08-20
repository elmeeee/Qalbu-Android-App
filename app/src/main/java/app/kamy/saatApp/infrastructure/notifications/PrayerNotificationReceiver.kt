package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.saatApp.infrastructure.audio.AdhanPlaybackLauncher
import app.kamy.saatApp.infrastructure.preferences.AdhanPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val appContext = context.applicationContext

        // Check if user allows notifications. If disabled, skip BOTH push notification AND adhan sound!
        val areNotificationsEnabled = androidx.core.app.NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        if (!areNotificationsEnabled) {
            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                runCatching { PrayerNotificationCoordinator.rescheduleFromCache(appContext) }
            }
            return
        }

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

        val globalAdhanSoundEnabled = PrayerNotificationPreferencesStore.from(appContext).isAdhanSoundEnabled()
        val shouldPlayAdhan = playAdhan && globalAdhanSoundEnabled

        val isTahajud = kind?.contains("LAST_THIRD") == true || kind?.contains("tahajud") == true || prayerName?.equals("tahajud", ignoreCase = true) == true
        var adhanRawRes: Int? = null
        val isPlaybackStarted = if (playAdhan && isTahajud) {
            adhanRawRes = app.kamy.saatApp.R.raw.tahajud_alarm
            AdhanPlaybackLauncher.startAdhanPlayback(
                context = appContext,
                rawRes = app.kamy.saatApp.R.raw.tahajud_alarm,
                title = title,
                body = body,
                notificationId = notificationId,
                prayerName = "tahajud"
            )
        } else if (shouldPlayAdhan && !prayerName.isNullOrBlank()) {
            val store = AdhanPreferencesStore.from(appContext)
            val voice = store.currentVoice()
            val fajrVoice = store.currentFajrVoice()
            val rawRes = AdhanVoiceCatalog.rawResForPrayer(prayerName, voice, fajrVoice)
            adhanRawRes = rawRes
            AdhanPlaybackLauncher.startAdhanPlayback(
                context = appContext,
                rawRes = rawRes,
                title = title,
                body = body,
                notificationId = notificationId,
                prayerName = prayerName
            )
        } else {
            false
        }

        // Only show separate push notification if AdhanPlaybackService was NOT started,
        // preventing duplicate/double notifications when adhan plays.
        if (title.isNotEmpty() && !isPlaybackStarted) {
            val alertChannel = when {
                kind?.startsWith("prayer_") == true || kind == "imsak" -> {
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
                showStopAdhan = (shouldPlayAdhan || (playAdhan && isTahajud)),
                adhanSoundRes = if (shouldPlayAdhan) adhanRawRes else null,
                kind = kind,
                useFullScreenIntent = isTahajud
            )
        }

        // Roll alarms forward in background — do not block the broadcast receiver.
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                PrayerNotificationCoordinator.rescheduleFromCache(appContext)
            } catch (_: Throwable) {
                // Reschedule failure is non-critical; next app launch will repair alarms.
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
