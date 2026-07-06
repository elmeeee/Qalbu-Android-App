package app.kamy.saatApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import app.kamy.saatApp.MainActivity

object ExactAlarmScheduler {

    internal fun shouldUseExactScheduling(sdkInt: Int, canScheduleExact: Boolean): Boolean {
        return sdkInt < Build.VERSION_CODES.S || canScheduleExact
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return shouldUseExactScheduling(Build.VERSION.SDK_INT, alarmManager.canScheduleExactAlarms())
    }

    fun schedule(
        context: Context,
        triggerAtMillis: Long,
        pending: PendingIntent,
        showIntentRequestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        runCatching {
            if (!shouldUseExactScheduling(Build.VERSION.SDK_INT, alarmManager.canScheduleExactAlarms())) {
                scheduleInexact(alarmManager, triggerAtMillis, pending)
                return
            }
            val showIntent = PendingIntent.getActivity(
                context,
                showIntentRequestCode,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
                pending
            )
        }.onFailure {
            scheduleInexact(alarmManager, triggerAtMillis, pending)
        }
    }

    private fun scheduleInexact(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pending: PendingIntent
    ) {
        runCatching {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }.onFailure {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                60_000L,
                pending
            )
        }
    }
}
