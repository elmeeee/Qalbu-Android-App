package app.kamy.qalbuApp.infrastructure.notifications

import android.content.Context
import app.kamy.qalbuApp.infrastructure.preferences.PrayerNotificationPreferencesStore
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
        longitude: Double
    ) {
        val appContext = context.applicationContext
        PrayerScheduleCache.save(appContext, bundle, latitude, longitude)
        scheduleAsync(appContext)
    }

    fun rescheduleFromCache(context: Context) {
        scheduleAsync(context.applicationContext)
    }

    private fun scheduleAsync(appContext: Context) {
        scope.launch {
            runCatching { rescheduleBlocking(appContext) }
        }
    }

    private fun rescheduleBlocking(appContext: Context) {
        val bundle = PrayerScheduleCache.load(appContext) ?: return
        val options = PrayerNotificationPreferencesStore.from(appContext).scheduleOptions()
        PrayerNotificationScheduler.reschedule(appContext, bundle, options)
    }
}
