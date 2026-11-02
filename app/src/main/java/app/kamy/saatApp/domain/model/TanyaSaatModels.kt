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

enum class KimiAiModel(val apiId: String, val displayName: String) {
    GPT_OSS_120B("openai/gpt-oss-120b", "GPT-OSS 120B"),
    QWEN_3_8_27B("qwen/qwen3.8-27b", "Qwen 3.8 27B");

    companion object {
        fun fromApiId(apiId: String?): KimiAiModel =
            entries.firstOrNull { it.apiId == apiId } ?: GPT_OSS_120B
    }
}

data class SaatMood(
    val id: String,
    @StringRes val labelRes: Int,
    val iconEmoji: String,
    @StringRes val promptRes: Int
) {
    companion object {
        val defaultList: List<SaatMood> = listOf(
            SaatMood(
                id = "quran",
                labelRes = R.string.mood_quran,
                iconEmoji = "📖",
                promptRes = R.string.mood_prompt_quran
            ),
            SaatMood(
                id = "hadith",
                labelRes = R.string.mood_hadith,
                iconEmoji = "📜",
                promptRes = R.string.mood_prompt_hadith
            ),
            SaatMood(
                id = "dua",
                labelRes = R.string.mood_dua,
                iconEmoji = "🤲",
                promptRes = R.string.mood_prompt_dua
            ),
            SaatMood(
                id = "worship",
                labelRes = R.string.mood_worship,
                iconEmoji = "🕌",
                promptRes = R.string.mood_prompt_worship
            ),
            SaatMood(
                id = "prophets",
                labelRes = R.string.mood_prophets,
                iconEmoji = "☪️",
                promptRes = R.string.mood_prompt_prophets
            ),
            SaatMood(
                id = "belief",
                labelRes = R.string.mood_belief,
                iconEmoji = "❓",
                promptRes = R.string.mood_prompt_belief
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
