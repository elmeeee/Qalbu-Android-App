package app.kamy.saatApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.core.util.UserProfileUtils
import app.kamy.saatApp.domain.model.KhgtRamadanCountdown
import app.kamy.saatApp.infrastructure.local.LocalKhgtCalendar
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.RamadanCountdownPreferencesStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object RamadanCountdownNotificationScheduler {

    const val CHANNEL_ID = NotificationChannels.RAMADAN_COUNTDOWN
    const val REQUEST_CODE = 7080
    const val NOTIFICATION_ID = 7080
    private const val FALLBACK_WORK_NAME = "ramadan_countdown_fallback_work"

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RamadanCountdownEntryPoint {
        fun localKhgtCalendar(): LocalKhgtCalendar
    }

    private fun khgtCalendar(context: Context): LocalKhgtCalendar =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RamadanCountdownEntryPoint::class.java
        ).localKhgtCalendar()

    fun ensureChannel(context: Context) {
        NotificationChannels.ensureAll(context)
    }

    suspend fun resolveCountdown(context: Context): KhgtRamadanCountdown? =
        runCatching {
            khgtCalendar(context).ramadanCountdown()
        }.onFailure {
            android.util.Log.e("RamadanCountdown", "Error resolving countdown", it)
        }.getOrNull()

    fun reschedule(context: Context) {
        if (!RamadanCountdownPreferencesStore.isEnabled(context)) {
            cancel(context)
            return
        }
        if (!RamadanCountdownPreferencesStore.hasShownToday(context)) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO).launch {
                showNotification(context.applicationContext)
            }
        }
        scheduleNext(context)
        enqueueFallbackWork(context)
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
        cancelFallbackWork(context)
        dismissNotification(context)
    }

    fun enqueueFallbackWork(context: Context) {
        runCatching {
            val request = androidx.work.PeriodicWorkRequestBuilder<RamadanCountdownWorker>(
                6,
                java.util.concurrent.TimeUnit.HOURS
            ).build()
            androidx.work.WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                FALLBACK_WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    private fun cancelFallbackWork(context: Context) {
        runCatching {
            androidx.work.WorkManager.getInstance(context.applicationContext)
                .cancelUniqueWork(FALLBACK_WORK_NAME)
        }
    }

    internal fun shouldShowCatchUpNotification(
        hourNow: Int,
        minuteNow: Int,
        targetHour: Int,
        targetMinute: Int,
        hasShownToday: Boolean,
        isEnabled: Boolean
    ): Boolean {
        if (!isEnabled || hasShownToday) return false
        return hourNow > targetHour || (hourNow == targetHour && minuteNow >= targetMinute)
    }

    fun dismissNotification(context: Context) {
        runCatching {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }

    fun scheduleNext(context: Context) {
        if (!RamadanCountdownPreferencesStore.isEnabled(context)) {
            cancel(context)
            return
        }
        val hour = RamadanCountdownPreferencesStore.morningHour(context)
        val minute = RamadanCountdownPreferencesStore.morningMinute(context)
        scheduleAt(context, hour, minute)
    }

    fun scheduleAt(context: Context, hour: Int, minute: Int) {
        ensureChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))

        val trigger = nextTriggerMillis(hour, minute)
        val pending = pendingIntent(context)
        ExactAlarmScheduler.scheduleExactAndAllowWhileIdle(
            context = context,
            triggerAtMillis = trigger,
            pending = pending
        )
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, RamadanCountdownNotificationReceiver::class.java)
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
        val now = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun getLocalizedContext(context: Context): Context {
        val lang = AppLanguageStore.from(context).current()
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

    suspend fun showNotification(context: Context, force: Boolean = false) {
        android.util.Log.d("RamadanCountdown", "showNotification called (force=$force)")
        ensureChannel(context)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            android.util.Log.w("RamadanCountdown", "Notifications not enabled")
            return
        }
        if (!RamadanCountdownPreferencesStore.isEnabled(context)) {
            android.util.Log.w("RamadanCountdown", "Store not enabled")
            return
        }

        val countdown = resolveCountdown(context)
        android.util.Log.d("RamadanCountdown", "Countdown resolved: $countdown")
        if (countdown == null) return

        // If Ramadan has already arrived, dismiss and cancel all countdown notifications
        if (countdown.isRamadanNow || countdown.daysRemaining <= 0) {
            android.util.Log.d("RamadanCountdown", "Already Ramadan, dismissing")
            dismissNotification(context)
            cancel(context)
            return
        }

        // Avoid showing more than once per calendar day
        if (!force && RamadanCountdownPreferencesStore.hasShownToday(context)) {
            android.util.Log.d("RamadanCountdown", "Already shown today, skipping")
            return
        }

        val localContext = getLocalizedContext(context)
        val firstName = UserProfileUtils.getCurrentUserFirstName()
        val days = countdown.daysRemaining
        val hijriYear = countdown.hijriYear

        val displayTitle = when {
            days == 1 -> {
                if (!firstName.isNullOrBlank()) {
                    localContext.getString(R.string.kimi_ramadan_countdown_title_h1_named, firstName)
                } else {
                    localContext.getString(R.string.kimi_ramadan_countdown_title_h1)
                }
            }
            else -> {
                if (!firstName.isNullOrBlank()) {
                    localContext.getString(R.string.kimi_ramadan_countdown_title_named, days, firstName)
                } else {
                    localContext.getString(R.string.kimi_ramadan_countdown_title, days)
                }
            }
        }

        val displayBody = when {
            days == 1 -> localContext.getString(R.string.kimi_ramadan_countdown_body_h1)
            days in 2..3 -> localContext.getString(R.string.kimi_ramadan_countdown_body_h2_3, days)
            days in 4..7 -> localContext.getString(R.string.kimi_ramadan_countdown_body_h4_7)
            days in 8..15 -> localContext.getString(R.string.kimi_ramadan_countdown_body_h8_15, days)
            days in 16..30 -> localContext.getString(R.string.kimi_ramadan_countdown_body_h16_30, days)
            else -> localContext.getString(R.string.kimi_ramadan_countdown_body_general, days, hijriYear)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/raw/off_toggle_adzan")
        val vibrationPattern = longArrayOf(0, 400, 200, 400)

        val largeKimiIcon = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.kimi_thinking_11)
        }.getOrNull()

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(displayTitle)
            .setContentText(displayBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayBody))
            .setContentIntent(pendingOpen)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(vibrationPattern)

        if (largeKimiIcon != null) {
            notificationBuilder.setLargeIcon(largeKimiIcon)
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build())
        RamadanCountdownPreferencesStore.markShownToday(context)
    }
}
