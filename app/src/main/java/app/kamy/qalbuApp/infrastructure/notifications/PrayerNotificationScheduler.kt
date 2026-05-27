package app.kamy.qalbuApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
    private const val NOTIFICATION_ID_BASE = 8_000

    fun reschedule(
        context: Context,
        bundle: PrayerScheduleBundle?,
        options: PrayerNotificationScheduleOptions
    ) {
        cancelAll(context)
        if (bundle == null) return
        NotificationChannels.ensureAll(context)
        val now = System.currentTimeMillis()

        if (options.adzanEnabled) {
            bundle.adzanPrayers.forEachIndexed { index, prayer ->
                PrayerScheduleBuilder.upcomingOccurrences(prayer.fireAtMillis, now)
                    .forEachIndexed { offset, fireAt ->
                        scheduleOneShot(
                            context = context,
                            requestCode = PRAYER_REQUEST_BASE + index * 2 + offset,
                            fireAt = fireAt,
                            channelId = NotificationChannels.PRAYER,
                            title = prayerTitle(prayer.name, fireAt),
                            body = prayerBody(prayer.name),
                            kind = "prayer_${prayer.name}",
                            notificationId = NOTIFICATION_ID_BASE + index + offset
                        )
                    }
            }
        }

        if (options.imsakEnabled) {
            bundle.imsak?.let { imsak ->
                PrayerScheduleBuilder.upcomingOccurrences(imsak.fireAtMillis, now)
                    .forEachIndexed { offset, fireAt ->
                        scheduleOneShot(
                            context = context,
                            requestCode = PRAYER_REQUEST_BASE + 50 + offset,
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
            PrayerScheduleBuilder.upcomingOccurrences(division.fireAtMillis, now)
                .forEachIndexed { offset, fireAt ->
                    scheduleOneShot(
                        context = context,
                        requestCode = NIGHT_REQUEST_BASE + codeOffset * 2 + offset,
                        fireAt = fireAt,
                        channelId = NotificationChannels.PRAYER,
                        title = division.kind.notificationTitle,
                        body = division.kind.notificationBody,
                        kind = "night_${division.kind.name}",
                        notificationId = NOTIFICATION_ID_BASE + 60 + codeOffset + offset
                    )
                }
        }

        scheduleSunnahReminders(context, options)
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
        (PRAYER_REQUEST_BASE until PRAYER_REQUEST_BASE + 60).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
        (NIGHT_REQUEST_BASE until NIGHT_REQUEST_BASE + 10).forEach { code ->
            alarmManager.cancel(pendingAlarm(context, code))
        }
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
        body: String
    ) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        NotificationChannels.ensureAll(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
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
        notificationId: Int = requestCode
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra(PrayerNotificationReceiver.EXTRA_CHANNEL_ID, channelId)
            putExtra(PrayerNotificationReceiver.EXTRA_TITLE, title)
            putExtra(PrayerNotificationReceiver.EXTRA_BODY, body)
            putExtra(PrayerNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(PrayerNotificationReceiver.EXTRA_KIND, kind)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pending)
        }
    }

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
