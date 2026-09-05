package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class QuranLastReadReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            QuranLastReadReminderScheduler.evaluateAndNotify(applicationContext)
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}
