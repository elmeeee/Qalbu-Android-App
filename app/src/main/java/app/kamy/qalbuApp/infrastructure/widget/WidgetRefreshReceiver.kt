package app.kamy.qalbuApp.infrastructure.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        WidgetCoordinator.refreshAll(context)
        WidgetRefreshScheduler.schedule(context)
    }
}
