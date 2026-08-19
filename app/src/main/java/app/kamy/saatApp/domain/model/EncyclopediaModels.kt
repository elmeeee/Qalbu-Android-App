package app.kamy.saatApp.domain.model

import androidx.annotation.StringRes
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage

enum class EncyclopediaCategory(
    val id: String,
    @StringRes val labelRes: Int,
    val iconRes: Int
) {
    ALL("all", R.string.encyclopedia_cat_all, R.drawable.ic_faraidh_doc),
    PROPHETS("prophets", R.string.encyclopedia_cat_prophets, R.drawable.ic_faraidh_people),
    COMPANIONS("companions", R.string.encyclopedia_cat_companions, R.drawable.ic_faraidh_people),
    FIQH("fiqh", R.string.encyclopedia_cat_fiqh, R.drawable.ic_faraidh_terms),
    GLOSSARY("glossary", R.string.encyclopedia_cat_glossary, R.drawable.ic_dua);

    companion object {
        fun fromId(id: String): EncyclopediaCategory =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALL
    }
}

data class QuranReference(
    val surahNumber: Int,
    val surahName: String,
    val surahNameEn: String = "",
    val surahNameMs: String = "",
    val ayahRange: String,
    val verseTextAr: String = "",
    val verseTextTranslation: String = "",
    val verseTextTranslationEn: String = "",
    val verseTextTranslationMs: String = ""
) {
    fun localizedSurahName(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> surahNameEn.ifBlank { surahName }
        AppLanguage.MALAY -> surahNameMs.ifBlank { surahName }
        else -> surahName
    }

    fun localizedVerseTranslation(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> verseTextTranslationEn.ifBlank { verseTextTranslation }
        AppLanguage.MALAY -> verseTextTranslationMs.ifBlank { verseTextTranslation }
        else -> verseTextTranslation
    }
}

data class EncyclopediaTopic(
    val id: String,
    val categoryId: String,
    val title: String,
    val titleEn: String = "",
    val titleMs: String = "",
    val subtitle: String,
    val subtitleEn: String = "",
    val subtitleMs: String = "",
    val icon: String,
    val readTimeMinutes: Int,
    val summary: String,
    val summaryEn: String = "",
    val summaryMs: String = "",
    val content: String,
    val contentEn: String = "",
    val contentMs: String = "",
    val quranReferences: List<QuranReference> = emptyList()
) {
    fun localizedTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> titleEn.ifBlank { title }
        AppLanguage.MALAY -> titleMs.ifBlank { title }
        else -> title
    }

    fun localizedSubtitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> subtitleEn.ifBlank { subtitle }
        AppLanguage.MALAY -> subtitleMs.ifBlank { subtitle }
        else -> subtitle
    }

    fun localizedSummary(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> summaryEn.ifBlank { summary }
        AppLanguage.MALAY -> summaryMs.ifBlank { summary }
        else -> summary
    }

    fun localizedContent(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> contentEn.ifBlank { content }
        AppLanguage.MALAY -> contentMs.ifBlank { content }
        else -> content
    }
}

data class GlossaryTerm(
    val id: String,
    val term: String,
    val termAr: String,
    val definition: String,
    val definitionEn: String = "",
    val definitionMs: String = ""
) {
    fun localizedDefinition(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> definitionEn.ifBlank { definition }
        AppLanguage.MALAY -> definitionMs.ifBlank { definition }
        else -> definition
    }
}
