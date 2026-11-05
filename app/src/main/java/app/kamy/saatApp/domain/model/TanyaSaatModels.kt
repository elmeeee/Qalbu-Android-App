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

@Serializable
data class KimiAiModel(
    val apiId: String,
    val displayName: String,
    val shortName: String,
    val tagline: String
) {
    companion object {
        val GPT_OSS_120B = KimiAiModel(
            apiId = "openai/gpt-oss-120b",
            displayName = "GPT-OSS 120B",
            shortName = "GPT-OSS",
            tagline = "Deep & detailed"
        )
        val QWEN_3_8_27B = KimiAiModel(
            apiId = "qwen/qwen3.8-27b",
            displayName = "Qwen 3.8 27B",
            shortName = "Qwen",
            tagline = "Fast & concise"
        )
        val GPT_OSS_20B = KimiAiModel(
            apiId = "openai/gpt-oss-20b",
            displayName = "GPT-OSS 20B",
            shortName = "GPT-20B",
            tagline = "Ultra-fast & efficient"
        )
        val ALLAM_2_7B = KimiAiModel(
            apiId = "allam-2-7b",
            displayName = "ALLaM 2.0 7B",
            shortName = "ALLaM",
            tagline = "SDAIA Islamic & Arabic"
        )

        val defaultList: List<KimiAiModel> = listOf(
            GPT_OSS_120B,
            QWEN_3_8_27B
        )

        /** Models intentionally hidden from the Kīmi model picker. */
        private val hiddenApiIds = setOf("openai/gpt-oss-20b", "allam-2-7b")

        fun isHidden(apiId: String): Boolean =
            hiddenApiIds.contains(apiId) ||
                apiId.contains("allam", ignoreCase = true) ||
                apiId.contains("gpt-oss-20b", ignoreCase = true)

        fun fromApiId(apiId: String?): KimiAiModel =
            defaultList.firstOrNull { it.apiId == apiId }
                ?: if (!apiId.isNullOrBlank() && !isHidden(apiId)) {
                    createFromId(apiId)
                } else {
                    GPT_OSS_120B
                }

        fun createFromId(id: String): KimiAiModel {
            return when {
                id.contains("gpt-oss-120b", ignoreCase = true) -> GPT_OSS_120B
                id.contains("qwen3.8-27b", ignoreCase = true) || id.contains("qwen", ignoreCase = true) -> QWEN_3_8_27B
                else -> {
                    val cleanName = id.substringAfterLast("/")
                        .replace("-", " ")
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    KimiAiModel(
                        apiId = id,
                        displayName = cleanName,
                        shortName = cleanName.split(" ").firstOrNull() ?: cleanName,
                        tagline = "Hosted on Groq"
                    )
                }
            }
        }
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
