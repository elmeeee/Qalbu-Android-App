package app.kamy.saatApp.domain.model

import androidx.compose.runtime.Immutable
import app.kamy.saatApp.core.locale.AppLanguage

@Immutable
enum class JamakType(val key: String) {
    JAMAK_TAQDIM_DZUHUR_ASHAR("JAMAK_TAQDIM_DZUHUR_ASHAR"),
    JAMAK_TAKHIR_DZUHUR_ASHAR("JAMAK_TAKHIR_DZUHUR_ASHAR"),
    JAMAK_TAQDIM_MAGHRIB_ISYA("JAMAK_TAQDIM_MAGHRIB_ISYA"),
    JAMAK_TAKHIR_MAGHRIB_ISYA("JAMAK_TAKHIR_MAGHRIB_ISYA"),
    QASHAR_ONLY("QASHAR_ONLY");

    companion object {
        fun fromKey(key: String): JamakType =
            entries.find { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) }
                ?: JAMAK_TAQDIM_DZUHUR_ASHAR
    }
}

@Immutable
data class JamakTypeInfo(
    val type: JamakType,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val subtitleId: String,
    val subtitleMs: String,
    val subtitleEn: String
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
}

@Immutable
data class JamakDalilItem(
    val id: String,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val arabic: String,
    val transliteration: String,
    val translationId: String,
    val translationMs: String,
    val translationEn: String,
    val referenceId: String,
    val referenceMs: String,
    val referenceEn: String,
    val explanationId: String,
    val explanationMs: String,
    val explanationEn: String
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

    fun reference(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> referenceEn
        AppLanguage.MALAY -> referenceMs
        AppLanguage.INDONESIAN -> referenceId
    }

    fun explanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> explanationEn
        AppLanguage.MALAY -> explanationMs
        AppLanguage.INDONESIAN -> explanationId
    }
}

@Immutable
data class JamakRuleItem(
    val id: String,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val descId: String,
    val descMs: String,
    val descEn: String,
    val detailId: String,
    val detailMs: String,
    val detailEn: String,
    val dalilRefId: String? = null,
    val dalilRefMs: String? = null,
    val dalilRefEn: String? = null
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

    fun detail(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> detailEn
        AppLanguage.MALAY -> detailMs
        AppLanguage.INDONESIAN -> detailId
    }

    fun dalilRef(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> dalilRefEn
        AppLanguage.MALAY -> dalilRefMs
        AppLanguage.INDONESIAN -> dalilRefId
    }
}

@Immutable
data class JamakNiatItem(
    val id: String,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val arabic: String,
    val transliteration: String,
    val translationId: String,
    val translationMs: String,
    val translationEn: String,
    val noteId: String? = null,
    val noteMs: String? = null,
    val noteEn: String? = null,
    val hadithRef: String? = null
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

    fun note(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> noteEn
        AppLanguage.MALAY -> noteMs
        AppLanguage.INDONESIAN -> noteId
    }
}

@Immutable
data class JamakStepItem(
    val stepNumber: Int,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val descId: String,
    val descMs: String,
    val descEn: String,
    val arabic: String? = null,
    val transliteration: String? = null,
    val translationId: String? = null,
    val translationMs: String? = null,
    val translationEn: String? = null,
    val tipId: String? = null,
    val tipMs: String? = null,
    val tipEn: String? = null
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

    fun translation(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> translationEn
        AppLanguage.MALAY -> translationMs
        AppLanguage.INDONESIAN -> translationId
    }

    fun tip(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> tipEn
        AppLanguage.MALAY -> tipMs
        AppLanguage.INDONESIAN -> tipId
    }
}

@Immutable
data class JamakGuideData(
    val typeInfos: List<JamakTypeInfo>,
    val dalilList: List<JamakDalilItem>,
    val rules: List<JamakRuleItem>,
    val niatList: List<JamakNiatItem>,
    val stepsMap: Map<JamakType, List<JamakStepItem>>
) {
    fun typeInfo(type: JamakType): JamakTypeInfo? =
        typeInfos.find { it.type == type }
}
