package app.kamy.qalbuApp.domain.model

enum class PrayerType(val aladhanKey: String) {
    FAJR("Fajr"),
    SUNRISE("Sunrise"),
    DHUHR("Dhuhr"),
    ASR("Asr"),
    MAGHRIB("Maghrib"),
    ISHA("Isha");

    companion object {
        val ADZAN_NOTIFICATION_PRAYERS = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)

        fun fromAladhanKey(key: String): PrayerType? = entries.firstOrNull { it.aladhanKey == key }
    }
}
