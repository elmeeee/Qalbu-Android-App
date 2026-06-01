package app.kamy.qalbuApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import app.kamy.qalbuApp.MainActivity

object ExactAlarmScheduler {

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    fun schedule(
        context: Context,
        triggerAtMillis: Long,
        pending: PendingIntent,
        showIntentRequestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
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
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pending
            )
        }.onFailure {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }
}
