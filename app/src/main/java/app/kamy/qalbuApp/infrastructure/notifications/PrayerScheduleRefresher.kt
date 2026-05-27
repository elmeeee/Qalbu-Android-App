package app.kamy.qalbuApp.infrastructure.notifications

import android.content.Context
import app.kamy.qalbuApp.di.PrayerRefreshEntryPoint
import dagger.hilt.android.EntryPointAccessors

/**
 * Refreshes cached prayer alarms (network when coordinates are stored, otherwise rolls forward).
 */
object PrayerScheduleRefresher {

    suspend fun refresh(context: Context) {
        runCatching {
            refreshInternal(context.applicationContext)
        }
    }

    private suspend fun refreshInternal(appContext: Context) {
        val coords = PrayerScheduleCache.loadCoordinates(appContext)
        if (coords != null) {
            runCatching {
                val entryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    PrayerRefreshEntryPoint::class.java
                )
                val method = entryPoint.prayerCalculationStore().current()
                val result = entryPoint.alAdhanRepository().fetchTimings(
                    latitude = coords.first,
                    longitude = coords.second,
                    method = method
                )
                val bundle = result.scheduleBundle
                if (bundle != null) {
                    PrayerNotificationCoordinator.onScheduleUpdated(
                        appContext,
                        bundle,
                        coords.first,
                        coords.second
                    )
                    return
                }
            }
        }
        PrayerNotificationCoordinator.rescheduleFromCache(appContext)
    }
}
