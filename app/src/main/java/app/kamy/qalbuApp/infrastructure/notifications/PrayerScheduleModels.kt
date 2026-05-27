package app.kamy.qalbuApp.infrastructure.notifications

/** One local notification fire time for adzan or imsak. */
data class PrayerNotificationItem(
    val name: String,
    val fireAtMillis: Long
)

enum class NightDivisionKind(val aladhanKey: String) {
    MIDNIGHT("Midnight"),
    FIRST_THIRD("Firstthird"),
    LAST_THIRD("Lastthird");

    val notificationTitle: String
        get() = when (this) {
            MIDNIGHT -> "🌙 Midnight"
            FIRST_THIRD -> "🌃 The Night Begins"
            LAST_THIRD -> "✨ The Last Third Has Begun"
        }

    val notificationBody: String
        get() = when (this) {
            MIDNIGHT ->
                "The night is halfway through. Pray Witr before you sleep — don't let it slip away."
            FIRST_THIRD ->
                "Rest well. The last third of the night is yours — rise for what the day can't give you."
            LAST_THIRD ->
                "Allah descends to the lowest heaven. The most powerful hour of the day starts now."
        }
}

data class NightDivisionItem(
    val kind: NightDivisionKind,
    val fireAtMillis: Long
)

/** Cached prayer timetable used to (re)schedule local notifications. */
data class PrayerScheduleBundle(
    val adzanPrayers: List<PrayerNotificationItem>,
    val imsak: PrayerNotificationItem?,
    val nightDivisions: List<NightDivisionItem>,
    val dayKey: String
) {
    companion object
}

data class PrayerNotificationScheduleOptions(
    /** Al-Adhan keys enabled for adhan, e.g. "Fajr", "Dhuhr". */
    val enabledAdzanPrayers: Set<String>,
    val imsakEnabled: Boolean,
    val midnightEnabled: Boolean,
    val firstThirdEnabled: Boolean,
    val lastThirdEnabled: Boolean,
    val yasinReminderEnabled: Boolean,
    val kahfReminderEnabled: Boolean
) {
    fun isAdzanEnabledFor(prayerName: String): Boolean = prayerName in enabledAdzanPrayers
}
