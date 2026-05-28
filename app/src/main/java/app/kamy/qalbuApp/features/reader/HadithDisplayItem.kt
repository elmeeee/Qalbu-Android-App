package app.kamy.qalbuApp.features.reader

import app.kamy.qalbuApp.domain.model.HadithReference
import app.kamy.qalbuApp.ui.common.looksLikeHtml
import app.kamy.qalbuApp.ui.common.stripHtmlTags

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
    val text = hadith?.firstOrNull() ?: return null
    val rawBody = text.body?.trim().orEmpty()
    if (rawBody.isEmpty()) return null
    val body = if (rawBody.looksLikeHtml()) rawBody else rawBody.stripHtmlTags()

    val source = name?.trim().orEmpty()
    val sourceName = source.ifEmpty { collection?.trim().orEmpty().ifEmpty { "Hadith" } }

    val referenceParts = buildList {
        hadithNumber?.trim()?.takeIf { it.isNotEmpty() }?.let { add("#$it") }
        bookNumber?.trim()?.takeIf { it.isNotEmpty() }?.let { add("Book $it") }
    }

    val gradeLines = (text.grades.orEmpty()).mapNotNull { grade ->
        val by = grade.gradedBy?.trim().orEmpty()
        val value = grade.grade?.trim().orEmpty()
        when {
            by.isEmpty() && value.isEmpty() -> null
            by.isEmpty() -> value
            value.isEmpty() -> by
            else -> "$value — $by"
        }
    }

    val id = listOfNotNull(collection, hadithNumber, urn?.toString(), text.urn?.toString())
        .joinToString("-")
        .ifEmpty { "${sourceName.hashCode()}-${body.hashCode()}" }

    return HadithDisplayItem(
        id = id,
        sourceName = sourceName,
        referenceLabel = referenceParts.takeIf { it.isNotEmpty() }?.joinToString(" · "),
        chapterTitle = text.chapterTitle?.trim()?.takeIf { it.isNotEmpty() },
        body = body,
        gradeLines = gradeLines
    )
}
