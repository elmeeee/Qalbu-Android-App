package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache

object RamadanPreferencesStore {
    private const val PREFS = "saat_ramadan"
    private const val KEY_MODE_ENABLED = "mode_enabled"
    private const val KEY_TARAWIH_GOAL = "tarawih_goal"
    private const val KEY_TARAWIH_DONE_PREFIX = "tarawih_"
    private const val KEY_FASTING_DONE_PREFIX = "fasting_"
    private const val KEY_DOA_DONE_PREFIX = "doa_"
    private const val KEY_SUNNAH_DONE_PREFIX = "sunnah_"

    private const val KEY_FORCE_RAMADAN_MODE = "force_ramadan_mode"
    private const val KEY_FORCE_TEN_LAST_NIGHTS = "force_ten_last_nights"

    fun isForceRamadanEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_FORCE_RAMADAN_MODE, false)

    fun setForceRamadanEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FORCE_RAMADAN_MODE, enabled)
            .apply()
    }

    fun isForceTenLastNightsEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_FORCE_TEN_LAST_NIGHTS, false)

    fun setForceTenLastNightsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FORCE_TEN_LAST_NIGHTS, enabled)
            .apply()
    }

    fun isModeEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_MODE_ENABLED, isRamadanSeason(context))

    fun setModeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MODE_ENABLED, enabled)
            .apply()
    }

    fun isRamadanSeason(context: Context): Boolean {
        val label = PrayerDayCache.load(context)?.hijriLabel ?: return false
        return label.contains("ramadan", ignoreCase = true) ||
            label.contains("ramadhan", ignoreCase = true)
    }

    fun tarawihGoal(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_TARAWIH_GOAL, 8)
            .coerceIn(4, 20)

    fun setTarawihGoal(context: Context, rakah: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TARAWIH_GOAL, rakah.coerceIn(4, 20))
            .apply()
    }

    fun isTarawihDone(context: Context, dayKey: String = PrayerTrackerStore.todayKey()): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_TARAWIH_DONE_PREFIX + dayKey, false)

    fun setTarawihDone(context: Context, done: Boolean, dayKey: String = PrayerTrackerStore.todayKey()) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_TARAWIH_DONE_PREFIX + dayKey, done)
            .apply()
    }

    fun toggleTarawihDone(context: Context): Boolean {
        val day = PrayerTrackerStore.todayKey()
        val next = !isTarawihDone(context, day)
        setTarawihDone(context, next, day)
        return next
    }

    fun isFastingDone(context: Context, dayKey: String = PrayerTrackerStore.todayKey()): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_FASTING_DONE_PREFIX + dayKey, true)

    fun setFastingDone(context: Context, done: Boolean, dayKey: String = PrayerTrackerStore.todayKey()) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FASTING_DONE_PREFIX + dayKey, done)
            .apply()
    }

    fun toggleFastingDone(context: Context): Boolean {
        val day = PrayerTrackerStore.todayKey()
        val next = !isFastingDone(context, day)
        setFastingDone(context, next, day)
        return next
    }

    fun isDoaDone(context: Context, dayKey: String = PrayerTrackerStore.todayKey()): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DOA_DONE_PREFIX + dayKey, false)

    fun setDoaDone(context: Context, done: Boolean, dayKey: String = PrayerTrackerStore.todayKey()) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DOA_DONE_PREFIX + dayKey, done)
            .apply()
    }

    fun toggleDoaDone(context: Context): Boolean {
        val day = PrayerTrackerStore.todayKey()
        val next = !isDoaDone(context, day)
        setDoaDone(context, next, day)
        return next
    }

    fun isSunnahDone(context: Context, dayKey: String = PrayerTrackerStore.todayKey()): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SUNNAH_DONE_PREFIX + dayKey, false)

    fun setSunnahDone(context: Context, done: Boolean, dayKey: String = PrayerTrackerStore.todayKey()) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SUNNAH_DONE_PREFIX + dayKey, done)
            .apply()
    }

    fun toggleSunnahDone(context: Context): Boolean {
        val day = PrayerTrackerStore.todayKey()
        val next = !isSunnahDone(context, day)
        setSunnahDone(context, next, day)
        return next
    }

    enum class FastingDayState {
        FASTED,      // Puasa (Fasted)
        NOT_FASTED,  // Tidak Puasa / Bolong (Missed / Excused - Qadha)
        UNRECORDED   // Belum diisi / Future
    }

    private const val KEY_RAMADAN_FASTING_PREFIX = "ramadan_fasting_day_"

    fun getRamadanDayFastingState(
        context: Context,
        hijriYear: Int,
        dayNumber: Int,
        currentDayNumber: Int
    ): FastingDayState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = "${KEY_RAMADAN_FASTING_PREFIX}${hijriYear}_${dayNumber}"
        if (prefs.contains(key)) {
            val saved = prefs.getString(key, null)
            if (saved != null) {
                return runCatching { FastingDayState.valueOf(saved) }.getOrDefault(FastingDayState.FASTED)
            }
        }
        return when {
            dayNumber < currentDayNumber -> FastingDayState.FASTED
            dayNumber == currentDayNumber -> {
                if (isFastingDone(context)) FastingDayState.FASTED else FastingDayState.NOT_FASTED
            }
            else -> FastingDayState.UNRECORDED
        }
    }

    fun setRamadanDayFastingState(
        context: Context,
        hijriYear: Int,
        dayNumber: Int,
        state: FastingDayState
    ) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("${KEY_RAMADAN_FASTING_PREFIX}${hijriYear}_${dayNumber}", state.name)
            .apply()
    }

    fun toggleRamadanDayFasting(
        context: Context,
        hijriYear: Int,
        dayNumber: Int,
        currentDayNumber: Int
    ): FastingDayState {
        val current = getRamadanDayFastingState(context, hijriYear, dayNumber, currentDayNumber)
        val next = when (current) {
            FastingDayState.FASTED -> FastingDayState.NOT_FASTED
            FastingDayState.NOT_FASTED -> FastingDayState.FASTED
            FastingDayState.UNRECORDED -> FastingDayState.FASTED
        }
        setRamadanDayFastingState(context, hijriYear, dayNumber, next)
        if (dayNumber == currentDayNumber) {
            setFastingDone(context, next == FastingDayState.FASTED)
        }
        return next
    }
}
