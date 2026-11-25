package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.kamy.saatApp.infrastructure.preferences.RamadanCountdownPreferencesStore
import java.util.Calendar

/**
 * Periodic WorkManager fallback for [RamadanCountdownNotificationScheduler].
 * Ensures the daily "Menuju Ramadan" notification still fires if the exact alarm
 * is deferred or killed by OEM battery restrictions.
 */
class RamadanCountdownWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val isEnabled = RamadanCountdownPreferencesStore.isEnabled(applicationContext)
            if (!isEnabled) {
                RamadanCountdownNotificationScheduler.cancel(applicationContext)
                return Result.success()
            }

            val now = Calendar.getInstance()
            val shouldShow = RamadanCountdownNotificationScheduler.shouldShowCatchUpNotification(
                hourNow = now.get(Calendar.HOUR_OF_DAY),
                minuteNow = now.get(Calendar.MINUTE),
                targetHour = RamadanCountdownPreferencesStore.morningHour(applicationContext),
                targetMinute = RamadanCountdownPreferencesStore.morningMinute(applicationContext),
                hasShownToday = RamadanCountdownPreferencesStore.hasShownToday(applicationContext),
                isEnabled = true
            )
            if (shouldShow) {
                RamadanCountdownNotificationScheduler.showNotification(applicationContext)
            }
            RamadanCountdownNotificationScheduler.scheduleNext(applicationContext)
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}
