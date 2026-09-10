package app.kamy.saatApp.infrastructure.preferences

import android.content.Context

enum class NightWorshipItem {
    TAHAJUD,
    DZIKIR,
    WITIR,
    PERBANYAK_DOA,
    BACA_QURAN,
    ITIKAF
}

object TenLastNightsStore {
    private const val PREFS = "saat_ten_last_nights"
    private const val KEY_WORSHIP_PREFIX = "worship_"

    fun isWorshipDone(
        context: Context,
        nightNumber: Int,
        item: NightWorshipItem,
        dayKey: String = PrayerTrackerStore.todayKey()
    ): Boolean {
        val key = "${KEY_WORSHIP_PREFIX}${nightNumber}_${item.name}_$dayKey"
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(key, false)
    }

    fun setWorshipDone(
        context: Context,
        nightNumber: Int,
        item: NightWorshipItem,
        done: Boolean,
        dayKey: String = PrayerTrackerStore.todayKey()
    ) {
        val key = "${KEY_WORSHIP_PREFIX}${nightNumber}_${item.name}_$dayKey"
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, done)
            .apply()
    }

    fun toggleWorshipDone(
        context: Context,
        nightNumber: Int,
        item: NightWorshipItem,
        dayKey: String = PrayerTrackerStore.todayKey()
    ): Boolean {
        val current = isWorshipDone(context, nightNumber, item, dayKey)
        val next = !current
        setWorshipDone(context, nightNumber, item, next, dayKey)
        return next
    }

    fun getWorshipMap(
        context: Context,
        nightNumber: Int,
        dayKey: String = PrayerTrackerStore.todayKey()
    ): Map<NightWorshipItem, Boolean> {
        return NightWorshipItem.entries.associateWith { item ->
            isWorshipDone(context, nightNumber, item, dayKey)
        }
    }
}
