package app.kamy.saatApp.domain.model

import app.kamy.saatApp.core.config.LocalQuranConfig

fun RandomAyahPayload.displayTransliteration(translationId: Int): String? =
    LocalQuranConfig.pickTransliteration(
        translationId = translationId,
        transliterationId = transliterationId,
        transliterationEn = transliterationEn
    )

fun RandomAyahPayload.transliterationUsesHtml(translationId: Int): Boolean =
    LocalQuranConfig.transliterationUsesHtml(translationId)
