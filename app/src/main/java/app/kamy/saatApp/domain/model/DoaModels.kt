package app.kamy.saatApp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DoaCategory(
    val id: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val photo: String? = null
)

@Serializable
data class DoaItem(
    val id: String? = null,
    val title: String? = null,
    val arabic: String? = null,
    val latin: String? = null,
    val translation: String? = null,
    val notes: String? = null,
    val fawaid: String? = null,
    val source: String? = null,
    val category: String? = null,
    val categories: DoaCategory? = null
)

@Serializable
data class DoaListResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: List<DoaItem>? = null
)

@Serializable
data class DhikrContentItem(
    val arabic: String? = null,
    val latin: String? = null,
    val translation: String? = null,
    val fawaid: String? = null,
    val notes: String? = null,
    @SerialName("repeat_count") val repeatCount: Int? = null
)

@Serializable
data class DhikrBundle(
    val title: String? = null,
    val category: String? = null,
    @SerialName("compiled_by") val compiledBy: String? = null,
    val content: List<DhikrContentItem>? = null
)

@Serializable
data class DhikrListResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: List<DhikrBundle>? = null
)

@Serializable
data class DoaCategoriesResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: List<DoaCategory>? = null
)

enum class DoaCatalogKind { DOA, DHIKR }

data class DoaCatalogEntry(
    val slug: String,
    val title: String,
    val kind: DoaCatalogKind
)
