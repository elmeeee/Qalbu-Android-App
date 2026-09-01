package app.kamy.saatApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.infrastructure.preferences.DailyVerseNotificationStoreReader
import app.kamy.saatApp.infrastructure.preferences.DailyVerseSnapshot
import app.kamy.saatApp.infrastructure.preferences.DailyVerseSnapshotStore
import app.kamy.saatApp.infrastructure.quran.DailyVerseLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Calendar

object DailyVerseNotificationScheduler {

    const val CHANNEL_ID = "daily_verse"
    const val EXTRA_CHAPTER = "daily_verse_chapter"
    const val EXTRA_AYAH = "daily_verse_ayah"

    private const val REQUEST_CODE = 7001
    private const val NOTIFICATION_ID = 7001
    private const val SHOW_ALARM_INTENT_REQUEST = 7_002

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DailyVerseEntryPoint {
        fun dailyVerseLoader(): DailyVerseLoader
    }

    suspend fun resolveSnapshot(context: Context): DailyVerseSnapshot? {
        DailyVerseSnapshotStore.loadForToday(context)?.let { return it }
        return runCatching {
            val loader = EntryPointAccessors.fromApplication(
                context.applicationContext,
                DailyVerseEntryPoint::class.java
            ).dailyVerseLoader()
            loader.loadForToday()
            DailyVerseSnapshotStore.loadForToday(context)
        }.getOrNull()
    }

    fun ensureChannel(context: Context) {
        NotificationChannels.ensureAll(context)
    }

    fun reschedule(context: Context) {
        val store = dailyVersePrefs(context)
        if (!store.isEnabled()) {
            cancel(context)
            return
        }
        scheduleAt(context, store.morningHour(), store.morningMinute(), store.days())
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
    }

    fun scheduleAt(context: Context, hour: Int, minute: Int, days: Set<Int> = (1..7).toSet()) {
        ensureChannel(context)
        cancel(context)
        val trigger = nextTriggerMillis(hour, minute, days)
        val pending = pendingIntent(context)
        ExactAlarmScheduler.scheduleExactAndAllowWhileIdle(
            context = context,
            triggerAtMillis = trigger,
            pending = pending
        )
    }

    fun scheduleNext(context: Context) {
        val store = dailyVersePrefs(context)
        if (!store.isEnabled()) {
            cancel(context)
            return
        }
        scheduleAt(context, store.morningHour(), store.morningMinute(), store.days())
    }

    private fun dailyVersePrefs(context: Context) = DailyVerseNotificationStoreReader(context)

    private fun getLocalizedContext(context: Context): Context {
        val lang = app.kamy.saatApp.infrastructure.preferences.AppLanguageStore.from(context).current()
        val locale = when (lang) {
            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> java.util.Locale.US
            app.kamy.saatApp.core.locale.AppLanguage.INDONESIAN -> java.util.Locale.forLanguageTag("id-ID")
            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> java.util.Locale.forLanguageTag("ms-MY")
        }
        val config = android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(config)
    }

    fun showNotification(context: Context, snapshot: DailyVerseSnapshot? = null) {
        ensureChannel(context)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val localContext = getLocalizedContext(context)
        val resolved = snapshot ?: DailyVerseSnapshotStore.loadForToday(context)

        val arabic = resolved?.arabic?.trim().orEmpty()
        val translationExcerpt = resolved?.translation?.trim().orEmpty()
        val reference = resolved?.let { "${it.surahName} ${it.ayahNumber}" }.orEmpty()

        val displayBody = buildString {
            if (arabic.isNotEmpty()) {
                append(arabic)
                append("\n\n")
            }
            if (translationExcerpt.isNotEmpty()) {
                append(translationExcerpt)
                append(" ")
            }
            if (reference.isNotEmpty()) {
                append("($reference)")
            }
        }.trim().ifEmpty { localContext.getString(R.string.daily_verse_notif_fallback) }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            resolved?.let {
                data = android.net.Uri.parse("saat://quran?chapter=${it.chapterNumber}&verse=${it.ayahNumber}")
            }
        }
        val pending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/raw/off_toggle_adzan")
        val vibrationPattern = longArrayOf(0, 400, 200, 400)
        val notification = NotificationCompat.Builder(context, NotificationChannels.DAILY_VERSE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(localContext.getString(R.string.daily_verse_notif_title))
            .setContentText(displayBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayBody))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(vibrationPattern)
            .build()
        PrayerNotificationScheduler.triggerHaptics(context)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyVerseNotificationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerMillis(hour: Int, minute: Int, days: Set<Int> = (1..7).toSet()): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
        }
        val compareCal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= compareCal.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val activeDays = if (days.isEmpty()) (1..7).toSet() else days
        while (cal.get(Calendar.DAY_OF_WEEK) !in activeDays) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
