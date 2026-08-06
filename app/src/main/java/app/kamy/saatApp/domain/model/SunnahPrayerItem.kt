package app.kamy.saatApp.domain.model

import app.kamy.saatApp.core.locale.AppLanguage

data class NiatItem(
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val arabic: String,
    val latin: String,
    val translationId: String,
    val translationMs: String,
    val translationEn: String
) {
    fun title(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.MALAY -> titleMs
        AppLanguage.INDONESIAN -> titleId
    }

    fun translation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> translationEn
        AppLanguage.MALAY -> translationMs
        AppLanguage.INDONESIAN -> translationId
    }
}

data class PrayerStepItem(
    val stepNumber: Int,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val descId: String,
    val descMs: String,
    val descEn: String,
    val arabic: String? = null,
    val latin: String? = null
) {
    fun title(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.MALAY -> titleMs
        AppLanguage.INDONESIAN -> titleId
    }

    fun desc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> descEn
        AppLanguage.MALAY -> descMs
        AppLanguage.INDONESIAN -> descId
    }
}

data class SunnahPrayerItem(
    val id: String,
    val category: String, // HARIAN, MALAM, KEBUTUHAN, JENAZAH, SPECIAL
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val summaryId: String,
    val summaryMs: String,
    val summaryEn: String,
    val waktuId: String,
    val waktuMs: String,
    val waktuEn: String,
    val rakaatInfoId: String,
    val rakaatInfoMs: String,
    val rakaatInfoEn: String,
    val fadhilahId: String,
    val fadhilahMs: String,
    val fadhilahEn: String,
    val dalilHadithId: String? = null,
    val dalilHadithMs: String? = null,
    val dalilHadithEn: String? = null,
    val hadithReference: String? = null,
    val recommendedSurahsId: String? = null,
    val recommendedSurahsMs: String? = null,
    val recommendedSurahsEn: String? = null,
    val niatList: List<NiatItem>,
    val steps: List<PrayerStepItem>,
    val doaArabic: String? = null,
    val doaLatin: String? = null,
    val doaTranslationId: String? = null,
    val doaTranslationMs: String? = null,
    val doaTranslationEn: String? = null
) {
    fun title(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.MALAY -> titleMs
        AppLanguage.INDONESIAN -> titleId
    }

    fun summary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> summaryEn
        AppLanguage.MALAY -> summaryMs
        AppLanguage.INDONESIAN -> summaryId
    }

    fun waktu(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> waktuEn
        AppLanguage.MALAY -> waktuMs
        AppLanguage.INDONESIAN -> waktuId
    }

    fun rakaatInfo(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> rakaatInfoEn
        AppLanguage.MALAY -> rakaatInfoMs
        AppLanguage.INDONESIAN -> rakaatInfoId
    }

    fun fadhilah(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> fadhilahEn
        AppLanguage.MALAY -> fadhilahMs
        AppLanguage.INDONESIAN -> fadhilahId
    }

    fun dalilHadith(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> dalilHadithEn
        AppLanguage.MALAY -> dalilHadithMs
        AppLanguage.INDONESIAN -> dalilHadithId
    }

    fun recommendedSurahs(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> recommendedSurahsEn
        AppLanguage.MALAY -> recommendedSurahsMs
        AppLanguage.INDONESIAN -> recommendedSurahsId
    }

    fun doaTranslation(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> doaTranslationEn
        AppLanguage.MALAY -> doaTranslationMs
        AppLanguage.INDONESIAN -> doaTranslationId
    }
}
