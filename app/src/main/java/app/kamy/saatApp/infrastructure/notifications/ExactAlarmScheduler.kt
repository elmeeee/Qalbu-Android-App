package app.kamy.saatApp.infrastructure.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import app.kamy.saatApp.MainActivity

object ExactAlarmScheduler {

    private const val TAG = "ExactAlarmScheduler"

    internal fun shouldUseExactScheduling(sdkInt: Int, canScheduleExact: Boolean): Boolean {
        return sdkInt < Build.VERSION_CODES.S || canScheduleExact
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        // API < 31: exact alarms are always allowed — no permission check needed.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (alarmManager.canScheduleExactAlarms()) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    fun schedule(
        context: Context,
        triggerAtMillis: Long,
        pending: PendingIntent,
        showIntentRequestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        runCatching {
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
        }.onFailure { e ->
            Log.w(TAG, "setAlarmClock failed, falling back to setExactAndAllowWhileIdle", e)
            scheduleExactAndAllowWhileIdle(context, triggerAtMillis, pending)
        }
    }

    fun scheduleExactAndAllowWhileIdle(
        context: Context,
        triggerAtMillis: Long,
        pending: PendingIntent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        runCatching {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }.onFailure { e ->
            Log.w(TAG, "setExactAndAllowWhileIdle failed, falling back to inexact", e)
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
        }.onFailure { e ->
            Log.w(TAG, "set() failed, last resort setWindow", e)
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                15_000L,
                pending
            )
        }
    }
}
