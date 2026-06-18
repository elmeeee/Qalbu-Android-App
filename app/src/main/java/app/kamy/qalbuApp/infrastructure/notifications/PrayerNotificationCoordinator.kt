package app.kamy.qalbuApp.infrastructure.notifications

import android.content.Context
import app.kamy.qalbuApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.qalbuApp.infrastructure.widget.WidgetCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object PrayerNotificationCoordinator {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun onScheduleUpdated(
        context: Context,
        bundle: PrayerScheduleBundle,
        latitude: Double,
        longitude: Double,
        meta: PrayerScheduleCache.WidgetMeta? = null
    ) {
        val appContext = context.applicationContext
        PrayerScheduleCache.save(appContext, bundle, latitude, longitude, meta)
        WidgetCoordinator.refreshAll(appContext)
        scheduleAsync(appContext, refreshIfStale = false)
    }

    fun rescheduleFromCache(context: Context) {
        scheduleAsync(context.applicationContext, refreshIfStale = true)
    }

    private fun scheduleAsync(appContext: Context, refreshIfStale: Boolean) {
        scope.launch {
            runCatching { rescheduleBlocking(appContext, refreshIfStale) }
        }
    }

    private suspend fun rescheduleBlocking(appContext: Context, refreshIfStale: Boolean) {
        if (refreshIfStale &&
            PrayerScheduleCache.isStale(appContext) &&
            PrayerScheduleCache.loadCoordinates(appContext) != null
        ) {
            PrayerScheduleRefresher.refresh(appContext)
        }
        val bundle = PrayerScheduleCache.load(appContext)
        val options = PrayerNotificationPreferencesStore.from(appContext).scheduleOptions()
        PrayerNotificationScheduler.reschedule(appContext, bundle, options)
        runCatching { PrayerCheckReminderScheduler.reschedule(appContext) }
    }
}
