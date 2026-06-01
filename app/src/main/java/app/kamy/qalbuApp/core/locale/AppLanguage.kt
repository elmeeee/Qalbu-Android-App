package app.kamy.qalbuApp.core.locale

import androidx.annotation.StringRes
import app.kamy.qalbuApp.R

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

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: ENGLISH
    }
}
