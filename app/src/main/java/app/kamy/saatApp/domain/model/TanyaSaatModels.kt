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

enum class KimiAiModel(val apiId: String) {
    GPT_OSS_120B("openai/gpt-oss-120b"),
    QWEN_3_8_27B("qwen/qwen3.8-27b");

    val displayName: String
        get() = apiId.substringAfter("/")

    companion object {
        fun fromApiId(apiId: String?): KimiAiModel =
            entries.firstOrNull { it.apiId == apiId } ?: GPT_OSS_120B
    }
}

data class SaatMood(
    val id: String,
    @StringRes val labelRes: Int,
    val iconEmoji: String,
    val promptQuery: String
) {
    companion object {
        val defaultList: List<SaatMood> = listOf(
            SaatMood(
                id = "quran",
                labelRes = R.string.mood_quran,
                iconEmoji = "📖",
                promptQuery = "Apa pesan utama Al-Qur'an tentang berbuat baik kepada sesama?"
            ),
            SaatMood(
                id = "hadith",
                labelRes = R.string.mood_hadith,
                iconEmoji = "📜",
                promptQuery = "Apa perbedaan hadis sahih dan daif? Jangan menyebut sumber atau derajat hadis jika tidak yakin."
            ),
            SaatMood(
                id = "dua",
                labelRes = R.string.mood_dua,
                iconEmoji = "🤲",
                promptQuery = "Apa doa yang dianjurkan sebelum tidur? Sebutkan sumber hanya jika yakin."
            ),
            SaatMood(
                id = "worship",
                labelRes = R.string.mood_worship,
                iconEmoji = "🕌",
                promptQuery = "Apa dasar dan tujuan shalat dalam ajaran Islam?"
            ),
            SaatMood(
                id = "prophets",
                labelRes = R.string.mood_prophets,
                iconEmoji = "☪️",
                promptQuery = "Apa perbedaan nabi dan rasul dalam Islam?"
            ),
            SaatMood(
                id = "belief",
                labelRes = R.string.mood_belief,
                iconEmoji = "❓",
                promptQuery = "Apa makna tauhid dalam Islam?"
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
