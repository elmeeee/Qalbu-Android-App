package app.kamy.saatApp.infrastructure.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PrayerAlarmRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in RESCHEDULE_ACTIONS) return
        val appContext = context.applicationContext
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                // Android 15+ contract: this path ONLY reschedules AlarmManager alarms.
                // No foreground service (of any restricted type) is started here.
                rescheduleAlarms(appContext)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        // Android 15+ restriction: BOOT_COMPLETED receivers must NOT start restricted
        // foreground service types (e.g. mediaPlayback, camera, microphone).
        // This receiver ONLY schedules AlarmManager alarms — no foreground service is started here.
        // BOOT_COMPLETED is also isolated in a separate <receiver> block in AndroidManifest.xml
        // to make this boundary verifiable by Play's static analysis scanner.
        private val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.TIME_SET",
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )

        /**
         * Reschedules all AlarmManager-based alarms from cached data.
         * Safe to call from BOOT_COMPLETED — does not start any foreground service.
         */
        suspend fun rescheduleAlarms(appContext: Context) {
            if (PrayerScheduleCache.loadCoordinates(appContext) != null) {
                runCatching { PrayerScheduleRefresher.refresh(appContext) }
            } else {
                PrayerNotificationCoordinator.rescheduleFromCache(appContext)
            }
            runCatching { DailyVerseNotificationScheduler.reschedule(appContext) }
            runCatching { PrayerCheckReminderScheduler.reschedule(appContext) }
            runCatching {
                app.kamy.saatApp.infrastructure.preferences.SurahReminderStore
                    .from(appContext)
                    .let { store -> store.rescheduleAlarms(store.getReminders()) }
            }
        }
    }
}
