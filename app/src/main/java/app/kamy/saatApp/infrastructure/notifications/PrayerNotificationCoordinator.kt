package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.saatApp.infrastructure.widget.WidgetCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object PrayerNotificationCoordinator {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Minimum interval between two full reschedule runs triggered by received alarms. */
    private const val RESCHEDULE_DEBOUNCE_MS = 60_000L
    @Volatile private var lastRescheduleFromCacheMs: Long = 0L

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
        val now = System.currentTimeMillis()
        // Debounce: if rescheduled very recently (e.g. multiple prayer alarms firing close
        // together), skip to avoid duplicate sunnah/duha/tahajud notifications.
        if (now - lastRescheduleFromCacheMs < RESCHEDULE_DEBOUNCE_MS) return
        lastRescheduleFromCacheMs = now
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
