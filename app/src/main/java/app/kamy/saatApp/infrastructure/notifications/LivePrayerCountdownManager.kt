package app.kamy.saatApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.KhgtWidgetCache
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LivePrayerCountdownManager {

    const val NOTIFICATION_ID = 9101
    private const val REFRESH_REQUEST_CODE = 9102

    fun update(context: Context) {
        val appContext = context.applicationContext
        val prefs = PrayerNotificationPreferencesStore.from(appContext)
        if (!prefs.isLiveCountdownEnabled()) {
            cancel(appContext)
            return
        }

        val bundle = PrayerScheduleCache.load(appContext)
        if (bundle == null || bundle.adzanPrayers.isEmpty()) {
            return
        }

        val lang = AppLanguageStore.from(appContext).current()
        val locale = Locale.forLanguageTag(lang.tag)
        val config = Configuration(appContext.resources.configuration).apply {
            setLocale(locale)
        }
        val localizedContext = runCatching {
            appContext.createConfigurationContext(config)
        }.getOrDefault(appContext)

        val meta = PrayerScheduleCache.loadMeta(appContext)
        val cachedDay = PrayerDayCache.load(appContext)

        val now = System.currentTimeMillis()
        val prayers = bundle.adzanPrayers.sortedBy { it.fireAtMillis }

        val nextPrayer = prayers.firstOrNull { it.fireAtMillis > now }
            ?: prayers.first().let { first ->
                first.copy(fireAtMillis = first.fireAtMillis + 24 * 60 * 60 * 1000L)
            }

        val prayerType = PrayerType.fromAladhanKey(nextPrayer.name) ?: PrayerType.FAJR
        val prayerName = prayerLabel(localizedContext, prayerType)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = timeFormat.format(Date(nextPrayer.fireAtMillis))

        val headerBadgeText = localizedContext.getString(
            R.string.live_countdown_approaching_format,
            prayerName.uppercase(locale)
        )
        val prefixText = localizedContext.getString(
            R.string.live_countdown_in_format,
            prayerName
        )

        val cityLabel = meta?.cityLabel
            ?: cachedDay?.cityName
            ?: LocationPreferencesStore.from(appContext).displayLabel()
            ?: localizedContext.getString(R.string.app_name)

        val hijriLabel = meta?.hijriLabel
            ?: cachedDay?.hijriLabel
            ?: KhgtWidgetCache.hijriLabel(appContext)

        val footer = if (!hijriLabel.isNullOrBlank()) {
            "$cityLabel • $hijriLabel"
        } else {
            cityLabel
        }

        val timeRemainingMs = (nextPrayer.fireAtMillis - now).coerceAtLeast(0L)
        val chronometerBase = android.os.SystemClock.elapsedRealtime() + timeRemainingMs

        val expandedView = RemoteViews(appContext.packageName, R.layout.notification_prayer_live_countdown).apply {
            setTextViewText(R.id.tv_header_badge, headerBadgeText)
            setTextViewText(R.id.tv_prayer_time, timeString)
            setTextViewText(R.id.tv_countdown_prefix, prefixText)
            setChronometer(R.id.chronometer_countdown, chronometerBase, null, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setChronometerCountDown(R.id.chronometer_countdown, true)
            }
            setTextViewText(R.id.tv_location_hijri, footer)
        }

        val collapsedView = RemoteViews(appContext.packageName, R.layout.notification_prayer_live_countdown_collapsed).apply {
            setTextViewText(R.id.tv_collapsed_title, "$prayerName • $timeString")
            setTextViewText(R.id.tv_collapsed_subtitle, footer)
            setChronometer(R.id.chronometer_countdown_collapsed, chronometerBase, null, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setChronometerCountDown(R.id.chronometer_countdown_collapsed, true)
            }
        }

        val openIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationChannels.ensureAll(appContext)

        val notification = NotificationCompat.Builder(appContext, NotificationChannels.LIVE_PRAYER_COUNTDOWN)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        scheduleNextTransitionAlarm(appContext, nextPrayer.fireAtMillis + 1500L)
    }

    fun cancel(context: Context) {
        val appContext = context.applicationContext
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        cancelTransitionAlarm(appContext)
    }

    private fun scheduleNextTransitionAlarm(context: Context, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LivePrayerCountdownReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REFRESH_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelTransitionAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LivePrayerCountdownReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REFRESH_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun prayerLabel(context: Context, type: PrayerType): String = when (type) {
        PrayerType.FAJR -> context.getString(R.string.prayer_fajr)
        PrayerType.DHUHR -> context.getString(R.string.prayer_dhuhr)
        PrayerType.ASR -> context.getString(R.string.prayer_asr)
        PrayerType.MAGHRIB -> context.getString(R.string.prayer_maghrib)
        PrayerType.ISHA -> context.getString(R.string.prayer_isha)
        PrayerType.SUNRISE -> context.getString(R.string.prayer_sunrise)
    }
}
