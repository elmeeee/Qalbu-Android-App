package app.kamy.saatApp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReadingSession(
    val id: String,
    val updatedAt: String? = null,
    val chapterNumber: Int,
    val verseNumber: Int
)

@Serializable
data class ReadingSessionsPage(
    val success: Boolean? = null,
    val data: List<ReadingSession>? = null,
    val pagination: CursorPagination? = null
)

@Serializable
data class CursorPagination(
    val startCursor: String? = null,
    val endCursor: String? = null,
    val hasNextPage: Boolean? = null,
    val hasPreviousPage: Boolean? = null
)

@Serializable
data class ReadingSessionInput(
    val chapterNumber: Int,
    val verseNumber: Int
)
