package app.kamy.saatApp.infrastructure.notifications

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

data class PrayerScheduleBundle(
    val adzanPrayers: List<PrayerNotificationItem>,
    val imsak: PrayerNotificationItem?,
    val nightDivisions: List<NightDivisionItem>,
    val dayKey: String
) {
    companion object
}

data class PrayerNotificationScheduleOptions(
    val enabledAdzanPrayers: Set<String>,
    val imsakEnabled: Boolean,
    val midnightEnabled: Boolean,
    val firstThirdEnabled: Boolean,
    val lastThirdEnabled: Boolean,
    val yasinReminderEnabled: Boolean,
    val kahfReminderEnabled: Boolean,
    val importantDaysReminderEnabled: Boolean,
    val adhanSoundEnabled: Boolean,
    val monThuFastReminderEnabled: Boolean,
    val dhuhaReminderEnabled: Boolean,
    val dhuhaHour: Int = 8,
    val dhuhaMinute: Int = 30,
    val tahajudHour: Int = 3,
    val tahajudMinute: Int = 30
) {
    fun isAdzanEnabledFor(prayerName: String): Boolean = prayerName in enabledAdzanPrayers
}

