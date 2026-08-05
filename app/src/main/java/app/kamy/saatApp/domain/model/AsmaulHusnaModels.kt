package app.kamy.saatApp.domain.model

import app.kamy.saatApp.core.locale.AppLanguage

data class AsmaulHusnaItem(
    val number: Int,
    val arabic: String,
    val latin: String,
    val meaningEn: String,
    val meaningId: String,
    val meaningMs: String,
    val dalilEn: String,
    val dalilId: String,
    val dalilMs: String,
    val dalilReference: String,
    val fadhilahEn: String,
    val fadhilahId: String,
    val fadhilahMs: String,
    val recommendedCount: Int = 100
) {
    fun meaning(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> meaningEn
        AppLanguage.MALAY -> meaningMs
        AppLanguage.INDONESIAN -> meaningId
    }

    fun dalil(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> dalilEn
        AppLanguage.MALAY -> dalilMs
        AppLanguage.INDONESIAN -> dalilId
    }

    fun fadhilah(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> fadhilahEn
        AppLanguage.MALAY -> fadhilahMs
        AppLanguage.INDONESIAN -> fadhilahId
    }
}
