package app.kamy.qalbuApp.infrastructure.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AdhanStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationId = intent?.getIntExtra(EXTRA_NOTIFICATION_ID, -1) ?: -1
        AdhanPlaybackService.stop(context, notificationId)
    }

    companion object {
        const val ACTION_STOP = "app.kamy.qalbuApp.action.STOP_ADHAN"
        const val EXTRA_NOTIFICATION_ID = "notification_id"

        fun intent(context: Context, notificationId: Int? = null): Intent =
            Intent(context, AdhanStopReceiver::class.java).apply {
                action = ACTION_STOP
                notificationId?.let { putExtra(EXTRA_NOTIFICATION_ID, it) }
            }
    }
}
