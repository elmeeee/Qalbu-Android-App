package app.kamy.qalbuApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class QuranReadingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        runCatching { QuranReadingReminderScheduler.showNotification(appContext) }
        runCatching { QuranReadingReminderScheduler.reschedule(appContext) }
    }
}
