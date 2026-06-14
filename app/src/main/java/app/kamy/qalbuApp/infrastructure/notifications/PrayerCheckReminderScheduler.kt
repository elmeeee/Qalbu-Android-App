package app.kamy.qalbuApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.infrastructure.preferences.PrayerTrackerPreferencesStore
import app.kamy.qalbuApp.infrastructure.preferences.PrayerTrackerStore
import java.util.Calendar

object PrayerCheckReminderScheduler {

    private const val REQUEST_BASE = 12_000
    private const val WRAP_UP_REQUEST = 12_100
    private const val CHECK_DELAY_MS = 45 * 60 * 1000L
    private const val WRAP_UP_HOUR = 21
    private const val WRAP_UP_MINUTE = 0

    fun reschedule(context: Context) {
        val appContext = context.applicationContext
        cancelAll(appContext)
        if (!PrayerTrackerPreferencesStore.from(appContext).checkRemindersEnabled()) return
        if (!NotificationManagerCompatHelper.areEnabled(appContext)) return

        val bundle = PrayerScheduleCache.load(appContext) ?: return
        val today = PrayerTrackerStore.todayKey()
        val now = System.currentTimeMillis()

        bundle.adzanPrayers.forEachIndexed { index, item ->
            val prayer = PrayerType.fromAladhanKey(item.name) ?: return@forEachIndexed
            if (PrayerTrackerStore.isCompleted(appContext, prayer, today)) return@forEachIndexed
            val fireAt = item.fireAtMillis + CHECK_DELAY_MS
            if (fireAt <= now) return@forEachIndexed
            scheduleOne(
                context = appContext,
                requestCode = REQUEST_BASE + index,
                fireAt = fireAt,
                prayer = prayer.name,
                dayKey = today,
                kind = KIND_PRAYER
            )
        }

        if (PrayerTrackerStore.completedCount(appContext, today) < PrayerTrackerStore.TRACKED_PRAYERS.size) {
            val wrapUp = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, WRAP_UP_HOUR)
                set(Calendar.MINUTE, WRAP_UP_MINUTE)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (wrapUp.timeInMillis <= now) return
            scheduleOne(
                context = appContext,
                requestCode = WRAP_UP_REQUEST,
                fireAt = wrapUp.timeInMillis,
                prayer = null,
                dayKey = today,
                kind = KIND_WRAP_UP
            )
        }
    }

    fun onPrayerMarked(context: Context, prayer: PrayerType) {
        val appContext = context.applicationContext
        val index = PrayerTrackerStore.TRACKED_PRAYERS.indexOf(prayer)
        if (index >= 0) {
            cancelRequest(appContext, REQUEST_BASE + index)
        }
        if (PrayerTrackerStore.completedCount(appContext) >= PrayerTrackerStore.TRACKED_PRAYERS.size) {
            cancelRequest(appContext, WRAP_UP_REQUEST)
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        (REQUEST_BASE until REQUEST_BASE + 10).forEach { code ->
            alarmManager.cancel(pendingIntent(context, code))
        }
        alarmManager.cancel(pendingIntent(context, WRAP_UP_REQUEST))
    }

    private fun cancelRequest(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, requestCode))
    }

    private fun scheduleOne(
        context: Context,
        requestCode: Int,
        fireAt: Long,
        prayer: String?,
        dayKey: String,
        kind: String
    ) {
        NotificationChannels.ensureAll(context)
        val pending = pendingIntent(context, requestCode, prayer, dayKey, kind)
        ExactAlarmScheduler.schedule(
            context = context,
            triggerAtMillis = fireAt,
            pending = pending,
            showIntentRequestCode = requestCode + 1_000
        )
    }

    private fun pendingIntent(
        context: Context,
        requestCode: Int,
        prayer: String? = null,
        dayKey: String? = null,
        kind: String? = null
    ): PendingIntent {
        val intent = Intent(context, PrayerCheckReminderReceiver::class.java).apply {
            prayer?.let { putExtra(PrayerCheckReminderReceiver.EXTRA_PRAYER, it) }
            dayKey?.let { putExtra(PrayerCheckReminderReceiver.EXTRA_DAY_KEY, it) }
            kind?.let { putExtra(PrayerCheckReminderReceiver.EXTRA_KIND, it) }
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    const val KIND_PRAYER = "prayer_check"
    const val KIND_WRAP_UP = "wrap_up"
}

private object NotificationManagerCompatHelper {
    fun areEnabled(context: Context): Boolean =
        androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
}
