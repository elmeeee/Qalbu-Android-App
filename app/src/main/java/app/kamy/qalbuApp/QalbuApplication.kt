package app.kamy.qalbuApp

import android.app.Application
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.qalbuApp.infrastructure.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QalbuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureAll(this)
        DailyVerseNotificationScheduler.reschedule(this)
        // Prayer alarms are scheduled after location/timings load (Today screen), not here.
    }
}
