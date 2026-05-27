package app.kamy.qalbuApp.domain.model

/**
 * Mirrors iOS Domain/Enums/PrayerType.swift.
 */
enum class PrayerType(val aladhanKey: String) {
    FAJR("Fajr"),
    SUNRISE("Sunrise"),
    DHUHR("Dhuhr"),
    ASR("Asr"),
    MAGHRIB("Maghrib"),
    ISHA("Isha");

    companion object {
        fun fromAladhanKey(key: String): PrayerType? = entries.firstOrNull { it.aladhanKey == key }
    }
}
