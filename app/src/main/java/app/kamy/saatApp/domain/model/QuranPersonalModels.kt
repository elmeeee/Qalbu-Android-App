package app.kamy.saatApp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VerseBookmark(
    val verseKey: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val createdAtMillis: Long,
    val surahLabel: String? = null
)

@Serializable
data class VerseNote(
    val verseKey: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String,
    val updatedAtMillis: Long
)

enum class HifzStatus {
    NONE,
    LEARNING,
    MEMORIZED,
    NEEDS_REVIEW;

    fun nextOnTap(): HifzStatus = when (this) {
        NONE -> LEARNING
        LEARNING -> MEMORIZED
        MEMORIZED -> NEEDS_REVIEW
        NEEDS_REVIEW -> NONE
    }
}

@Serializable
data class HifzEntry(
    val verseKey: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val status: String
)

data class LocalReadingProgress(
    val chapterNumber: Int,
    val verseNumber: Int,
    val updatedAtMillis: Long
) {
    fun toReadingSession(): ReadingSession = ReadingSession(
        id = "local",
        updatedAt = updatedAtMillis.toString(),
        chapterNumber = chapterNumber,
        verseNumber = verseNumber
    )
}
