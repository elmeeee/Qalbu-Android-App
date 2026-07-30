package app.kamy.saatApp.core.locale

import androidx.annotation.StringRes
import app.kamy.saatApp.R

enum class AppLanguage(
    val tag: String,
    val apiCode: String,
    @StringRes val labelRes: Int
) {
    ENGLISH("en", "en", R.string.language_english),
    INDONESIAN("id", "id", R.string.language_indonesian),
    MALAY("ms", "ms", R.string.language_malay);

    /** Language name for AI prompts (English output instruction). */
    val aiPromptLanguage: String
        get() = when (this) {
            ENGLISH -> "English"
            INDONESIAN -> "Indonesian (Bahasa Indonesia)"
            MALAY -> "Malay (Bahasa Melayu)"
        }

    /** Tone hint so AI drafts sound like a real person, not a bot. */
    val aiToneHint: String
        get() = when (this) {
            ENGLISH ->
                "Everyday English — warm, honest, like a note to a close friend. Not preachy or polished."
            INDONESIAN ->
                "Bahasa Indonesia sehari-hari — hangat, jujur, seperti chat WA ke teman dekat. Jangan formal, kaku, atau seperti khutbah."
            MALAY ->
                "Bahasa Melayu harian — mesra, jujur, seperti mesej WA kepada rakan rapat. Jangan formal, kaku, atau seperti khutbah."
        }

    /** Hard rule: no English leakage when not English. */
    val aiLanguageRule: String
        get() = when (this) {
            ENGLISH -> "Write 100% in English."
            INDONESIAN -> "WAJIB 100% Bahasa Indonesia. Dilarang pakai kata/frasa English (kecuali nama surah/Allah)."
            MALAY -> "WAJIB 100% Bahasa Melayu. Dilarang guna perkataan/frasa English (kecuali nama surah/Allah)."
        }

    companion object {
        fun fromTag(tag: String?): AppLanguage {
            if (!tag.isNullOrBlank()) {
                entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) }?.let { return it }
            }
            val sysLocale = java.util.Locale.getDefault()
            val sysLang = sysLocale.language.lowercase()
            val sysCountry = sysLocale.country.uppercase()
            return when {
                sysLang == "id" || sysLang == "in" || sysCountry == "ID" -> INDONESIAN
                sysLang == "ms" || sysCountry == "MY" -> MALAY
                else -> INDONESIAN
            }
        }
    }
}
