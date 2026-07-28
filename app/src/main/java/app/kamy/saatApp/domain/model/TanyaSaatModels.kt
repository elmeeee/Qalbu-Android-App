package app.kamy.saatApp.domain.model

import androidx.annotation.StringRes
import app.kamy.saatApp.R
import kotlinx.serialization.Serializable

@Serializable
data class SaatAiResponseEnvelope(
    val empathyText: String = "",
    val chapterNumber: Int? = null,
    val verseNumber: Int? = null,
    val doaSlug: String? = null,
    val keywords: List<String> = emptyList()
)

data class SaatMood(
    val id: String,
    @StringRes val labelRes: Int,
    val iconEmoji: String,
    val promptQuery: String
) {
    companion object {
        val defaultList: List<SaatMood> = listOf(
            SaatMood(
                id = "anxious",
                labelRes = R.string.mood_anxious,
                iconEmoji = "😰",
                promptQuery = "Saya merasa cemas dan khawatir tentang masa depan."
            ),
            SaatMood(
                id = "sad",
                labelRes = R.string.mood_sad,
                iconEmoji = "💔",
                promptQuery = "Saya merasa sedih, kecewa, dan terluka."
            ),
            SaatMood(
                id = "grateful",
                labelRes = R.string.mood_grateful,
                iconEmoji = "🤲",
                promptQuery = "Saya merasa sangat bersyukur atas nikmat Allah."
            ),
            SaatMood(
                id = "angry",
                labelRes = R.string.mood_angry,
                iconEmoji = "😠",
                promptQuery = "Saya sedang merasa marah dan emosi tak terkontrol."
            ),
            SaatMood(
                id = "restless",
                labelRes = R.string.mood_restless,
                iconEmoji = "😴",
                promptQuery = "Saya merasa tidak tenang dan sulit tidur."
            ),
            SaatMood(
                id = "guidance",
                labelRes = R.string.mood_guidance,
                iconEmoji = "❓",
                promptQuery = "Saya merasa bingung dan butuh petunjuk hidup."
            )
        )
    }
}

enum class ChatSender {
    USER, AI
}

data class SaatVerseCardData(
    val chapterNumber: Int,
    val verseNumber: Int,
    val surahName: String,
    val arabicText: String,
    val translationText: String,
    val verseKey: String
)

data class SaatChatMessage(
    val id: String,
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val verseData: SaatVerseCardData? = null,
    val doaData: DoaItem? = null
)
