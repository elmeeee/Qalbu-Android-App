package app.kamy.saatApp

import android.app.Application
import android.content.Context
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.di.LocalQuranEntryPoint
import app.kamy.saatApp.infrastructure.airplane.AirplaneModeReceiver
import app.kamy.saatApp.infrastructure.defaults.SmartDefaultsInitializer
import app.kamy.saatApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.saatApp.infrastructure.notifications.NotificationChannels
import app.kamy.saatApp.infrastructure.notifications.PrayerCheckReminderScheduler
import app.kamy.saatApp.infrastructure.notifications.PrayerNotificationCoordinator
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.widget.WidgetCoordinator
import app.kamy.saatApp.infrastructure.widget.WidgetRefreshScheduler
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors

@HiltAndroidApp
class SaatApplication : Application(), androidx.work.Configuration.Provider {

    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun attachBaseContext(base: Context) {
        // Set locale BEFORE any resource access so the app always starts in the
        // persisted language (or device default on first launch).
        val language = AppLanguageStore.from(base).current()
        super.attachBaseContext(AppLocale.wrap(base, language))
    }

    override fun onCreate() {
        super.onCreate()
        // WorkManager auto-init is disabled in the manifest. Initialize here so
        // a library/database failure is contained instead of crashing startup.
        runCatching {
            androidx.work.WorkManager.initialize(this, workManagerConfiguration)
        }
        runCatching {
            val language = AppLanguageStore.from(this).current()
            val locale = java.util.Locale.forLanguageTag(language.tag)
            java.util.Locale.setDefault(locale)
            val config = android.content.res.Configuration(resources.configuration)
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        runCatching { NotificationChannels.ensureAll(this) }
        runCatching { AirplaneModeReceiver.register(this) }
        runCatching { SmartDefaultsInitializer.applyIfNeeded(this) }
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
        // Warm up Quran database in background (non-blocking for first paint).
        warmUpBackgroundTasks()
    }

    private fun warmUpBackgroundTasks() {
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
}
