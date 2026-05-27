package app.kamy.qalbuApp

import android.app.Application
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QalbuApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DailyVerseNotificationScheduler.reschedule(this)
    }
}
