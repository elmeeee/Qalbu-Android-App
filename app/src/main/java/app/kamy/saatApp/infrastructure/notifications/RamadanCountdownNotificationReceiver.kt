package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RamadanCountdownNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val force = intent?.getBooleanExtra("force", false) ?: false
        android.util.Log.d("RamadanCountdown", "Receiver onReceive triggered (force=$force)")
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                RamadanCountdownNotificationScheduler.showNotification(appContext, force = force)
            } catch (e: Throwable) {
                android.util.Log.e("RamadanCountdown", "Receiver failed", e)
            } finally {
                runCatching { RamadanCountdownNotificationScheduler.scheduleNext(appContext) }
                pendingResult.finish()
            }
        }
    }
}
