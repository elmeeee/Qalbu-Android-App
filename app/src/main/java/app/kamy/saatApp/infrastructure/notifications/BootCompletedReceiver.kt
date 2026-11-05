package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Dedicated receiver for [Intent.ACTION_BOOT_COMPLETED].
 *
 * Android 15 (API 35) blocks BOOT_COMPLETED receivers from starting restricted foreground
 * service types. This receiver does no work itself: it only enqueues [BootRescheduleWorker],
 * a regular background job that reschedules AlarmManager alarms. No foreground service
 * is reachable from this call graph.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val request = OneTimeWorkRequestBuilder<BootRescheduleWorker>().build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            BootRescheduleWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
