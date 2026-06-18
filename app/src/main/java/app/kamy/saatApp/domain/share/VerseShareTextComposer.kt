package app.kamy.saatApp.domain.share

import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.core.locale.AppStrings
import app.kamy.saatApp.domain.model.HadithReference
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.infrastructure.ai.AiReflectionRepository
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.repository.ContentRepository
import app.kamy.saatApp.ui.common.sanitizeTajweedArabicHtml
import app.kamy.saatApp.ui.common.stripHtmlTags
import app.kamy.saatApp.ui.common.toReaderPlainText
import app.kamy.saatApp.ui.common.toVerseTranslationPlainText
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerseShareTextComposer @Inject constructor(
    private val contentRepository: ContentRepository,
    private val aiReflection: AiReflectionRepository,
    private val appLanguageStore: AppLanguageStore,
    private val strings: AppStrings
) {
    private val shareTextCache = LinkedHashMap<String, String>()
    private val shareTafsirCache = LinkedHashMap<String, String>()
    private val shareHadithCache = LinkedHashMap<String, String>()
    private val maxCacheEntries = 24

    fun clearCaches() {
        shareTextCache.clear()
        shareTafsirCache.clear()
        shareHadithCache.clear()
    }

    fun cachedShareText(verse: RandomAyahPayload, referenceLabel: String?): String? =
        shareCacheKey(verse)?.let { shareTextCache[it]?.trim()?.takeIf { t -> t.isNotEmpty() } }

    suspend fun quickReflectionText(verse: RandomAyahPayload, referenceLabel: String?): String {
        val verseKey = verse.verseKey
        val tafsir = verseKey?.let { shareTafsirCache[it]?.takeIf { t -> t.isNotEmpty() } }
        val hadith = verseKey?.let { shareHadithCache[it]?.takeIf { t -> t.isNotEmpty() } }
        return guaranteedFaithfulShareText(
            verseKey = verseKey,
            referenceLabel = referenceLabel,
            arabic = verse.fullArabicForShare(),
            translation = verse.translations?.firstOrNull()?.text,
            tafsir = tafsir,
            hadith = hadith
        )
    }

    suspend fun prepareShareText(
        verse: RandomAyahPayload,
        referenceLabel: String?,
        forceRefresh: Boolean = false
    ): String {
        val cacheKey = shareCacheKey(verse)
        if (forceRefresh && cacheKey != null) {
            shareTextCache.remove(cacheKey)
        }
        cacheKey?.let { key ->
            shareTextCache[key]?.takeIf { it.isNotEmpty() }?.let { return it }
        }

        val verseKey = verse.verseKey
        val arabic = verse.fullArabicForShare()
        val translation = verse.translations?.firstOrNull()?.text
        val tafsir = loadTafsirForShare(verseKey)
        val hadith = loadHadithForShare(verseKey)

        val reflection = generatePersonalizedReflection(
            verse = verse,
            referenceLabel = referenceLabel,
            arabic = arabic,
            translation = translation,
            tafsir = tafsir,
            hadith = hadith
        )

        val final = buildFaithfulShareText(
            verseKey = verseKey,
            referenceLabel = referenceLabel,
            arabic = arabic,
            translation = translation,
            tafsir = tafsir,
            aiReflection = reflection
        ) ?: guaranteedFaithfulShareText(
            verseKey = verseKey,
            referenceLabel = referenceLabel,
            arabic = arabic,
            translation = translation,
            tafsir = tafsir,
            hadith = hadith
        )

        cacheKey?.let {
            shareTextCache[it] = final
            trimCachesIfNeeded()
        }
        return final
    }

    private suspend fun loadTafsirForShare(ayahKey: String?): String? {
        if (ayahKey.isNullOrBlank()) return null
        shareTafsirCache[ayahKey]?.let { return it.ifBlank { null } }
        val value = withTimeoutOrNull(2_500L) {
            runCatching {
                contentRepository.getTafsirByAyah(ayahKey = ayahKey)
                    ?.text
                    ?.stripHtmlTags()
            }.getOrNull()
        }
        shareTafsirCache[ayahKey] = value.orEmpty()
        trimCachesIfNeeded()
        return value
    }

    private suspend fun loadHadithForShare(ayahKey: String?): String? {
        if (ayahKey.isNullOrBlank()) return null
        shareHadithCache[ayahKey]?.let { return it.ifBlank { null } }
        val value = withTimeoutOrNull(2_500L) {
            runCatching {
                contentRepository.getHadithsByAyah(ayahKey, limit = 3)
                    .hadiths
                    .orEmpty()
                    .take(3)
                    .joinToString("\n\n") { formatHadithForPrompt(it, appLanguageStore.current().apiCode) }
                    .take(1_200)
            }.getOrNull()
        }
        shareHadithCache[ayahKey] = value.orEmpty()
        trimCachesIfNeeded()
        return value
    }

    private suspend fun generatePersonalizedReflection(
        verse: RandomAyahPayload,
        referenceLabel: String?,
        arabic: String?,
        translation: String?,
        tafsir: String?,
        hadith: String?
    ): String? {
        val language = appLanguageStore.current()
        val outputLanguage = language.aiPromptLanguage
        val toneHint = language.aiToneHint
        val languageRule = language.aiLanguageRule
        val duaOpener = strings.getString(R.string.share_dua_opener)
        val verseLabel = humanLabel(verse.verseKey, referenceLabel)
        val translationText = translation.orEmpty().toVerseTranslationPlainText()
        val arabicText = arabic?.trim().orEmpty()
        val tafsirText = tafsir?.replace("\n", " ")?.trim().orEmpty()
        val hadithText = hadith?.replace("\n", " ")?.trim().orEmpty()
        val sourceName = verse.translations?.firstOrNull()?.resourceName?.trim().orEmpty()

        val system = """
            You help Muslims write short personal reflections to share with friends.
            The verse (Arabic + translation) is shown separately — do NOT quote or repeat it.
            Return plain text only. No headings, lists, markdown, or labels.
            $languageRule
            Tone: $toneHint
            Sound like a real person — not AI, not a sermon, not a blog.
        """.trimIndent()

        val user = """
            Write ONLY the reflection + dua in $outputLanguage.

            Structure:
            • 3–5 short sentences (max 55 words): one honest takeaway for daily life. First person is fine.
            • Blank line.
            • One line starting with: $duaOpener
              Then 1–2 natural dua sentences (max 28 words).

            Avoid completely:
            - "Section", "Reflection", "Dua", numbered lists, bullet points
            - Sermon phrases: "Let us", "Indeed", "Furthermore", "In today's world", "As Muslims we must"
            - Robotic or overly poetic language
            - Repeating the verse, translation, or surah name

            Context (do not copy verbatim):
            Verse: $verseLabel
            Translation meaning: ${translationText.ifBlank { "N/A" }}
            Tafsir hint: ${tafsirText.ifBlank { "N/A" }.take(900)}
            Hadith hint: ${hadithText.ifBlank { "N/A" }.take(800)}
            Arabic (meaning only, do not output Arabic): ${arabicText.ifBlank { "N/A" }.take(400)}
            Source: ${sourceName.ifBlank { "N/A" }}
        """.trimIndent()

        return aiReflection.complete(system = system, user = user, temperature = 0.62)
            ?.let { humanizeAiOutput(it, language) }
            ?.takeIf { it.isNotBlank() }
    }

    private suspend fun generatePersonalizedDua(
        verseLabel: String,
        translation: String?,
        tafsir: String?,
        hadith: String?
    ): String? {
        val language = appLanguageStore.current()
        val outputLanguage = language.aiPromptLanguage
        val toneHint = language.aiToneHint
        val languageRule = language.aiLanguageRule
        val duaOpener = strings.getString(R.string.share_dua_opener)
        val system = """
            Write a short personal dua for sharing. Plain text. $languageRule
            Tone: $toneHint — natural, not robotic.
        """.trimIndent()
        val user = """
            Write only a dua in $outputLanguage.
            Start with exactly: $duaOpener
            Then 1–2 short sentences (max 28 words). No headings or markdown.

            Verse: $verseLabel
            Translation: ${translation.orEmpty().toVerseTranslationPlainText().ifBlank { "N/A" }}
        """.trimIndent()
        return aiReflection.complete(system = system, user = user, temperature = 0.58)
            ?.let { humanizeAiOutput(it, language) }
            ?.takeIf { it.isNotBlank() }
    }

    private fun buildFaithfulShareText(
        verseKey: String?,
        referenceLabel: String?,
        arabic: String?,
        translation: String?,
        tafsir: String?,
        aiReflection: String?
    ): String? {
        val cleanedTranslation = translation.orEmpty().toVerseTranslationPlainText()
        val cleanedArabic = arabic?.trim().orEmpty()
        val verseLabel = humanLabel(verseKey, referenceLabel)
        val aiBody = aiReflection?.trim().orEmpty()
        if (aiBody.isEmpty()) return null
        if (!isReflectionAligned(reflectionForAlignment(aiBody), cleanedTranslation, tafsir)) return null
        return buildShareDocument(verseLabel, cleanedTranslation, cleanedArabic, aiBody)
    }

    private suspend fun guaranteedFaithfulShareText(
        verseKey: String?,
        referenceLabel: String?,
        arabic: String?,
        translation: String?,
        tafsir: String?,
        hadith: String?
    ): String {
        val verseLabel = humanLabel(verseKey, referenceLabel)
        val cleanedTranslation = translation.orEmpty().toVerseTranslationPlainText()
        val cleanedArabic = arabic?.trim().orEmpty()
        val tafsirLine = tafsir?.trim().orEmpty()
        val reflectionBody = buildString {
            appendLine(strings.getString(R.string.share_guaranteed_intro))
            appendLine()
            append(
                if (tafsirLine.isEmpty()) {
                    strings.getString(R.string.share_fallback_reflection)
                } else {
                    humanizeTafsirSnippet(tafsirLine.take(200))
                }
            )
            val dua = generatePersonalizedDua(
                verseLabel = verseLabel,
                translation = translation,
                tafsir = tafsir,
                hadith = hadith
            )
            if (!dua.isNullOrBlank()) {
                appendLine()
                appendLine()
                append(dua)
            }
        }.trim()
        return buildShareDocument(verseLabel, cleanedTranslation, cleanedArabic, reflectionBody)
    }

    private fun buildShareDocument(
        verseLabel: String,
        cleanedTranslation: String,
        cleanedArabic: String,
        body: String
    ): String {
        val now = Date()
        return buildString {
            appendLine(strings.getString(R.string.share_brand_header))
            appendLine(strings.getString(R.string.share_brand_tagline))
            appendLine()
            appendLine(dynamicAddressLine(now))
            appendLine(dynamicTransitionLine(now))
            appendLine()
            appendLine(strings.getString(R.string.share_allah_says))
            appendLine(buildVerseBlock(cleanedArabic, cleanedTranslation, verseLabel))
            appendLine()
            append(body.trim())
            appendLine()
            appendLine()
            append(hashtagsBlock(now))
        }.trim()
    }

    private fun buildVerseBlock(arabic: String, translation: String, label: String): String =
        buildString {
            append("📖 ")
            when {
                arabic.isNotEmpty() && translation.isNotEmpty() -> {
                    appendLine("_${formatArabicBlock(arabic)}_")
                    appendLine("_\"$translation\"_")
                    append("($label)")
                }
                translation.isNotEmpty() -> {
                    appendLine("_\"$translation\"_")
                    append("($label)")
                }
                arabic.isNotEmpty() -> {
                    appendLine("_${formatArabicBlock(arabic)}_")
                    append("($label)")
                }
                else -> append("($label)")
            }
        }

    private fun dynamicAddressLine(now: Date): String {
        val ids = when (ShareDayPeriod.forDate(now)) {
            ShareDayPeriod.MORNING -> listOf(
                R.string.share_addr_morning_1,
                R.string.share_addr_morning_2,
                R.string.share_addr_morning_3
            )
            ShareDayPeriod.AFTERNOON -> listOf(
                R.string.share_addr_afternoon_1,
                R.string.share_addr_afternoon_2,
                R.string.share_addr_afternoon_3
            )
            ShareDayPeriod.EVENING -> listOf(
                R.string.share_addr_evening_1,
                R.string.share_addr_evening_2,
                R.string.share_addr_evening_3
            )
            ShareDayPeriod.NIGHT -> listOf(
                R.string.share_addr_night_1,
                R.string.share_addr_night_2,
                R.string.share_addr_night_3
            )
        }
        return strings.getString(ids.random())
    }

    private fun dynamicTransitionLine(now: Date): String =
        "${dayAwareHook(now)} ${periodAwareReminder(now)}"

    private fun dayAwareHook(now: Date): String {
        val id = when (Calendar.getInstance().apply { time = now }.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> R.string.share_hook_monday
            Calendar.TUESDAY -> R.string.share_hook_tuesday
            Calendar.WEDNESDAY -> R.string.share_hook_wednesday
            Calendar.THURSDAY -> R.string.share_hook_thursday
            Calendar.FRIDAY -> R.string.share_hook_friday
            Calendar.SATURDAY -> R.string.share_hook_saturday
            else -> R.string.share_hook_sunday
        }
        return strings.getString(id)
    }

    private fun periodAwareReminder(now: Date): String {
        val ids = when (ShareDayPeriod.forDate(now)) {
            ShareDayPeriod.MORNING -> listOf(
                R.string.share_reminder_morning_1,
                R.string.share_reminder_morning_2,
                R.string.share_reminder_morning_3
            )
            ShareDayPeriod.AFTERNOON -> listOf(
                R.string.share_reminder_afternoon_1,
                R.string.share_reminder_afternoon_2,
                R.string.share_reminder_afternoon_3
            )
            ShareDayPeriod.EVENING -> listOf(
                R.string.share_reminder_evening_1,
                R.string.share_reminder_evening_2,
                R.string.share_reminder_evening_3
            )
            ShareDayPeriod.NIGHT -> listOf(
                R.string.share_reminder_night_1,
                R.string.share_reminder_night_2,
                R.string.share_reminder_night_3
            )
        }
        return strings.getString(ids.random())
    }

    private fun hashtagsBlock(now: Date): String {
        val dayTag = when (Calendar.getInstance().apply { time = now }.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> strings.getString(R.string.share_hashtag_monday)
            Calendar.TUESDAY -> strings.getString(R.string.share_hashtag_tuesday)
            Calendar.WEDNESDAY -> strings.getString(R.string.share_hashtag_wednesday)
            Calendar.THURSDAY -> strings.getString(R.string.share_hashtag_thursday)
            Calendar.FRIDAY -> strings.getString(R.string.share_hashtag_friday)
            Calendar.SATURDAY -> strings.getString(R.string.share_hashtag_saturday)
            else -> strings.getString(R.string.share_hashtag_sunday)
        }
        return buildString {
            appendLine(strings.getString(R.string.share_hashtag_quran))
            appendLine(strings.getString(R.string.share_hashtag_rrs))
            append(dayTag)
        }.trim()
    }

    private fun reflectionForAlignment(aiBody: String): String =
        aiBody.substringBefore("🤲").trim()

    private fun isReflectionAligned(reflection: String, translation: String, tafsir: String?): Boolean {
        if (reflection.length < 20) return false
        if (appLanguageStore.current() != AppLanguage.ENGLISH) return true
        val source = "$translation ${tafsir.orEmpty()}".lowercase().trim()
        if (source.isEmpty()) return true
        val reflectionLower = reflection.lowercase()
        val sourceWords = source
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length >= 5 }
            .toSet()
        if (sourceWords.isEmpty()) return true
        return sourceWords.any { reflectionLower.contains(it) }
    }

    private fun shareCacheKey(verse: RandomAyahPayload): String? {
        val lang = appLanguageStore.current().tag
        verse.verseKey?.takeIf { it.isNotBlank() }?.let { return "$lang:v4:$it" }
        verse.id?.let { return "$lang:v4:id-$it" }
        return null
    }

    private fun humanLabel(verseKey: String?, referenceLabel: String?): String =
        Companion.humanLabel(verseKey, referenceLabel, strings)

    private fun trimCachesIfNeeded() {
        trimMap(shareTextCache)
        trimMap(shareTafsirCache)
        trimMap(shareHadithCache)
    }

    private fun trimMap(map: LinkedHashMap<String, String>) {
        while (map.size > maxCacheEntries) {
            val eldest = map.keys.firstOrNull() ?: break
            map.remove(eldest)
        }
    }

    companion object {
        private const val TAFSIR_RESOURCE_ID = "169"

        fun humanLabel(verseKey: String?, referenceLabel: String?, strings: AppStrings? = null): String {
            if (!referenceLabel.isNullOrBlank()) {
                return referenceLabel.replace(" - ", "・").trim()
            }
            if (verseKey.isNullOrBlank()) {
                return strings?.getString(R.string.quran_title) ?: "Quran"
            }
            val parts = verseKey.split(":")
            if (parts.size == 2) {
                val chapter = parts[0].toIntOrNull()
                val ayah = parts[1].toIntOrNull()
                if (chapter != null && ayah != null) {
                    return if (strings != null) {
                        "${strings.getString(R.string.surah_number, chapter)}・$ayah"
                    } else {
                        "Surah $chapter・$ayah"
                    }
                }
            }
            return verseKey
        }

        fun sanitizeAiOutput(raw: String): String = raw
            .replace(Regex("(?is)<think>.*?</think>"), "")
            .replace(Regex("(?im)^SECTION\\s*\\d+\\s*[—–-]?.*$"), "")
            .replace(Regex("(?im)^(reflection|dua|refleksi|doa)\\s*[—–-]?.*$"), "")
            .replace(Regex("[*_`]"), "")
            .lines()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()

        fun humanizeAiOutput(raw: String, language: AppLanguage): String {
            var text = sanitizeAiOutput(raw)
            val sermonPatterns = listOf(
                "(?i)in today'?s (fast-paced )?world",
                "(?i)let us remember",
                "(?i)it is important to (note|remember)",
                "(?i)as muslims,? we (must|should)",
                "(?i)furthermore,",
                "(?i)in conclusion,",
                "(?i)this (verse|ayah) (teaches|reminds) us that"
            )
            sermonPatterns.forEach { pattern ->
                text = text.replace(Regex(pattern), "")
            }
            text = text.replace(Regex(" {2,}"), " ")
                .replace(Regex("\n{3,}"), "\n\n")
                .trim()
            if (language != AppLanguage.ENGLISH && looksMostlyEnglish(text)) {
                text = text.lines()
                    .filterNot { looksMostlyEnglish(it) && !it.trimStart().startsWith("🤲") }
                    .joinToString("\n")
                    .trim()
            }
            return text
        }

        private fun looksMostlyEnglish(text: String): Boolean {
            val words = text.lowercase()
                .split(Regex("[^a-zA-Z']+"))
                .filter { it.length >= 3 }
            if (words.size < 4) return false
            val englishHints = setOf(
                "the", "and", "that", "this", "with", "your", "from", "have", "will",
                "should", "remember", "verse", "allah", "indeed", "let", "our"
            )
            val hits = words.count { it in englishHints }
            return hits >= words.size / 3
        }

        fun humanizeTafsirSnippet(raw: String): String =
            raw.trim()
                .replace(Regex("(?i)^in (this|the) verse,?"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
                .let { snippet ->
                    if (snippet.endsWith(".")) snippet else "$snippet."
                }

        fun formatArabicBlock(arabic: String): String =
            arabic.trim()
                .replace(Regex("\\s+"), " ")
                .let { block ->
                    // Keep full ayah on one flowing line for clean copy/share; RTL renders in apps.
                    block
                }

        fun RandomAyahPayload.fullArabicForShare(): String {
            val candidates = listOf(
                textUthmani,
                textUthmaniSimple,
                textUthmaniTajweed,
                textImlaei,
                textImlaeiSimple,
                textQpcHafs,
                textIndopak
            )
            return candidates.firstNotNullOfOrNull { it?.toPlainArabic()?.takeIf { ar -> ar.isNotBlank() } }
                .orEmpty()
        }

        private fun String.toPlainArabic(): String = sanitizeTajweedArabicHtml().stripHtmlTags()

        private fun formatHadithForPrompt(hadith: HadithReference, preferredLang: String): String {
            val body = hadith.hadith
                ?.firstOrNull { it.lang.equals(preferredLang, ignoreCase = true) }
                ?.body
                ?: hadith.hadith?.firstOrNull { it.lang.equals("en", ignoreCase = true) }?.body
                ?: hadith.hadith?.firstOrNull()?.body
            val citation = listOfNotNull(
                hadith.collection?.takeIf { it.isNotBlank() },
                hadith.hadithNumber?.takeIf { it.isNotBlank() }
            ).joinToString(" ")
            return buildString {
                if (!body.isNullOrBlank()) append(body.toReaderPlainText().take(350))
                if (citation.isNotBlank()) {
                    if (isNotEmpty()) append(" ")
                    append("($citation)")
                }
            }.trim()
        }
    }
}
