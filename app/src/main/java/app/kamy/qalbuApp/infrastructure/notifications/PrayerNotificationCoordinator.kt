package app.kamy.qalbuApp.infrastructure.notifications

import android.content.Context
import app.kamy.qalbuApp.infrastructure.preferences.PrayerNotificationPreferencesStore

object PrayerNotificationCoordinator {

    fun onScheduleUpdated(context: Context, bundle: PrayerScheduleBundle) {
        PrayerScheduleCache.save(context, bundle)
        rescheduleFromCache(context)
    }

    fun rescheduleFromCache(context: Context) {
        val bundle = PrayerScheduleCache.load(context)
        val options = PrayerNotificationPreferencesStore.from(context).scheduleOptions()
        PrayerNotificationScheduler.reschedule(context, bundle, options)
    }
}
