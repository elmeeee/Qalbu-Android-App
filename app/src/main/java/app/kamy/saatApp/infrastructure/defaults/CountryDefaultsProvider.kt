package app.kamy.saatApp.infrastructure.defaults

import android.content.Context
import android.telephony.TelephonyManager
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod

/**
 * Smart defaults for Quran translation and prayer calculation method based on
 * the user's country (ISO 3166-1 alpha-2).
 *
 * Country detection uses SIM country code first, falling back to network country
 * code. Neither requires [android.Manifest.permission.READ_PHONE_STATE] on API 30+.
 *
 * | Country    | Code | Translation           | Prayer Method |
 * |------------|------|-----------------------|---------------|
 * | Indonesia  | ID   | Kemenag RI            | KEMENAG       |
 * | Malaysia   | MY   | DBP                   | JAKIM         |
 * | Singapore  | SG   | Sahih International   | MUIS          |
 * | Brunei     | BN   | Sahih International   | BRUNEI (MUIB) |
 * | Others     | *    | Sahih International   | MWL           |
 */
data class CountryDefaults(
    val translationId: Int,
    val translationName: String,
    val prayerMethod: PrayerCalculationMethod
) {
    val quranTranslation: String get() = translationName
    val prayerCalculationMethod: String get() = prayerMethod.rawValue
}

object CountryDefaultsProvider {

    /**
     * Detects the device's country and returns matching defaults.
     * Safe to call on devices without telephony (tablets, WiFi-only) —
     * returns the generic fallback.
     */
    fun detect(context: Context): CountryDefaults {
        val countryCode = detectCountryCode(context)
        return defaultsForCountryCode(countryCode)
    }

    /**
     * Pure mapping function — testable without Android dependencies.
     */
    fun defaultsForCountryCode(code: String?): CountryDefaults {
        val upper = code?.uppercase()
        val translationId = translationIdForCountry(upper)
        val translationName = LocalQuranConfig.translations
            .firstOrNull { it.id == translationId }
            ?.let { LocalQuranConfig.translationDisplayLabel(it) }
            ?: "Sahih International"
        val prayerMethod = PrayerCalculationMethod.forCountryCode(upper ?: "")
        return CountryDefaults(
            translationId = translationId,
            translationName = translationName,
            prayerMethod = prayerMethod
        )
    }

    private fun translationIdForCountry(code: String?): Int = when (code) {
        "ID" -> LocalQuranConfig.TRANSLATION_INDONESIAN
        "MY" -> LocalQuranConfig.TRANSLATION_MALAY
        else -> LocalQuranConfig.TRANSLATION_ENGLISH
    }

    /**
     * Returns the ISO 3166-1 alpha-2 country code, preferring SIM over network.
     * Returns null if no telephony service is available (e.g. WiFi-only tablets).
     *
     * [TelephonyManager.getSimCountryIso] and [TelephonyManager.getNetworkCountryIso]
     * do NOT require READ_PHONE_STATE permission on API 30+.
     */
    private fun detectCountryCode(context: Context): String? =
        runCatching {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                ?: return@runCatching null
            val sim = tm.simCountryIso?.takeIf { it.isNotBlank() }
            sim ?: tm.networkCountryIso?.takeIf { it.isNotBlank() }
        }.getOrNull()
}
