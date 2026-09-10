package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LivePrayerCountdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        runCatching {
            LivePrayerCountdownManager.update(appContext)
        }
    }
}
