package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

@Serializable
data class QfSearchResponse(
    val pagination: QfSearchPagination? = null,
    val result: QfSearchResultBody? = null
)

@Serializable
data class QfSearchPagination(
    val currentPage: Int? = null,
    val nextPage: Int? = null,
    val perPage: Int? = null,
    val totalPages: Int? = null,
    val totalRecords: Int? = null
)

@Serializable
data class QfSearchResultBody(
    val navigation: List<QfSearchHit>? = null,
    val verses: List<QfSearchHit>? = null
)

@Serializable
data class QfSearchHit(
    val resultType: String? = null,
    val key: JsonElement? = null,
    val name: String? = null,
    val isArabic: Boolean? = null
)

fun QfSearchHit.keyAsString(): String? = when (val element = key) {
    null -> null
    is JsonPrimitive -> when {
        element.isString -> element.content
        else -> element.intOrNull?.toString() ?: element.content
    }
    else -> element.toString()
}

data class SearchNavResult(
    val type: String,
    val key: String,
    val name: String,
    val chapterNumber: Int?
)

data class SearchVerseResult(
    val verseKey: String,
    val name: String,
    val chapterNumber: Int,
    val ayahNumber: Int
)

data class QuickSearchResult(
    val navigation: List<SearchNavResult> = emptyList(),
    val verses: List<SearchVerseResult> = emptyList()
)
