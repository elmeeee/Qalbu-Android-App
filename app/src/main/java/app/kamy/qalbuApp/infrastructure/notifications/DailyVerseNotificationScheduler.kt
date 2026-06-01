package app.kamy.qalbuApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.qalbuApp.MainActivity
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.infrastructure.preferences.DailyVerseNotificationStore
import app.kamy.qalbuApp.infrastructure.preferences.DailyVerseSnapshot
import app.kamy.qalbuApp.infrastructure.preferences.DailyVerseSnapshotStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
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
        fun contentRepository(): ContentRepository
    }

    fun ensureChannel(context: Context) {
        NotificationChannels.ensureAll(context)
    }

    fun reschedule(context: Context) {
        val prefs = context.getSharedPreferences("qalbu_notification_prefs", Context.MODE_PRIVATE)
        val enabled = if (!prefs.contains("dailyVerseNotificationsEnabled")) {
            true
        } else {
            prefs.getBoolean("dailyVerseNotificationsEnabled", true)
        }
        if (!enabled) {
            cancel(context)
            return
        }
        val hour = prefs.getInt("dailyVerseNotificationHour", DailyVerseNotificationStore.DEFAULT_HOUR)
        val minute = prefs.getInt("dailyVerseNotificationMinute", DailyVerseNotificationStore.DEFAULT_MINUTE)
        scheduleAt(context, hour, minute)
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    fun scheduleAt(context: Context, hour: Int, minute: Int) {
        ensureChannel(context)
        cancel(context)
        val trigger = nextTriggerMillis(hour, minute)
        val pending = pendingIntent(context)
        ExactAlarmScheduler.schedule(
            context = context,
            triggerAtMillis = trigger,
            pending = pending,
            showIntentRequestCode = SHOW_ALARM_INTENT_REQUEST
        )
    }

    fun scheduleNext(context: Context) {
        val prefs = context.getSharedPreferences("qalbu_notification_prefs", Context.MODE_PRIVATE)
        val enabled = if (!prefs.contains("dailyVerseNotificationsEnabled")) {
            true
        } else {
            prefs.getBoolean("dailyVerseNotificationsEnabled", true)
        }
        if (!enabled) {
            cancel(context)
            return
        }
        val hour = prefs.getInt("dailyVerseNotificationHour", DailyVerseNotificationStore.DEFAULT_HOUR)
        val minute = prefs.getInt("dailyVerseNotificationMinute", DailyVerseNotificationStore.DEFAULT_MINUTE)
        scheduleAt(context, hour, minute)
    }

    suspend fun resolveSnapshot(context: Context): DailyVerseSnapshot? {
        DailyVerseSnapshotStore.loadForToday(context)?.let { return it }
        return runCatching {
            val repo = EntryPointAccessors.fromApplication(
                context.applicationContext,
                DailyVerseEntryPoint::class.java
            ).contentRepository()
            val verse = repo.getRandomAyah() ?: return null
            val chapterName = verse.chapterNumber?.let { chapter ->
                repo.getChapters().find { it.id == chapter }?.displayComplexName
            }
            DailyVerseSnapshotStore.save(context, verse, chapterName)
            DailyVerseSnapshotStore.loadForToday(context)
        }.getOrNull()
    }

    fun showNotification(context: Context, snapshot: DailyVerseSnapshot? = null) {
        ensureChannel(context)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val resolved = snapshot ?: DailyVerseSnapshotStore.loadForToday(context)
        val body = resolved?.notificationBody()
            ?: context.getString(R.string.daily_verse_notif_fallback)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            resolved?.let {
                putExtra(EXTRA_CHAPTER, it.chapterNumber)
                putExtra(EXTRA_AYAH, it.ayahNumber)
            }
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.DAILY_VERSE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.daily_verse_notif_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
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

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
