package app.kamy.saatApp.infrastructure.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.LocalReadingProgress
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.DailyVerseSnapshotStore
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.QuranLastReadReminderStore
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import app.kamy.saatApp.infrastructure.repository.QuranRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object QuranLastReadReminderScheduler {

    const val NOTIFICATION_ID = 7005
    private const val WORK_NAME = "quran_last_read_reminder_work"

    enum class ReminderStage {
        NONE,
        THREE_DAYS,
        SEVEN_DAYS
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface QuranReminderEntryPoint {
        fun quranRepository(): QuranRepository
    }

    fun enqueue(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<QuranLastReadReminderWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    suspend fun evaluateAndNotify(context: Context, nowMillis: Long = System.currentTimeMillis()): Boolean {
        NotificationChannels.ensureAll(context)

        val prefs = PrayerNotificationPreferencesStore.from(context)
        if (!prefs.isQuranReminderEnabled()) return false

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false

        val progress = QuranPersonalStore.lastReadProgress(context) ?: return false
        if (progress.chapterNumber <= 0 || progress.verseNumber <= 0 || progress.updatedAtMillis <= 0L) {
            return false
        }

        val stage = evaluateStage(context, progress, nowMillis)
        if (stage == ReminderStage.NONE) return false

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(nowMillis))
        val reminderStore = QuranLastReadReminderStore.from(context)
        if (reminderStore.lastNotificationDate() == todayDate) {
            return false
        }

        // Daily Verse conflict check: if Daily Verse was recorded today, skip/defer to prevent dual Qur'an notifications
        val dailyVerseToday = DailyVerseSnapshotStore.loadForToday(context)
        if (dailyVerseToday != null) {
            val hourOfDay = java.util.Calendar.getInstance().apply { timeInMillis = nowMillis }.get(java.util.Calendar.HOUR_OF_DAY)
            // If it's early morning (before 12:00) and daily verse is active, defer to avoid morning notification clash
            if (hourOfDay < 12) {
                return false
            }
        }

        val surahName = resolveSurahName(context, progress.chapterNumber)
        val locationString = "$surahName: ${progress.verseNumber}"

        showNotification(context, progress, stage, locationString)

        when (stage) {
            ReminderStage.THREE_DAYS -> reminderStore.mark3DayReminderSent(progress.updatedAtMillis, todayDate)
            ReminderStage.SEVEN_DAYS -> reminderStore.mark7DayReminderSent(progress.updatedAtMillis, todayDate)
            ReminderStage.NONE -> Unit
        }

        return true
    }

    fun evaluateStage(
        context: Context,
        progress: LocalReadingProgress,
        nowMillis: Long = System.currentTimeMillis()
    ): ReminderStage = evaluateStage(QuranLastReadReminderStore.from(context), progress, nowMillis)

    fun evaluateStage(
        reminderStore: QuranLastReadReminderStore,
        progress: LocalReadingProgress,
        nowMillis: Long = System.currentTimeMillis()
    ): ReminderStage {
        if (progress.updatedAtMillis <= 0L || nowMillis < progress.updatedAtMillis) {
            return ReminderStage.NONE
        }

        val daysSinceLastRead = ((nowMillis - progress.updatedAtMillis) / (1000L * 60 * 60 * 24)).toInt()

        return when {
            daysSinceLastRead < 3 -> ReminderStage.NONE
            daysSinceLastRead in 3..6 -> {
                if (reminderStore.is3DayReminderSent(progress.updatedAtMillis)) {
                    ReminderStage.NONE
                } else {
                    ReminderStage.THREE_DAYS
                }
            }
            daysSinceLastRead in 7..13 -> {
                if (reminderStore.is7DayReminderSent(progress.updatedAtMillis)) {
                    ReminderStage.NONE
                } else {
                    ReminderStage.SEVEN_DAYS
                }
            }
            else -> {
                // 14+ days: do not spam daily
                ReminderStage.NONE
            }
        }
    }

    private suspend fun resolveSurahName(context: Context, chapterNumber: Int): String {
        return runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                QuranReminderEntryPoint::class.java
            )
            val chapters = entryPoint.quranRepository().getChapters()
            chapters.find { it.id == chapterNumber }?.displayComplexName
        }.getOrNull() ?: "Surah $chapterNumber"
    }

    private fun getLocalizedContext(context: Context): Context {
        val lang = AppLanguageStore.from(context).current()
        val locale = when (lang) {
            AppLanguage.ENGLISH -> Locale.US
            AppLanguage.INDONESIAN -> Locale.forLanguageTag("id-ID")
            AppLanguage.MALAY -> Locale.forLanguageTag("ms-MY")
        }
        val config = android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(config)
    }

    private fun showNotification(
        context: Context,
        progress: LocalReadingProgress,
        stage: ReminderStage,
        locationString: String
    ) {
        val localContext = getLocalizedContext(context)

        val (title, body) = when (stage) {
            ReminderStage.THREE_DAYS -> {
                val t = localContext.getString(R.string.last_read_reminder_3d_title)
                val b = localContext.getString(R.string.last_read_reminder_3d_body, locationString)
                t to b
            }
            ReminderStage.SEVEN_DAYS -> {
                val t = localContext.getString(R.string.last_read_reminder_7d_title)
                val b = localContext.getString(R.string.last_read_reminder_7d_body, locationString)
                t to b
            }
            ReminderStage.NONE -> return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = Uri.parse("saat://quran/${progress.chapterNumber}/${progress.verseNumber}")
            putExtra(DailyVerseNotificationScheduler.EXTRA_CHAPTER, progress.chapterNumber)
            putExtra(DailyVerseNotificationScheduler.EXTRA_AYAH, progress.verseNumber)
            putExtra("chapter", progress.chapterNumber)
            putExtra("ayah", progress.verseNumber)
        }

        val pending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = Uri.parse("android.resource://${context.packageName}/raw/off_toggle_adzan")
        val vibrationPattern = longArrayOf(0, 400, 200, 400)

        val notification = NotificationCompat.Builder(context, NotificationChannels.QURAN_REMINDER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(vibrationPattern)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
