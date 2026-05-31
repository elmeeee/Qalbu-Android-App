package app.kamy.qalbuApp.infrastructure.notifications

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
                val snapshot = DailyVerseNotificationScheduler.resolveSnapshot(appContext)
                DailyVerseNotificationScheduler.showNotification(appContext, snapshot)
                DailyVerseNotificationScheduler.scheduleNext(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
