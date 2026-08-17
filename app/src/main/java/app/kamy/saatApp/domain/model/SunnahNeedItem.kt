package app.kamy.saatApp.domain.model

import app.kamy.saatApp.core.locale.AppLanguage

data class SunnahActionStep(
    val stepNumber: Int,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val descId: String,
    val descMs: String,
    val descEn: String,
    val arabic: String? = null,
    val latin: String? = null,
    val targetCountId: String? = null,
    val targetCountMs: String? = null,
    val targetCountEn: String? = null
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

    fun targetCount(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> targetCountEn ?: targetCountId
        AppLanguage.MALAY -> targetCountMs ?: targetCountId
        AppLanguage.INDONESIAN -> targetCountId
    }
}

data class SunnahNeedItem(
    val id: String,
    val category: String,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val subtitleId: String,
    val subtitleMs: String,
    val subtitleEn: String,
    val descriptionId: String,
    val descriptionMs: String,
    val descriptionEn: String,
    val hadithReference: String,
    val dalilArabic: String? = null,
    val dalilLatin: String? = null,
    val dalilTranslationId: String? = null,
    val dalilTranslationMs: String? = null,
    val dalilTranslationEn: String? = null,
    val actionSteps: List<SunnahActionStep> = emptyList()
) {
    fun title(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.MALAY -> titleMs
        AppLanguage.INDONESIAN -> titleId
    }

    fun subtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> subtitleEn
        AppLanguage.MALAY -> subtitleMs
        AppLanguage.INDONESIAN -> subtitleId
    }

    fun description(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> descriptionEn
        AppLanguage.MALAY -> descriptionMs
        AppLanguage.INDONESIAN -> descriptionId
    }

    fun dalilTranslation(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> dalilTranslationEn
        AppLanguage.MALAY -> dalilTranslationMs
        AppLanguage.INDONESIAN -> dalilTranslationId
    }
}
