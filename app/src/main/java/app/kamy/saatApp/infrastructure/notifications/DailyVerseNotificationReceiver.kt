package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DailyVerseNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val snapshot = runCatching {
                    DailyVerseNotificationScheduler.resolveSnapshot(appContext)
                }.getOrNull()
                runCatching {
                    DailyVerseNotificationScheduler.showNotification(appContext, snapshot)
                }
            } finally {
                runCatching { DailyVerseNotificationScheduler.scheduleNext(appContext) }
                pendingResult.finish()
            }
        }
    }
}
