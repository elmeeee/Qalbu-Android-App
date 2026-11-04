package app.kamy.saatApp.domain.model

import androidx.annotation.StringRes
import app.kamy.saatApp.R
import kotlinx.serialization.Serializable

/**
 * Raw JSON envelope returned by the Kīmi model.
 * All fields are optional so a partially valid answer can still be rendered.
 */
@Serializable
data class SaatAiResponseEnvelope(
    val empathyText: String = "",
    val chapterNumber: Int? = null,
    val verseNumber: Int? = null,
    val doaSlug: String? = null,
    val keywords: List<String> = emptyList(),
    val references: List<SaatAiReferenceDto> = emptyList(),
    val followUps: List<String> = emptyList()
)

@Serializable
data class SaatAiReferenceDto(
    val type: String = "other",
    val citation: String = "",
    val note: String? = null,
    val grade: String? = null
)

enum class KimiAiModel(
    val apiId: String,
    val displayName: String,
    val shortName: String,
    val tagline: String
) {
    GPT_OSS_120B(
        apiId = "openai/gpt-oss-120b",
        displayName = "GPT-OSS 120B",
        shortName = "GPT-OSS",
        tagline = "Deep & detailed"
    ),
    QWEN_3_8_27B(
        apiId = "qwen/qwen3.8-27b",
        displayName = "Qwen 3.8 27B",
        shortName = "Qwen",
        tagline = "Fast & concise"
    );

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
                id = "history",
                labelRes = R.string.mood_history,
                iconEmoji = "🏛️",
                promptRes = R.string.mood_prompt_history
            ),
            SaatMood(
                id = "fiqh",
                labelRes = R.string.mood_fiqh,
                iconEmoji = "⚖️",
                promptRes = R.string.mood_prompt_fiqh
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

enum class SaatReferenceType {
    QURAN, HADITH, SCHOLAR, HISTORY, OTHER;

    companion object {
        fun fromRaw(raw: String?): SaatReferenceType = when (raw?.trim()?.lowercase()) {
            "quran", "qur'an", "al-quran", "ayah", "verse" -> QURAN
            "hadith", "hadis", "sunnah" -> HADITH
            "scholar", "ulama", "fiqh", "tafsir", "book", "kitab" -> SCHOLAR
            "history", "sirah", "seerah", "sejarah" -> HISTORY
            else -> OTHER
        }
    }
}

data class SaatReference(
    val type: SaatReferenceType,
    val citation: String,
    val note: String? = null,
    val grade: String? = null
)

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
    val doaData: DoaItem? = null,
    val references: List<SaatReference> = emptyList(),
    val followUps: List<String> = emptyList(),
    val modelName: String? = null,
    val isError: Boolean = false
)
