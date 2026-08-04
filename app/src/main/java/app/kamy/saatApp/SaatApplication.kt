package app.kamy.saatApp

import android.app.Application
import android.content.Context
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.di.LocalQuranEntryPoint
import app.kamy.saatApp.infrastructure.airplane.AirplaneModeReceiver
import app.kamy.saatApp.infrastructure.defaults.SmartDefaultsInitializer
import app.kamy.saatApp.infrastructure.network.NetworkDebugger
import app.kamy.saatApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.saatApp.infrastructure.notifications.NotificationChannels
import app.kamy.saatApp.infrastructure.notifications.PrayerCheckReminderScheduler
import app.kamy.saatApp.infrastructure.notifications.PrayerNotificationCoordinator
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.OfflineDownloadStore
import app.kamy.saatApp.infrastructure.widget.WidgetCoordinator
import app.kamy.saatApp.infrastructure.widget.WidgetRefreshScheduler
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors

@HiltAndroidApp
class SaatApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        try {
            runCatching {
                val language = AppLanguageStore.from(this).current()
                val locale = java.util.Locale.forLanguageTag(language.tag)
                java.util.Locale.setDefault(locale)
                val config = android.content.res.Configuration(resources.configuration)
                config.setLocale(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)
            }
            NotificationChannels.ensureAll(this)
            AirplaneModeReceiver.register(this)
            runCatching { SmartDefaultsInitializer.applyIfNeeded(this) }
            NetworkDebugger.install(this)
            runCatching { DailyVerseNotificationScheduler.reschedule(this) }
            runCatching { PrayerNotificationCoordinator.rescheduleFromCache(this) }
            runCatching { PrayerCheckReminderScheduler.reschedule(this) }
            runCatching { app.kamy.saatApp.infrastructure.preferences.SurahReminderStore.from(this).let { store -> store.rescheduleAlarms(store.getReminders()) } }
            runCatching {
                WidgetCoordinator.refreshAll(this)
                if (WidgetCoordinator.hasAnyWidgets(this)) {
                    WidgetRefreshScheduler.schedule(this)
                }
            }
            markBundledQuranAvailable()
            warmUpLocalQuranDatabase()
        } catch (t: Throwable) {
            android.util.Log.e("SaatApplication", "Startup initialization failed", t)
        }
    }

    private fun warmUpLocalQuranDatabase() {
        Executors.newSingleThreadExecutor().execute {
            runCatching {
                EntryPointAccessors.fromApplication(this, LocalQuranEntryPoint::class.java)
                    .localQuranDatabase()
                    .warmUp()
            }.onFailure {
                android.util.Log.e("SaatApplication", "Failed to warm up local Quran DB", it)
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
