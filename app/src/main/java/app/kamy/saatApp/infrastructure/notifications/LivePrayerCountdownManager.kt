package app.kamy.saatApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.view.View
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

    private const val PRAYER_ARRIVED_WINDOW_MS = 15 * 60 * 1000L // 15 mins "Saatnya Shalat"
    private const val PRAYER_FINISHED_WINDOW_MS = 45 * 60 * 1000L // 45 mins "Shalat selesai"

    fun update(context: Context) {
        val appContext = context.applicationContext
        val prefs = PrayerNotificationPreferencesStore.from(appContext)
        if (!prefs.isLiveCountdownEnabled()) {
            cancel(appContext)
            return
        }

        val bundle = PrayerScheduleCache.load(appContext)
        val cachedDay = PrayerDayCache.load(appContext)

        val rawPrayers: List<PrayerNotificationItem> = when {
            bundle != null && bundle.adzanPrayers.isNotEmpty() -> bundle.adzanPrayers
            cachedDay != null && cachedDay.timings.isNotEmpty() -> {
                cachedDay.timings.map { entry ->
                    PrayerNotificationItem(entry.type.aladhanKey, entry.date.time)
                }
            }
            else -> emptyList()
        }

        if (rawPrayers.isEmpty()) {
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

        val now = System.currentTimeMillis()
        val prayers = rawPrayers.sortedBy { it.fireAtMillis }

        val meta = PrayerScheduleCache.loadMeta(appContext)
        val hijriLabel = meta?.hijriLabel
            ?: cachedDay?.hijriLabel
            ?: KhgtWidgetCache.hijriLabel(appContext)
            ?: ""
        val isRamadan = hijriLabel.contains("Ramadan", ignoreCase = true)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Find current active window or next prayer
        // 1. Check if any prayer just arrived (within 15 minutes)
        val arrivedPrayer = prayers.firstOrNull { prayer ->
            now >= prayer.fireAtMillis && now < (prayer.fireAtMillis + PRAYER_ARRIVED_WINDOW_MS)
        }

        // 2. Check if recently finished prayer (between 15 min and 45 min after prayer)
        val finishedPrayer = if (arrivedPrayer == null) {
            prayers.firstOrNull { prayer ->
                now >= (prayer.fireAtMillis + PRAYER_ARRIVED_WINDOW_MS) &&
                        now < (prayer.fireAtMillis + PRAYER_FINISHED_WINDOW_MS)
            }
        } else null

        // 3. Find next upcoming prayer
        val nextPrayer = prayers.firstOrNull { it.fireAtMillis > now }
            ?: prayers.first().let { first ->
                first.copy(fireAtMillis = first.fireAtMillis + 24 * 60 * 60 * 1000L)
            }

        // Find previous prayer for progress calculation
        val prevPrayer = prayers.lastOrNull { it.fireAtMillis <= now }
            ?: prayers.last().let { last ->
                last.copy(fireAtMillis = last.fireAtMillis - 24 * 60 * 60 * 1000L)
            }

        val totalSpan = (nextPrayer.fireAtMillis - prevPrayer.fireAtMillis).coerceAtLeast(1L)
        val elapsed = (now - prevPrayer.fireAtMillis).coerceIn(0L, totalSpan)
        val progressPercent = ((elapsed * 100) / totalSpan).toInt().coerceIn(0, 100)

        val nextPrayerType = PrayerType.fromAladhanKey(nextPrayer.name) ?: PrayerType.FAJR
        val nextPrayerName = prayerLabel(localizedContext, nextPrayerType)
        val nextPrayerTimeStr = timeFormat.format(Date(nextPrayer.fireAtMillis))

        val timeRemainingMs = (nextPrayer.fireAtMillis - now).coerceAtLeast(0L)
        val chronometerBase = android.os.SystemClock.elapsedRealtime() + timeRemainingMs

        val openIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val expandedView = RemoteViews(appContext.packageName, R.layout.notification_prayer_live_countdown)
        val collapsedView = RemoteViews(appContext.packageName, R.layout.notification_prayer_live_countdown_collapsed)

        var nextAlarmTriggerMillis = nextPrayer.fireAtMillis

        when {
            // STATE 2: WAKTU SHALAT TIBA (Saatnya Shalat)
            arrivedPrayer != null -> {
                val prayerType = PrayerType.fromAladhanKey(arrivedPrayer.name) ?: PrayerType.FAJR
                val prayerName = prayerLabel(localizedContext, prayerType)
                val prayerTimeStr = timeFormat.format(Date(arrivedPrayer.fireAtMillis))

                // Expanded View
                expandedView.setImageViewResource(R.id.iv_live_icon, R.drawable.ic_live_bell)
                expandedView.setViewVisibility(R.id.container_state_countdown, View.GONE)
                expandedView.setViewVisibility(R.id.container_state_arrived, View.VISIBLE)
                expandedView.setViewVisibility(R.id.container_state_finished, View.GONE)
                expandedView.setViewVisibility(R.id.container_progress_full, View.GONE)
                expandedView.setViewVisibility(R.id.container_arrived_button, View.VISIBLE)
                expandedView.setViewVisibility(R.id.container_progress_with_countdown, View.GONE)

                expandedView.setTextViewText(R.id.tv_arrived_title, localizedContext.getString(R.string.live_countdown_prayer_time_arrived))
                expandedView.setTextViewText(R.id.tv_arrived_prayer_name, prayerName)
                expandedView.setTextViewText(R.id.tv_arrived_prayer_time, prayerTimeStr)
                expandedView.setTextViewText(R.id.btn_open_app, localizedContext.getString(R.string.live_countdown_open_app))
                expandedView.setOnClickPendingIntent(R.id.btn_open_app, pendingIntent)

                // Collapsed View
                collapsedView.setImageViewResource(R.id.iv_collapsed_icon, R.drawable.ic_live_bell)
                collapsedView.setTextViewText(R.id.tv_collapsed_title, localizedContext.getString(R.string.live_countdown_prayer_time_arrived))
                collapsedView.setTextViewText(R.id.tv_collapsed_subtitle, "$prayerName • $prayerTimeStr")
                collapsedView.setChronometer(R.id.chronometer_countdown_collapsed, chronometerBase, null, true)

                nextAlarmTriggerMillis = arrivedPrayer.fireAtMillis + PRAYER_ARRIVED_WINDOW_MS + 500L
            }

            // STATE 3 & STATE 5: SETELAH SHALAT / SETELAH IFTAR (Selesai)
            finishedPrayer != null -> {
                val prayerType = PrayerType.fromAladhanKey(finishedPrayer.name) ?: PrayerType.FAJR
                val isIftar = isRamadan && prayerType == PrayerType.MAGHRIB

                val iconRes = if (isIftar) R.drawable.ic_live_ramadan else R.drawable.ic_live_check
                val finishedTitle = if (isIftar) {
                    localizedContext.getString(R.string.live_countdown_iftar_finished)
                } else {
                    val finishedName = prayerLabel(localizedContext, prayerType)
                    localizedContext.getString(R.string.live_countdown_prayer_finished_format, finishedName)
                }

                // Expanded View
                expandedView.setImageViewResource(R.id.iv_live_icon, iconRes)
                expandedView.setViewVisibility(R.id.container_state_countdown, View.GONE)
                expandedView.setViewVisibility(R.id.container_state_arrived, View.GONE)
                expandedView.setViewVisibility(R.id.container_state_finished, View.VISIBLE)
                expandedView.setViewVisibility(R.id.container_progress_full, View.GONE)
                expandedView.setViewVisibility(R.id.container_arrived_button, View.GONE)
                expandedView.setViewVisibility(R.id.container_progress_with_countdown, View.VISIBLE)

                expandedView.setTextViewText(R.id.tv_finished_title, finishedTitle)
                expandedView.setTextViewText(R.id.tv_finished_next_label, localizedContext.getString(R.string.live_countdown_next_label))
                expandedView.setTextViewText(R.id.tv_finished_next_info, "$nextPrayerName • $nextPrayerTimeStr")
                expandedView.setProgressBar(R.id.progress_bar_split, 100, progressPercent, false)
                expandedView.setChronometer(R.id.chronometer_countdown_small, chronometerBase, null, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    expandedView.setChronometerCountDown(R.id.chronometer_countdown_small, true)
                }

                // Collapsed View
                collapsedView.setImageViewResource(R.id.iv_collapsed_icon, iconRes)
                collapsedView.setTextViewText(R.id.tv_collapsed_title, finishedTitle)
                collapsedView.setTextViewText(R.id.tv_collapsed_subtitle, "$nextPrayerName • $nextPrayerTimeStr")
                collapsedView.setChronometer(R.id.chronometer_countdown_collapsed, chronometerBase, null, true)

                nextAlarmTriggerMillis = finishedPrayer.fireAtMillis + PRAYER_FINISHED_WINDOW_MS + 500L
            }

            // STATE 4: SAAT RAMADAN (MENUJU IFTAR)
            isRamadan && nextPrayerType == PrayerType.MAGHRIB -> {
                val imsakPrayer = prayers.firstOrNull { PrayerType.fromAladhanKey(it.name) == PrayerType.FAJR }
                val imsakTimeStr = if (imsakPrayer != null) timeFormat.format(Date(imsakPrayer.fireAtMillis)) else "04:35"
                val subtitleStr = localizedContext.getString(R.string.live_countdown_iftar_imsak_format, nextPrayerTimeStr, imsakTimeStr)

                // Expanded View
                expandedView.setImageViewResource(R.id.iv_live_icon, R.drawable.ic_live_ramadan)
                expandedView.setViewVisibility(R.id.container_state_countdown, View.VISIBLE)
                expandedView.setViewVisibility(R.id.container_state_arrived, View.GONE)
                expandedView.setViewVisibility(R.id.container_state_finished, View.GONE)
                expandedView.setViewVisibility(R.id.container_progress_full, View.VISIBLE)
                expandedView.setViewVisibility(R.id.container_arrived_button, View.GONE)
                expandedView.setViewVisibility(R.id.container_progress_with_countdown, View.GONE)

                expandedView.setTextViewText(R.id.tv_countdown_title, localizedContext.getString(R.string.live_countdown_towards_iftar))
                expandedView.setChronometer(R.id.chronometer_countdown, chronometerBase, null, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    expandedView.setChronometerCountDown(R.id.chronometer_countdown, true)
                }
                expandedView.setTextViewText(R.id.tv_countdown_subtitle, subtitleStr)
                expandedView.setProgressBar(R.id.progress_bar_full, 100, progressPercent, false)

                // Collapsed View
                collapsedView.setImageViewResource(R.id.iv_collapsed_icon, R.drawable.ic_live_ramadan)
                collapsedView.setTextViewText(R.id.tv_collapsed_title, localizedContext.getString(R.string.live_countdown_towards_iftar))
                collapsedView.setTextViewText(R.id.tv_collapsed_subtitle, "Iftar • $nextPrayerTimeStr")
                collapsedView.setChronometer(R.id.chronometer_countdown_collapsed, chronometerBase, null, true)

                nextAlarmTriggerMillis = nextPrayer.fireAtMillis + 500L
            }

            // STATE 1: MENUJU WAKTU SHALAT (DEFAULT COUNTDOWN)
            else -> {
                val towardsText = localizedContext.getString(R.string.live_countdown_towards_format, nextPrayerName)
                val subtitleText = "$nextPrayerName • $nextPrayerTimeStr"

                // Expanded View
                expandedView.setImageViewResource(R.id.iv_live_icon, R.drawable.ic_live_mosque)
                expandedView.setViewVisibility(R.id.container_state_countdown, View.VISIBLE)
                expandedView.setViewVisibility(R.id.container_state_arrived, View.GONE)
                expandedView.setViewVisibility(R.id.container_state_finished, View.GONE)
                expandedView.setViewVisibility(R.id.container_progress_full, View.VISIBLE)
                expandedView.setViewVisibility(R.id.container_arrived_button, View.GONE)
                expandedView.setViewVisibility(R.id.container_progress_with_countdown, View.GONE)

                expandedView.setTextViewText(R.id.tv_countdown_title, towardsText)
                expandedView.setChronometer(R.id.chronometer_countdown, chronometerBase, null, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    expandedView.setChronometerCountDown(R.id.chronometer_countdown, true)
                }
                expandedView.setTextViewText(R.id.tv_countdown_subtitle, subtitleText)
                expandedView.setProgressBar(R.id.progress_bar_full, 100, progressPercent, false)

                // Collapsed View
                collapsedView.setImageViewResource(R.id.iv_collapsed_icon, R.drawable.ic_live_mosque)
                collapsedView.setTextViewText(R.id.tv_collapsed_title, towardsText)
                collapsedView.setTextViewText(R.id.tv_collapsed_subtitle, subtitleText)
                collapsedView.setChronometer(R.id.chronometer_countdown_collapsed, chronometerBase, null, true)

                nextAlarmTriggerMillis = nextPrayer.fireAtMillis + 500L
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            collapsedView.setChronometerCountDown(R.id.chronometer_countdown_collapsed, true)
        }

        expandedView.setOnClickPendingIntent(R.id.notification_root, pendingIntent)
        collapsedView.setOnClickPendingIntent(R.id.notification_collapsed_root, pendingIntent)

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

        scheduleNextTransitionAlarm(appContext, nextAlarmTriggerMillis)
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
