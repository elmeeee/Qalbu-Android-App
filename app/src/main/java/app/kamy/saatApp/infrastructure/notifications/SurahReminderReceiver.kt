package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.kamy.saatApp.core.locale.AppLanguage
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
        
        // Trigger notification
        val languageStore = AppLanguageStore.from(appContext)
        val isIndoMalay = languageStore.current() == AppLanguage.INDONESIAN || languageStore.current() == AppLanguage.MALAY
        
        val title = if (isIndoMalay) {
            "Waktunya Membaca Surah $surahName"
        } else {
            "Time to read Surah $surahName"
        }
        
        val body = if (isIndoMalay) {
            "Sempatkan membaca Surah $surahName hari ini."
        } else {
            "Take a moment to read Surah $surahName today."
        }
        
        // Show notification with default android notification sound
        PrayerNotificationScheduler.showNotification(
            context = appContext,
            notificationId = reminderId.hashCode(),
            channelId = NotificationChannels.SUNNAH,
            title = title,
            body = body,
            silent = false,
            showStopAdhan = false,
            adhanSoundRes = null,
            kind = "custom_surah_$surahNumber"
        )
        
        // Reschedule for next week
        store.scheduleAlarm(reminder)
    }
}
