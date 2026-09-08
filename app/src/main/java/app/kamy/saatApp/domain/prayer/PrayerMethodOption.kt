package app.kamy.saatApp.domain.prayer

data class PrayerMethodOption(
    val aladhanId: Int,
    val apiKey: String,
    val name: String,
    val method: PrayerCalculationMethod,
    val organization: String = method.organization,
    @androidx.annotation.DrawableRes val iconRes: Int = method.iconRes,
    val countryName: String = method.countryName
)

