package app.kamy.saatApp.infrastructure.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.SurahReminderStore

class SurahReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val surahNumber = intent.getIntExtra("surah_number", 1)
        val surahName = intent.getStringExtra("surah_name") ?: "Al-Kahf"

        val appContext = context.applicationContext
        val store = SurahReminderStore.from(appContext)
        val reminders = store.getReminders()
        val reminder = reminders.firstOrNull { it.id == reminderId }

        if (reminder == null || !reminder.enabled) return

        // Localized context for 3-language support (EN / ID / MS)
        val lang = AppLanguageStore.from(appContext).current()
        val localCtx = AppLocale.wrap(appContext, lang)

        val title = localCtx.getString(R.string.surah_reminder_notif_title, surahName)
        val body = localCtx.getString(R.string.surah_reminder_notif_body, surahName)

        // Show notification using the SUNNAH channel (IMPORTANCE_HIGH)
        val notificationId = reminderId.hashCode()
        val openIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("surah_number", surahNumber)
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        PrayerNotificationScheduler.showNotification(
            context = appContext,
            notificationId = notificationId,
            channelId = NotificationChannels.SUNNAH,
            title = title,
            body = body,
            silent = false,
            showStopAdhan = false,
            adhanSoundRes = null,
            kind = "sunnah_surah_$surahNumber",
            customPendingIntent = pendingIntent
        )

        // Reschedule for next week
        store.scheduleAlarm(reminder)
    }
}
