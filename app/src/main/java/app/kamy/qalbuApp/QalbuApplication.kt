package app.kamy.qalbuApp

import android.app.Application
import android.content.Context
import app.kamy.qalbuApp.core.locale.AppLocale
import app.kamy.qalbuApp.di.LocalQuranEntryPoint
import app.kamy.qalbuApp.infrastructure.network.NetworkDebugger
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.qalbuApp.infrastructure.notifications.NotificationChannels
import app.kamy.qalbuApp.infrastructure.notifications.PrayerCheckReminderScheduler
import app.kamy.qalbuApp.infrastructure.notifications.PrayerNotificationCoordinator
import app.kamy.qalbuApp.infrastructure.notifications.QuranReadingReminderScheduler
import app.kamy.qalbuApp.infrastructure.preferences.AppLanguageStore
import app.kamy.qalbuApp.infrastructure.preferences.OfflineDownloadStore
import app.kamy.qalbuApp.infrastructure.widget.WidgetCoordinator
import app.kamy.qalbuApp.infrastructure.widget.WidgetRefreshScheduler
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors

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
        runCatching {
            WidgetCoordinator.refreshAll(this)
            if (WidgetCoordinator.hasAnyWidgets(this)) {
                WidgetRefreshScheduler.schedule(this)
            }
        }
        markBundledQuranAvailable()
        warmUpLocalQuranDatabase()
    }

    private fun warmUpLocalQuranDatabase() {
        Executors.newSingleThreadExecutor().execute {
            runCatching {
                EntryPointAccessors.fromApplication(this, LocalQuranEntryPoint::class.java)
                    .localQuranDatabase()
                    .warmUp()
            }.onFailure {
                android.util.Log.e("QalbuApplication", "Failed to warm up local Quran DB", it)
            }
        }
    }

    private fun markBundledQuranAvailable() {
        for (chapter in 1..114) {
            OfflineDownloadStore.markChapterDownloaded(this, chapter)
        }
        OfflineDownloadStore.markCompleted(this)
    }
}
