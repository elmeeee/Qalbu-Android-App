package app.kamy.qalbuApp.features.reader

import app.kamy.qalbuApp.domain.model.HadithGrade
import app.kamy.qalbuApp.domain.model.HadithReference
import app.kamy.qalbuApp.ui.common.toReaderPlainText

data class HadithDisplayItem(
    val id: String,
    val sourceName: String,
    val referenceLabel: String?,
    val chapterTitle: String?,
    val body: String,
    val gradeLines: List<String>
)

fun List<HadithReference>.toDisplayItems(): List<HadithDisplayItem> =
    mapNotNull { it.toDisplayItem() }

private fun HadithReference.toDisplayItem(): HadithDisplayItem? {
    val texts = hadith.orEmpty().filter { !it.body.isNullOrBlank() }
    if (texts.isEmpty()) return null

    val body = texts
        .joinToString(separator = "\n\n") { it.body!!.trim() }
        .toReaderPlainText()
    if (body.isBlank()) return null

    val source = name?.trim().orEmpty()
    val sourceName = source.ifEmpty { collection?.trim().orEmpty().ifEmpty { "Hadith" } }

    val referenceParts = buildList {
        hadithNumber?.trim()?.takeIf { it.isNotEmpty() }?.let { add("#$it") }
        bookNumber?.trim()?.takeIf { it.isNotEmpty() }?.let { add("Book $it") }
    }

    val gradeLines = texts.flatMap { text -> text.grades.orEmpty() }
        .mapNotNull { grade -> grade.toDisplayLine() }
        .distinct()

    val chapterTitle = texts.firstNotNullOfOrNull { t ->
        t.chapterTitle?.trim()?.takeIf { it.isNotEmpty() }
    }

    val id = listOfNotNull(collection, hadithNumber, urn?.toString())
        .plus(texts.mapNotNull { it.urn?.toString() })
        .joinToString("-")
        .ifEmpty { "${sourceName.hashCode()}-${body.hashCode()}" }

    return HadithDisplayItem(
        id = id,
        sourceName = sourceName,
        referenceLabel = referenceParts.takeIf { it.isNotEmpty() }?.joinToString(" · "),
        chapterTitle = chapterTitle,
        body = body,
        gradeLines = gradeLines
    )
}

private fun HadithGrade.toDisplayLine(): String? {
    val by = gradedBy?.trim().orEmpty()
    val value = grade?.trim().orEmpty()
    return when {
        by.isEmpty() && value.isEmpty() -> null
        by.isEmpty() -> value
        value.isEmpty() -> by
        else -> "$value — $by"
    }
}
