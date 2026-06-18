package app.kamy.qalbuApp.domain.model

import app.kamy.qalbuApp.core.config.LocalQuranConfig

fun RandomAyahPayload.displayTransliteration(translationId: Int): String? =
    LocalQuranConfig.pickTransliteration(
        translationId = translationId,
        transliterationId = transliterationId,
        transliterationEn = transliterationEn
    )
