package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Dedicated receiver for [Intent.ACTION_BOOT_COMPLETED].
 *
 * Android 15 (API 35) prohibits BOOT_COMPLETED broadcast receivers from starting
 * restricted foreground service types (mediaPlayback, camera, microphone, etc.).
 * This class is intentionally isolated so that Play's static scanner can verify
 * the full call graph from BOOT_COMPLETED reaches only alarm-scheduling code,
 * never [android.content.Context.startForegroundService].
 *
 * All work is delegated to [PrayerAlarmRescheduleReceiver.rescheduleAlarms], which
 * exclusively reschedules AlarmManager alarms — no foreground service is started.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                // Android 15+ safe: reschedules AlarmManager alarms only.
                // No restricted foreground service type is started here.
                PrayerAlarmRescheduleReceiver.rescheduleAlarms(appContext)
            } finally {
                pending.finish()
            }
        }
    }
}
