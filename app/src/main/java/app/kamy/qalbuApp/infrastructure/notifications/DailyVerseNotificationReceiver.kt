package app.kamy.qalbuApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Fired by [DailyVerseNotificationScheduler] for the daily verse alarm. */
class DailyVerseNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        DailyVerseNotificationScheduler.showNotification(context)
        DailyVerseNotificationScheduler.scheduleNext(context)
    }
}
