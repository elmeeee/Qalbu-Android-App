package app.kamy.qalbuApp.domain.prayer

/** One row in the prayer-method picker (API name + mapped enum for timings). */
data class PrayerMethodOption(
    val aladhanId: Int,
    val apiKey: String,
    val name: String,
    val method: PrayerCalculationMethod,
    val organization: String = method.organization
)
