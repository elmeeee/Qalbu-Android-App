package app.kamy.qalbuApp

import android.app.Application
import android.content.Context
import app.kamy.qalbuApp.core.locale.AppLocale
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.qalbuApp.infrastructure.notifications.NotificationChannels
import app.kamy.qalbuApp.infrastructure.notifications.QuranReadingReminderScheduler
import app.kamy.qalbuApp.infrastructure.notifications.PrayerCheckReminderScheduler
import app.kamy.qalbuApp.infrastructure.notifications.PrayerNotificationCoordinator
import app.kamy.qalbuApp.infrastructure.network.NetworkDebugger
import app.kamy.qalbuApp.infrastructure.preferences.AppLanguageStore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QalbuApplication : Application() {

    override fun attachBaseContext(base: Context) {
        val language = AppLanguageStore.from(base).current()
        super.attachBaseContext(AppLocale.wrap(base, language))
    }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureAll(this)
        NetworkDebugger.install(this)
        runCatching { DailyVerseNotificationScheduler.reschedule(this) }
        runCatching { PrayerNotificationCoordinator.rescheduleFromCache(this) }
        runCatching { PrayerCheckReminderScheduler.reschedule(this) }
        runCatching { QuranReadingReminderScheduler.reschedule(this) }
    }
}
