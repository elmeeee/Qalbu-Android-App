package app.kamy.saatApp.domain.model

data class SearchVerseResult(
    val verseKey: String,
    val name: String,
    val chapterNumber: Int,
    val ayahNumber: Int
)

data class QuickSearchResult(
    val verses: List<SearchVerseResult> = emptyList()
)
