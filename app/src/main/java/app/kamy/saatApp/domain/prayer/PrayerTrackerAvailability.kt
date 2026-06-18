package app.kamy.saatApp.domain.prayer

import android.content.Context
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleCache
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore

object PrayerTrackerAvailability {

    fun availablePrayers(
        context: Context,
        nowMillis: Long = System.currentTimeMillis()
    ): Set<PrayerType> {
        val bundle = PrayerScheduleCache.load(context) ?: return emptySet()
        val byKey = bundle.adzanPrayers.associateBy { it.name }
        return PrayerTrackerStore.TRACKED_PRAYERS.filter { prayer ->
            val fireAt = byKey[prayer.aladhanKey]?.fireAtMillis ?: return@filter false
            nowMillis >= fireAt
        }.toSet()
    }

    fun canToggle(
        context: Context,
        prayer: PrayerType,
        isCompleted: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (isCompleted) return true
        return prayer in availablePrayers(context, nowMillis)
    }
}
