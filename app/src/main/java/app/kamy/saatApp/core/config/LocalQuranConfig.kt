package app.kamy.saatApp.core.config

import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.QFTranslation
import app.kamy.saatApp.domain.model.RecitationPayload
import app.kamy.saatApp.domain.model.RecitationTranslatedName
import app.kamy.saatApp.domain.model.TranslatedSubName

/**
 * Local Quran bundle IDs (not Quran Foundation resource IDs).
 * Maps to columns in bundled [qurannew.db].
 */
object LocalQuranConfig {
    const val TRANSLATION_INDONESIAN = 1
    const val TRANSLATION_ENGLISH = 2
    const val TRANSLATION_MALAY = 3

    const val DEFAULT_TRANSLATION_ID = TRANSLATION_INDONESIAN

    const val RECITATION_ALAFASY = 1
    const val RECITATION_HUSARY = 2
    const val RECITATION_MINSHAWI = 3
    const val RECITATION_JIBREEL = 4
    const val RECITATION_AJAMY = 5
    const val RECITATION_AYYOUB = 6
    const val RECITATION_MUAIQLY = 7
    const val RECITATION_SHAATREE = 8

    const val DEFAULT_RECITATION_ID = RECITATION_ALAFASY

    const val TAFSIR_WAJIZ_ID = "wajiz"
    const val TAFSIR_TAHLILI_ID = "tahlili"
    const val TAFSIR_JALALAYN_ID = "jalalayn"
    const val TAFSIR_RESOURCE_ID = TAFSIR_WAJIZ_ID

    const val MUROTTAL_CDN = "https://cdn.islamic.network/quran/audio/128"

    val translations: List<QFTranslation> = listOf(
        QFTranslation(
            id = TRANSLATION_INDONESIAN,
            name = "Indonesian",
            authorName = "Kementerian Agama RI",
            slug = "id",
            languageName = "indonesian",
            translatedName = TranslatedSubName("Indonesian", "indonesian")
        ),
        QFTranslation(
            id = TRANSLATION_ENGLISH,
            name = "Sahih International",
            authorName = "Sahih International",
            slug = "en",
            languageName = "english",
            translatedName = TranslatedSubName("Sahih International", "english")
        ),
        QFTranslation(
            id = TRANSLATION_MALAY,
            name = "Malay",
            authorName = "DBP",
            slug = "my",
            languageName = "malay",
            translatedName = TranslatedSubName("Malay", "malay")
        )
    )

    val recitations: List<RecitationPayload> = listOf(
        recitation(RECITATION_ALAFASY,  "Mishary Rashid Alafasy",         "alafasy"),
        recitation(RECITATION_HUSARY,   "Mahmoud Khalil Al-Husary",       "husarymujawwad"),
        recitation(RECITATION_MINSHAWI, "Muhammad Siddiq Al-Minshawi",    "minshawi"),
        recitation(RECITATION_JIBREEL,  "Muhammad Jibreel",               "muhammadjibreel"),
        recitation(RECITATION_AJAMY,    "Ahmed Al-Ajamy",                 "ahmedajamy"),
        recitation(RECITATION_AYYOUB,   "Muhammad Ayyoub",                "muhammadayyoub"),
        recitation(RECITATION_MUAIQLY,  "Maher Al-Muaiqly",              "mahermuaiqly"),
        recitation(RECITATION_SHAATREE, "Abu Bakr Ash-Shaatree",          "shaatree"),
    )

    private fun recitation(id: Int, name: String, slug: String) = RecitationPayload(
        id = id,
        reciterName = slug,
        translatedName = RecitationTranslatedName(name)
    )

    fun reciterSlug(recitationId: Int): String =
        recitations.firstOrNull { it.identifiableId == recitationId }?.reciterName ?: "alafasy"

    /** Map legacy QF translation IDs saved in prefs to local IDs. */
    fun normalizeTranslationId(savedId: Int): Int = when (savedId) {
        TRANSLATION_INDONESIAN, TRANSLATION_ENGLISH, TRANSLATION_MALAY -> savedId
        4 -> TRANSLATION_INDONESIAN
        22, 131, 33 -> TRANSLATION_INDONESIAN
        20, 84 -> TRANSLATION_ENGLISH
        else -> DEFAULT_TRANSLATION_ID
    }

    fun normalizeRecitationId(savedId: Int): Int =
        if (recitations.any { it.identifiableId == savedId }) savedId else DEFAULT_RECITATION_ID

    fun murottalUrl(recitationId: Int, globalAyahNumber: Int): String {
        val slug = reciterSlug(recitationId)
        return "$MUROTTAL_CDN/ar.$slug/$globalAyahNumber.mp3"
    }

    fun pickTransliteration(
        translationId: Int,
        transliterationId: String?,
        transliterationEn: String?
    ): String? {
        val text = when (translationId) {
            TRANSLATION_ENGLISH -> transliterationEn
            else -> transliterationId ?: transliterationEn
        }
        return text?.trim()?.takeIf { it.isNotBlank() }
    }

    fun transliterationUsesHtml(translationId: Int): Boolean =
        translationId == TRANSLATION_ENGLISH

    fun translationForAppLanguage(language: AppLanguage): QFTranslation =
        when (language) {
            AppLanguage.ENGLISH -> translations.first { it.id == TRANSLATION_ENGLISH }
            AppLanguage.MALAY -> translations.first { it.id == TRANSLATION_MALAY }
            AppLanguage.INDONESIAN -> translations.first { it.id == TRANSLATION_INDONESIAN }
        }

    fun appLanguageForTranslationId(translationId: Int): AppLanguage? =
        when (normalizeTranslationId(translationId)) {
            TRANSLATION_ENGLISH -> AppLanguage.ENGLISH
            TRANSLATION_MALAY -> AppLanguage.MALAY
            TRANSLATION_INDONESIAN -> AppLanguage.INDONESIAN
            else -> null
        }

    fun translationDisplayLabel(translation: QFTranslation): String =
        translation.authorName.ifBlank { translation.name }

    fun supportsTafsir(translationId: Int): Boolean = true
}
