package app.kamy.saatApp.domain.model

import app.kamy.saatApp.core.locale.AppLanguage

data class JanazahTakbirStep(
    val takbirNumber: Int,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val descId: String,
    val descMs: String,
    val descEn: String,
    val arabic: String,
    val latin: String,
    val translationId: String,
    val translationMs: String,
    val translationEn: String,
    val importantNotesId: String? = null,
    val importantNotesMs: String? = null,
    val importantNotesEn: String? = null
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

    fun translation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> translationEn
        AppLanguage.MALAY -> translationMs
        AppLanguage.INDONESIAN -> translationId
    }

    fun importantNotes(lang: AppLanguage): String? = when (lang) {
        AppLanguage.ENGLISH -> importantNotesEn
        AppLanguage.MALAY -> importantNotesMs
        AppLanguage.INDONESIAN -> importantNotesId
    }
}

data class JanazahNiatItem(
    val id: String,
    val category: String,
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val subtitleId: String,
    val subtitleMs: String,
    val subtitleEn: String,
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

    fun subtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> subtitleEn
        AppLanguage.MALAY -> subtitleMs
        AppLanguage.INDONESIAN -> subtitleId
    }

    fun translation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> translationEn
        AppLanguage.MALAY -> translationMs
        AppLanguage.INDONESIAN -> translationId
    }
}

data class JanazahPositionGuide(
    val titleId: String,
    val titleMs: String,
    val titleEn: String,
    val imamPositionId: String,
    val imamPositionMs: String,
    val imamPositionEn: String,
    val descriptionId: String,
    val descriptionMs: String,
    val descriptionEn: String,
    val hadithRef: String
) {
    fun title(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.MALAY -> titleMs
        AppLanguage.INDONESIAN -> titleId
    }

    fun imamPosition(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> imamPositionEn
        AppLanguage.MALAY -> imamPositionMs
        AppLanguage.INDONESIAN -> imamPositionId
    }

    fun description(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> descriptionEn
        AppLanguage.MALAY -> descriptionMs
        AppLanguage.INDONESIAN -> descriptionId
    }
}

data class JanazahDuaItem(
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

data class JanazahGuide(
    val principleDescId: String,
    val principleDescMs: String,
    val principleDescEn: String,
    val rewardHadithId: String,
    val rewardHadithMs: String,
    val rewardHadithEn: String,
    val pillarsId: List<String>,
    val pillarsMs: List<String>,
    val pillarsEn: List<String>,
    val conditionsId: List<String>,
    val conditionsMs: List<String>,
    val conditionsEn: List<String>,
    val takbirSteps: List<JanazahTakbirStep>,
    val niatList: List<JanazahNiatItem>,
    val positionGuides: List<JanazahPositionGuide>,
    val afterDuas: List<JanazahDuaItem>
) {
    fun principleDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> principleDescEn
        AppLanguage.MALAY -> principleDescMs
        AppLanguage.INDONESIAN -> principleDescId
    }

    fun rewardHadith(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> rewardHadithEn
        AppLanguage.MALAY -> rewardHadithMs
        AppLanguage.INDONESIAN -> rewardHadithId
    }

    fun pillars(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.ENGLISH -> pillarsEn
        AppLanguage.MALAY -> pillarsMs
        AppLanguage.INDONESIAN -> pillarsId
    }

    fun conditions(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.ENGLISH -> conditionsEn
        AppLanguage.MALAY -> conditionsMs
        AppLanguage.INDONESIAN -> conditionsId
    }
}
