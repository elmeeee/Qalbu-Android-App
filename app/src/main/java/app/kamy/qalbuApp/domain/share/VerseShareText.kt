package app.kamy.qalbuApp.domain.share

import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.ui.common.toVerseTranslationPlainText

data class VerseShareInput(
    val arabic: String,
    val translation: String,
    val reference: String,
    val verseKey: String?
)

object VerseShareText {

    fun inputFrom(
        verse: RandomAyahPayload,
        referenceLabel: String?
    ): VerseShareInput = VerseShareInput(
        arabic = verse.textUthmani?.trim().orEmpty(),
        translation = verse.translations?.firstOrNull()?.text
            ?.toVerseTranslationPlainText()
            .orEmpty(),
        reference = referenceLabel
            ?: verse.referenceLabel(null)
            ?: verse.verseKey.orEmpty(),
        verseKey = verse.verseKey
    )

    fun plainText(verse: RandomAyahPayload, referenceLabel: String?): String {
        val input = inputFrom(verse, referenceLabel)
        return buildString {
            if (input.arabic.isNotEmpty()) {
                appendLine(input.arabic)
                appendLine()
            }
            if (input.translation.isNotEmpty()) {
                appendLine(input.translation)
                appendLine()
            }
            if (input.reference.isNotEmpty()) {
                append("— ${input.reference}")
            }
        }.trim()
    }

    fun withReflectionFooter(reflection: String, verse: RandomAyahPayload, referenceLabel: String?): String {
        val verseBlock = plainText(verse, referenceLabel)
        return buildString {
            appendLine(reflection.trim())
            if (verseBlock.isNotEmpty()) {
                appendLine()
                appendLine()
                append(verseBlock)
            }
        }.trim()
    }
}
