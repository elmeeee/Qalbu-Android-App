package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import android.util.Log
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.saatApp.infrastructure.widget.WidgetCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object PrayerNotificationCoordinator {

    private const val TAG = "PrayerNotifCoordinator"
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

    /**
     * Reschedules all prayer alarms from cached data.
     *
     * @param force When true, bypasses the debounce window. Use when the user
     *   explicitly changed notification preferences (toggle on/off).
     */
    fun rescheduleFromCache(context: Context, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force) {
            // Debounce: if rescheduled very recently (e.g. multiple prayer alarms firing close
            // together), skip to avoid duplicate sunnah/duha/tahajud notifications.
            if (now - lastRescheduleFromCacheMs < RESCHEDULE_DEBOUNCE_MS) {
                Log.d(TAG, "rescheduleFromCache debounced (force=$force)")
                return
            }
        }
        lastRescheduleFromCacheMs = now
        Log.d(TAG, "rescheduleFromCache executing (force=$force)")
        scheduleAsync(context.applicationContext, refreshIfStale = !force)
    }

    private fun scheduleAsync(appContext: Context, refreshIfStale: Boolean) {
        scope.launch {
            runCatching { rescheduleBlocking(appContext, refreshIfStale) }
                .onFailure { e -> Log.e(TAG, "rescheduleBlocking failed", e) }
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

