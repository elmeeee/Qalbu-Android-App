package app.kamy.saatApp.infrastructure.defaults

import app.kamy.saatApp.core.locale.AppLanguage
import java.util.Locale

/**
 * Detects the device's current locale and maps it to a supported [AppLanguage].
 *
 * Mapping:
 * - "id" → INDONESIAN
 * - "ms" → MALAY
 * - anything else → ENGLISH (fallback)
 */
object DeviceLanguageDetector {

    /**
     * Returns the [AppLanguage] matching the device's default locale.
     */
    fun detect(): AppLanguage = fromLanguageTag(Locale.getDefault().language)

    /**
     * Pure mapping function — testable without Android dependencies.
     */
    fun fromLanguageTag(tag: String?): AppLanguage = when (tag?.lowercase()) {
        "id" -> AppLanguage.INDONESIAN
        "ms" -> AppLanguage.MALAY
        else -> AppLanguage.ENGLISH
    }
}
