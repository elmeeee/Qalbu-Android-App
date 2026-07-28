package app.kamy.saatApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.di.PrayerRefreshEntryPoint
import app.kamy.saatApp.infrastructure.audio.AdhanStopReceiver
import dagger.hilt.android.EntryPointAccessors
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PrayerNotificationScheduler {

    private const val PRAYER_REQUEST_BASE = 8_000
    private const val NIGHT_REQUEST_BASE = 9_000
    private const val SUNNAH_YASIN_REQUEST = 10_000
    private const val SUNNAH_KAHF_REQUEST = 10_100
    private const val FAST_MON_REQUEST = 10_300
    private const val FAST_THU_REQUEST = 10_350
    private const val DHUHA_REQUEST_BASE = 10_400
    private const val MIDNIGHT_REFRESH_REQUEST = 11_000
    private const val IMPORTANT_DAYS_REQUEST_BASE = 12_000
    private const val IMPORTANT_DAYS_ID_BASE = 12_000
    private const val NOTIFICATION_ID_BASE = 8_000
    private const val DAYS_TO_SCHEDULE = 7
    private const val SUNNAH_WEEKS_TO_SCHEDULE = 8


    suspend fun reschedule(
        context: Context,
        bundle: PrayerScheduleBundle?,
        options: PrayerNotificationScheduleOptions
    ) {
        NotificationChannels.ensureAll(context)
        runCatching { cancelAll(context) }
        scheduleSunnahReminders(context, options)
        scheduleImportantDaysReminders(context, options)
        scheduleMidnightRefresh(context)
        if (bundle == null) return

        val now = System.currentTimeMillis()

        bundle.adzanPrayers.forEachIndexed { index, prayer ->
                val adhanSoundEnabledForThisPrayer = options.isAdzanEnabledFor(prayer.name)
                PrayerScheduleBuilder.upcomingOccurrences(
                    prayer.fireAtMillis,
                    now,
                    DAYS_TO_SCHEDULE
                ).forEachIndexed { offset, fireAt ->
                        scheduleOneShot(
                            context = context,
                            requestCode = PRAYER_REQUEST_BASE + index * 20 + offset,
                            fireAt = fireAt,
                            channelId = NotificationChannels.PRAYER_ALERT,
                            title = prayerTitle(context, prayer.name, fireAt),
                            body = prayerBody(context, prayer.name),
                            kind = "prayer_${prayer.name}",
                            notificationId = NOTIFICATION_ID_BASE + index * 10 + offset,
                            playAdhan = adhanSoundEnabledForThisPrayer,
                            prayerName = prayer.name
                        )
                    }
            }
 
        if (options.imsakEnabled) {
            bundle.imsak?.let { imsak ->
                PrayerScheduleBuilder.upcomingOccurrences(
                    imsak.fireAtMillis,
                    now,
                    DAYS_TO_SCHEDULE
                ).forEachIndexed { offset, fireAt ->
                        scheduleOneShot(
                            context = context,
                            requestCode = PRAYER_REQUEST_BASE + 120 + offset,
                            fireAt = fireAt,
                            channelId = NotificationChannels.PRAYER,
                            title = prayerTitle(context, "Imsak", fireAt),
                            body = prayerBody(context, "Imsak"),
                            kind = "imsak",
                            notificationId = NOTIFICATION_ID_BASE + 50 + offset
                        )
                    }
            }
        }
 
        bundle.nightDivisions.forEach { division ->
            val enabled = when (division.kind) {
                NightDivisionKind.MIDNIGHT -> options.midnightEnabled
                NightDivisionKind.FIRST_THIRD -> options.firstThirdEnabled
                NightDivisionKind.LAST_THIRD -> options.lastThirdEnabled
            }
            if (!enabled) return@forEach
            val codeOffset = division.kind.ordinal
            PrayerScheduleBuilder.upcomingOccurrences(
                division.fireAtMillis,
                now,
                DAYS_TO_SCHEDULE
            ).forEachIndexed { offset, fireAt ->
                    val titleRes = when (division.kind) {
                        NightDivisionKind.MIDNIGHT -> R.string.night_midnight_title
                        NightDivisionKind.FIRST_THIRD -> R.string.night_first_third_title
                        NightDivisionKind.LAST_THIRD -> R.string.night_last_third_title
                    }
                    val bodyRes = when (division.kind) {
                        NightDivisionKind.MIDNIGHT -> R.string.night_midnight_body
                        NightDivisionKind.FIRST_THIRD -> R.string.night_first_third_body
                        NightDivisionKind.LAST_THIRD -> R.string.night_last_third_body
                    }
                    val localContext = getLocalizedContext(context)
                    scheduleOneShot(
                        context = context,
                        requestCode = NIGHT_REQUEST_BASE + codeOffset * 20 + offset,
                        fireAt = fireAt,
                        channelId = NotificationChannels.PRAYER,
                        title = localContext.getString(titleRes),
                        body = localContext.getString(bodyRes),
                        kind = "night_${division.kind.name}",
                        notificationId = NOTIFICATION_ID_BASE + 60 + codeOffset + offset
                    )
                }
        }
    }

    fun scheduleMidnightRefresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, PrayerMidnightRefreshReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            MIDNIGHT_REFRESH_REQUEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(context, alarmManager, cal.timeInMillis, pending)
    }

    fun scheduleSunnahReminders(context: Context, options: PrayerNotificationScheduleOptions) {
        cancelSunnah(context)
        val now = System.currentTimeMillis()
        val localContext = getLocalizedContext(context)

        if (options.monThuFastReminderEnabled) {
            val nextSun = nextWeekdayTime(Calendar.SUNDAY, 20, 0, now)
            upcomingWeeklyOccurrences(nextSun, now, SUNNAH_WEEKS_TO_SCHEDULE).forEachIndexed { offset, fireAt ->
                scheduleOneShot(
                    context = context,
                    requestCode = FAST_MON_REQUEST + offset,
                    fireAt = fireAt,
                    channelId = NotificationChannels.SUNNAH,
                    title = localContext.getString(R.string.sunnah_mon_fast_title),
                    body = localContext.getString(R.string.sunnah_mon_fast_body),
                    kind = "sunnah_mon_fast",
                    notificationId = FAST_MON_REQUEST + offset
                )
            }
            val nextWed = nextWeekdayTime(Calendar.WEDNESDAY, 20, 0, now)
            upcomingWeeklyOccurrences(nextWed, now, SUNNAH_WEEKS_TO_SCHEDULE).forEachIndexed { offset, fireAt ->
                scheduleOneShot(
                    context = context,
                    requestCode = FAST_THU_REQUEST + offset,
                    fireAt = fireAt,
                    channelId = NotificationChannels.SUNNAH,
                    title = localContext.getString(R.string.sunnah_thu_fast_title),
                    body = localContext.getString(R.string.sunnah_thu_fast_body),
                    kind = "sunnah_thu_fast",
                    notificationId = FAST_THU_REQUEST + offset
                )
            }
        }

        if (options.dhuhaReminderEnabled) {
            val firstDhuha = nextDailyTime(options.dhuhaHour, options.dhuhaMinute, now)
            upcomingDailyOccurrences(firstDhuha, now, 7).forEachIndexed { offset, fireAt ->
                scheduleOneShot(
                    context = context,
                    requestCode = DHUHA_REQUEST_BASE + offset,
                    fireAt = fireAt,
                    channelId = NotificationChannels.SUNNAH,
                    title = localContext.getString(R.string.sunnah_dhuha_title),
                    body = localContext.getString(R.string.sunnah_dhuha_body),
                    kind = "sunnah_dhuha",
                    notificationId = DHUHA_REQUEST_BASE + offset
                )
            }
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        (PRAYER_REQUEST_BASE until PRAYER_REQUEST_BASE + 200).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
        (NIGHT_REQUEST_BASE until NIGHT_REQUEST_BASE + 80).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                MIDNIGHT_REFRESH_REQUEST,
                Intent(context, PrayerMidnightRefreshReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        cancelSunnah(context)
        cancelImportantDays(context)
    }

    private fun cancelSunnah(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        (SUNNAH_YASIN_REQUEST until SUNNAH_YASIN_REQUEST + SUNNAH_WEEKS_TO_SCHEDULE).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
        (SUNNAH_KAHF_REQUEST until SUNNAH_KAHF_REQUEST + SUNNAH_WEEKS_TO_SCHEDULE).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
        (FAST_MON_REQUEST until FAST_MON_REQUEST + SUNNAH_WEEKS_TO_SCHEDULE).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
        (FAST_THU_REQUEST until FAST_THU_REQUEST + SUNNAH_WEEKS_TO_SCHEDULE).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
        (DHUHA_REQUEST_BASE until DHUHA_REQUEST_BASE + 7).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
    }

    suspend fun scheduleImportantDaysReminders(context: Context, options: PrayerNotificationScheduleOptions) {
        cancelImportantDays(context)
        if (!options.importantDaysReminderEnabled) return

        val now = System.currentTimeMillis()
        val entryPoint = runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                PrayerRefreshEntryPoint::class.java
            )
        }.getOrNull() ?: return
        
        val khgtCalendar = entryPoint.khgtCalendarRepository()

        val checkCal = Calendar.getInstance()
        for (offset in 0 until 7) {
            checkCal.timeInMillis = now
            checkCal.add(Calendar.DAY_OF_YEAR, offset)
            
            val info = runCatching { khgtCalendar.infoForDate(checkCal) }.getOrNull()
            if (info != null && info.isImportantDay) {
                val fireCal = Calendar.getInstance().apply {
                    timeInMillis = checkCal.timeInMillis
                    add(Calendar.DAY_OF_YEAR, -1) // Night before
                    set(Calendar.HOUR_OF_DAY, 20)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val compareCal = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val nowTime = compareCal.timeInMillis
                if (fireCal.timeInMillis >= nowTime) {
                    scheduleOneShot(
                        context = context,
                        requestCode = IMPORTANT_DAYS_REQUEST_BASE + offset,
                        fireAt = fireCal.timeInMillis,
                        channelId = NotificationChannels.SUNNAH,
                        title = "",
                        body = "",
                        kind = "important_day_${info.eventTitle}",
                        notificationId = IMPORTANT_DAYS_ID_BASE + offset
                    )
                }
            }
        }
    }

    private fun cancelImportantDays(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (offset in 0 until 7) {
            val code = IMPORTANT_DAYS_REQUEST_BASE + offset
            alarmManager.cancel(pendingAlarm(context, code))
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        silent: Boolean = false,
        showStopAdhan: Boolean = false,
        @RawRes adhanSoundRes: Int? = null,
        kind: String? = null,
        customPendingIntent: PendingIntent? = null
    ) {
        NotificationChannels.ensureAll(context)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val notification = PrayerNotificationBuilder.build(
            context = context,
            notificationId = notificationId,
            channelId = channelId,
            title = title,
            body = body,
            silent = silent,
            showStopAdhan = showStopAdhan,
            adhanSoundRes = adhanSoundRes,
            customPendingIntent = customPendingIntent
        )
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun scheduleOneShot(
        context: Context,
        requestCode: Int,
        fireAt: Long,
        channelId: String,
        title: String,
        body: String,
        kind: String,
        notificationId: Int = requestCode,
        playAdhan: Boolean = false,
        prayerName: String? = null
    ) {
        runCatching {
            val currentMillis = System.currentTimeMillis()
            val compareCal = Calendar.getInstance().apply {
                timeInMillis = currentMillis
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val nowTime = compareCal.timeInMillis

            val fireCal = Calendar.getInstance().apply {
                timeInMillis = fireAt
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val fireTime = fireCal.timeInMillis

            if (fireTime < nowTime) return@runCatching

            val actualFireAt = if (fireTime == nowTime) {
                currentMillis + 1000L
            } else {
                fireAt
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                putExtra(PrayerNotificationReceiver.EXTRA_CHANNEL_ID, channelId)
                putExtra(PrayerNotificationReceiver.EXTRA_TITLE, title)
                putExtra(PrayerNotificationReceiver.EXTRA_BODY, body)
                putExtra(PrayerNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(PrayerNotificationReceiver.EXTRA_KIND, kind)
                putExtra(PrayerNotificationReceiver.EXTRA_PLAY_ADHAN, playAdhan)
                putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME, prayerName)
                putExtra(PrayerNotificationReceiver.EXTRA_FIRE_AT, actualFireAt)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setExactAlarm(context, alarmManager, actualFireAt, pending)
        }
    }

    private fun setExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        fireAt: Long,
        pending: PendingIntent
    ) {
        ExactAlarmScheduler.schedule(
            context = context,
            triggerAtMillis = fireAt,
            pending = pending,
            showIntentRequestCode = SHOW_ALARM_INTENT_REQUEST
        )
    }

    private const val SHOW_ALARM_INTENT_REQUEST = 7_001

    private fun pendingAlarm(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, PrayerNotificationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextWeekdayTime(weekday: Int, hour: Int, minute: Int, from: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = from }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val compareCal = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fromTime = compareCal.timeInMillis

        while (cal.get(Calendar.DAY_OF_WEEK) != weekday || cal.timeInMillis <= fromTime) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun nextDailyTime(hour: Int, minute: Int, from: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = from }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val compareCal = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fromTime = compareCal.timeInMillis

        if (cal.timeInMillis <= fromTime) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun upcomingWeeklyOccurrences(firstFireAt: Long, now: Long, count: Int): List<Long> {
        if (count <= 0) return emptyList()
        val cal = Calendar.getInstance().apply { timeInMillis = firstFireAt }
        val compareCal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowTime = compareCal.timeInMillis
        return buildList {
            repeat(count) {
                if (cal.timeInMillis > nowTime) {
                    add(cal.timeInMillis)
                }
                cal.add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
    }

    private fun upcomingDailyOccurrences(firstFireAt: Long, now: Long, count: Int): List<Long> {
        if (count <= 0) return emptyList()
        val cal = Calendar.getInstance().apply { timeInMillis = firstFireAt }
        val compareCal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowTime = compareCal.timeInMillis
        return buildList {
            repeat(count) {
                if (cal.timeInMillis > nowTime) {
                    add(cal.timeInMillis)
                }
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun getLocalizedContext(context: Context): Context {
        val lang = app.kamy.saatApp.infrastructure.preferences.AppLanguageStore.from(context).current()
        val locale = when (lang) {
            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> java.util.Locale.US
            app.kamy.saatApp.core.locale.AppLanguage.INDONESIAN -> java.util.Locale.forLanguageTag("id-ID")
            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> java.util.Locale.forLanguageTag("ms-MY")
            else -> java.util.Locale.US
        }
        val config = android.content.res.Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        return context.createConfigurationContext(config)
    }

    private fun prayerTitle(context: Context, name: String, fireAtMillis: Long): String {
        val localContext = getLocalizedContext(context)
        val time = java.text.SimpleDateFormat("HH.mm", java.util.Locale.US).format(java.util.Date(fireAtMillis))
        val display = when (name) {
            "Fajr" -> localContext.getString(R.string.prayer_fajr)
            "Dhuhr" -> localContext.getString(R.string.prayer_dhuhr)
            "Asr" -> localContext.getString(R.string.prayer_asr)
            "Maghrib" -> localContext.getString(R.string.prayer_maghrib)
            "Isha" -> localContext.getString(R.string.prayer_isha)
            "Imsak" -> localContext.getString(R.string.prayer_imsak)
            else -> name
        }
        return localContext.getString(R.string.prayer_notif_title, display, time)
    }

    private fun prayerBody(context: Context, name: String): String {
        val localContext = getLocalizedContext(context)
        return when (name) {
            "Fajr" -> localContext.getString(R.string.prayer_body_fajr)
            "Dhuhr" -> localContext.getString(R.string.prayer_body_dhuhr)
            "Asr" -> localContext.getString(R.string.prayer_body_asr)
            "Maghrib" -> localContext.getString(R.string.prayer_body_maghrib)
            "Isha" -> localContext.getString(R.string.prayer_body_isha)
            "Imsak" -> localContext.getString(R.string.prayer_body_imsak)
            else -> {
                val display = when (name) {
                    "Fajr" -> localContext.getString(R.string.prayer_fajr)
                    "Dhuhr" -> localContext.getString(R.string.prayer_dhuhr)
                    "Asr" -> localContext.getString(R.string.prayer_asr)
                    "Maghrib" -> localContext.getString(R.string.prayer_maghrib)
                    "Isha" -> localContext.getString(R.string.prayer_isha)
                    "Imsak" -> localContext.getString(R.string.prayer_imsak)
                    else -> name
                }
                localContext.getString(R.string.prayer_body_default, display)
            }
        }
    }
}
