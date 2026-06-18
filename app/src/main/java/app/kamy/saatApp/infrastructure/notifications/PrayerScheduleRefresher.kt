package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import app.kamy.saatApp.di.PrayerRefreshEntryPoint
import dagger.hilt.android.EntryPointAccessors

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
                    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    val meta = PrayerScheduleCache.WidgetMeta(
                        cityLabel = result.cityName.orEmpty(),
                        hijriLabel = result.hijriLabel,
                        gregorianLabel = result.gregorianLabel,
                        timings = result.timings.associate {
                            it.type.aladhanKey to formatter.format(it.date)
                        }
                    )
                    PrayerNotificationCoordinator.onScheduleUpdated(
                        appContext,
                        bundle,
                        coords.first,
                        coords.second,
                        meta = meta
                    )
                    return
                }
            }
        }
        PrayerNotificationCoordinator.rescheduleFromCache(appContext)
    }
}
