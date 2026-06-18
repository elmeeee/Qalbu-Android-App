package app.kamy.saatApp.domain.faraidh

import kotlinx.serialization.Serializable

@Serializable
data class FaraidhGlossaryBundle(
    val terms: List<FaraidhGlossaryTerm> = emptyList()
)

@Serializable
data class FaraidhGlossaryTerm(
    val id: String,
    val titleEn: String,
    val titleId: String,
    val titleMs: String,
    val bodyEn: String,
    val bodyId: String,
    val bodyMs: String,
    val arabic: String? = null
)

data class FaraidhGlossaryItem(
    val id: String,
    val title: String,
    val body: String,
    val arabic: String?
)
