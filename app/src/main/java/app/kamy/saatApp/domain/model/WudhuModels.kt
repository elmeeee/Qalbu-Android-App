package app.kamy.saatApp.domain.model

import app.kamy.saatApp.core.locale.AppLanguage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WudhuItem(
    val id: Int,
    val arab: String? = null,
    val transliteration: String? = null,
    val indonesia: String? = null,
    val explanation: String? = null,
    val proposition: String? = null,
    val image: String? = null,
    @SerialName("transliteration_en") val transliterationEn: String? = null,
    @SerialName("indonesia_en") val indonesiaEn: String? = null,
    @SerialName("explanation_en") val explanationEn: String? = null,
    @SerialName("proposition_en") val propositionEn: String? = null,
    @SerialName("indonesia_my") val indonesiaMy: String? = null,
    @SerialName("explanation_my") val explanationMy: String? = null,
    @SerialName("proposition_my") val propositionMy: String? = null,
    val type: String? = null
)

fun WudhuItem.getLocalizedExplanation(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ENGLISH -> explanationEn?.takeIf { it.isNotBlank() } ?: explanation.orEmpty()
        AppLanguage.MALAY -> explanationMy?.takeIf { it.isNotBlank() } ?: explanation.orEmpty()
        AppLanguage.INDONESIAN -> explanation.orEmpty()
    }
}

fun WudhuItem.getLocalizedInstruction(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ENGLISH -> indonesiaEn?.takeIf { it.isNotBlank() } ?: indonesia.orEmpty()
        AppLanguage.MALAY -> indonesiaMy?.takeIf { it.isNotBlank() } ?: indonesia.orEmpty()
        AppLanguage.INDONESIAN -> indonesia.orEmpty()
    }
}

fun WudhuItem.getLocalizedTransliteration(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ENGLISH -> transliterationEn?.takeIf { it.isNotBlank() } ?: transliteration.orEmpty()
        else -> transliteration?.takeIf { it.isNotBlank() } ?: transliterationEn.orEmpty()
    }
}

fun WudhuItem.getLocalizedProposition(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ENGLISH -> propositionEn?.takeIf { it.isNotBlank() } ?: proposition.orEmpty()
        AppLanguage.MALAY -> propositionMy?.takeIf { it.isNotBlank() } ?: proposition.orEmpty()
        AppLanguage.INDONESIAN -> proposition.orEmpty()
    }
}
