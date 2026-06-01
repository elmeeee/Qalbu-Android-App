package app.kamy.qalbuApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.kamy.qalbuApp.MainActivity
import app.kamy.qalbuApp.R
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PrayerNotificationScheduler {

    private const val PRAYER_REQUEST_BASE = 8_000
    private const val NIGHT_REQUEST_BASE = 9_000
    private const val SUNNAH_YASIN_REQUEST = 10_001
    private const val SUNNAH_KAHF_REQUEST = 10_002
    private const val MIDNIGHT_REFRESH_REQUEST = 11_000
    private const val NOTIFICATION_ID_BASE = 8_000
    private const val DAYS_TO_SCHEDULE = 7

    fun reschedule(
        context: Context,
        bundle: PrayerScheduleBundle?,
        options: PrayerNotificationScheduleOptions
    ) {
        NotificationChannels.ensureAll(context)
        runCatching { cancelAll(context) }
        scheduleSunnahReminders(context, options)
        scheduleMidnightRefresh(context)
        if (bundle == null) return

        val now = System.currentTimeMillis()

        bundle.adzanPrayers.forEachIndexed { index, prayer ->
                if (!options.isAdzanEnabledFor(prayer.name)) return@forEachIndexed
                PrayerScheduleBuilder.upcomingOccurrences(
                    prayer.fireAtMillis,
                    now,
                    DAYS_TO_SCHEDULE
                ).forEachIndexed { offset, fireAt ->
                        scheduleOneShot(
                            context = context,
                            requestCode = PRAYER_REQUEST_BASE + index * 20 + offset,
                            fireAt = fireAt,
                            channelId = NotificationChannels.PRAYER,
                            title = prayerTitle(prayer.name, fireAt),
                            body = prayerBody(prayer.name),
                            kind = "prayer_${prayer.name}",
                            notificationId = NOTIFICATION_ID_BASE + index + offset,
                            playAdhan = true,
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
                            title = prayerTitle("Imsak", fireAt),
                            body = prayerBody("Imsak"),
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
                    scheduleOneShot(
                        context = context,
                        requestCode = NIGHT_REQUEST_BASE + codeOffset * 20 + offset,
                        fireAt = fireAt,
                        channelId = NotificationChannels.PRAYER,
                        title = division.kind.notificationTitle,
                        body = division.kind.notificationBody,
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

        if (options.yasinReminderEnabled) {
            val fire = nextWeekdayTime(
                weekday = Calendar.THURSDAY,
                hour = 20,
                minute = 0,
                from = now
            )
            scheduleOneShot(
                context = context,
                requestCode = SUNNAH_YASIN_REQUEST,
                fireAt = fire,
                channelId = NotificationChannels.SUNNAH,
                title = "📖 Read Surah Yasin",
                body = "Thursday night — a blessed time to read Surah Yasin before Jumu'ah.",
                kind = "sunnah_yasin"
            )
        }

        if (options.kahfReminderEnabled) {
            val fire = nextWeekdayTime(
                weekday = Calendar.FRIDAY,
                hour = 9,
                minute = 0,
                from = now
            )
            scheduleOneShot(
                context = context,
                requestCode = SUNNAH_KAHF_REQUEST,
                fireAt = fire,
                channelId = NotificationChannels.SUNNAH,
                title = "📖 Read Surah Al-Kahf",
                body = "It's Friday — read Surah Al-Kahf for light between this Friday and the next.",
                kind = "sunnah_kahf"
            )
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
        val nm = NotificationManagerCompat.from(context)
        (NOTIFICATION_ID_BASE until NOTIFICATION_ID_BASE + 80).forEach { nm.cancel(it) }
    }

    private fun cancelSunnah(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingAlarm(context, SUNNAH_YASIN_REQUEST))
        alarmManager.cancel(pendingAlarm(context, SUNNAH_KAHF_REQUEST))
        NotificationManagerCompat.from(context).cancel(SUNNAH_YASIN_REQUEST)
        NotificationManagerCompat.from(context).cancel(SUNNAH_KAHF_REQUEST)
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        silent: Boolean = false
    ) {
        NotificationChannels.ensureAll(context)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        if (silent) {
            builder.setSilent(true)
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
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
            val now = System.currentTimeMillis()
            if (fireAt <= now + 2_000L) return@runCatching

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                putExtra(PrayerNotificationReceiver.EXTRA_CHANNEL_ID, channelId)
                putExtra(PrayerNotificationReceiver.EXTRA_TITLE, title)
                putExtra(PrayerNotificationReceiver.EXTRA_BODY, body)
                putExtra(PrayerNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(PrayerNotificationReceiver.EXTRA_KIND, kind)
                putExtra(PrayerNotificationReceiver.EXTRA_PLAY_ADHAN, playAdhan)
                putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME, prayerName)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setExactAlarm(context, alarmManager, fireAt, pending)
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
        while (cal.get(Calendar.DAY_OF_WEEK) != weekday || cal.timeInMillis <= from) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun prayerTitle(name: String, fireAtMillis: Long): String {
        val time = java.text.SimpleDateFormat("HH.mm", Locale.US).format(Date(fireAtMillis))
        val display = when (name) {
            "Fajr" -> "Fajr"
            "Dhuhr" -> "Dhuhr"
            "Asr" -> "Asr"
            "Maghrib" -> "Maghrib"
            "Isha" -> "Isha"
            "Imsak" -> "Imsak"
            else -> name
        }
        return "It's time for $display · $time"
    }

    private fun prayerBody(name: String): String = when (name) {
        "Fajr" -> "The world is still asleep. You don't have to be."
        "Dhuhr" -> "Pause. Pray. Then carry on."
        "Asr" -> "The angels are witnessing. Don't let this one pass."
        "Maghrib" -> "The sun just set. This one can't wait."
        "Isha" -> "End your day the right way."
        "Imsak" -> "Prepare for your fast. The dawn is near."
        else -> "It is now time for the $name prayer."
    }
}
