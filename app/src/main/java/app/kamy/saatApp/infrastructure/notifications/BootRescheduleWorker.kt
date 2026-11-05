package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Reschedules prayer, reminder and widget alarms after a reboot.
 *
 * Runs as a regular (non-expedited) WorkManager job so that the BOOT_COMPLETED receiver
 * never starts a restricted foreground service type (Android 15+). Alarm scheduling only.
 */
class BootRescheduleWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        PrayerAlarmRescheduleReceiver.rescheduleAlarms(applicationContext)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "boot_reschedule_alarms"
    }
}
