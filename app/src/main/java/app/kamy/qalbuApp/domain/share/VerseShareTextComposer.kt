package app.kamy.qalbuApp.domain.share

import app.kamy.qalbuApp.domain.model.HadithReference
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.infrastructure.ai.AiReflectionRepository
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors iOS TodayShareTextComposer — AI reflection grounded in tafsir + hadith,
 * formatted for social share.
 */
@Singleton
class VerseShareTextComposer @Inject constructor(
    private val contentRepository: ContentRepository,
    private val aiReflection: AiReflectionRepository
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

    suspend fun prefetchShareTextIfNeeded(verse: RandomAyahPayload, referenceLabel: String?) {
        val cacheKey = shareCacheKey(verse) ?: return
        if (shareTextCache.containsKey(cacheKey)) return
        shareTextCache[cacheKey] = prepareShareText(verse, referenceLabel)
        trimCachesIfNeeded()
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
            arabic = verse.textUthmani,
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
        val arabic = verse.textUthmani
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
                contentRepository.getTafsirByAyah(resourceId = TAFSIR_RESOURCE_ID, ayahKey = ayahKey)
                    ?.text
                    ?.stripHtml()
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
                    .joinToString("\n\n") { formatHadithForPrompt(it) }
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
        val verseLabel = humanLabel(verse.verseKey, referenceLabel)
        val translationText = translation.stripHtml()
        val arabicText = arabic?.trim().orEmpty()
        val tafsirText = tafsir?.replace("\n", " ")?.trim().orEmpty()
        val hadithText = hadith?.replace("\n", " ")?.trim().orEmpty()
        val sourceName = verse.translations?.firstOrNull()?.resourceName?.trim().orEmpty()

        val system = """
            You write concise Islamic reflections for social sharing.
            Return plain text only (no JSON, no markdown code fences).
            Keep aqidah-safe and avoid inventing hadith references.
            Stay faithful to the provided verse, tafsir, and hadith context.
        """.trimIndent()

        val user = """
            Write TWO sections in plain text (no JSON, no code fences):

            SECTION 1 — REFLECTION (4-6 short lines, English):
            Focus on practical heart-check and behavior.
            Do not include the verse quote, reference line, hashtags, or the dua header.
            Keep under 70 words.
            When citing hadith, use only the hadith context below (collection/number if present).
            Do not invent facts outside the given verse + tafsir + hadith.

            SECTION 2 — DUA (English, 1-2 short sentences only):
            Start section 2 with exactly this line on its own:
            🤲 *Ya Allah,*
            Then write a fresh, personalized supplication tied to this verse (under 35 words).
            Do not repeat the fixed phrase "purify our hearts and our tongues".

            Verse reference: $verseLabel
            Verse number: ${verse.resolvedVerseNumber ?: ""}
            Juz: ${verse.juzNumber ?: ""}
            Page: ${verse.pageNumber ?: ""}
            Translation: ${translationText.ifBlank { "N/A" }}
            Translation source: ${sourceName.ifBlank { "N/A" }}
            Arabic (context): ${arabicText.ifBlank { "N/A" }}
            Tafsir: ${tafsirText.ifBlank { "N/A" }.take(1_200)}
            Hadith: ${hadithText.ifBlank { "N/A" }.take(1_000)}
        """.trimIndent()

        return aiReflection.complete(system = system, user = user, temperature = 0.35)
    }

    private suspend fun generatePersonalizedDua(
        verseLabel: String,
        translation: String?,
        tafsir: String?,
        hadith: String?
    ): String? {
        val system = """
            You write short Islamic supplications for social sharing.
            Return plain text only. Stay faithful to the given verse context.
        """.trimIndent()
        val user = """
            Write only a DUA block in English.
            Start with exactly:
            🤲 *Ya Allah,*
            Then 1-2 short personalized sentences (under 35 words) based on the verse.
            Do not repeat generic stock phrases like "purify our hearts and our tongues".

            Verse: $verseLabel
            Translation: ${translation.stripHtml().ifBlank { "N/A" }}
            Tafsir: ${tafsir?.take(220).orEmpty().ifBlank { "N/A" }}
            Hadith: ${hadith?.take(400).orEmpty().ifBlank { "N/A" }}
        """.trimIndent()
        return aiReflection.complete(system = system, user = user, temperature = 0.35)
    }

    private fun buildFaithfulShareText(
        verseKey: String?,
        referenceLabel: String?,
        arabic: String?,
        translation: String?,
        tafsir: String?,
        aiReflection: String?
    ): String? {
        val cleanedTranslation = translation.stripHtml()
        val cleanedArabic = arabic?.trim().orEmpty()
        val verseLabel = humanLabel(verseKey, referenceLabel)

        val verseBlock = when {
            cleanedTranslation.isNotEmpty() ->
                "📖 _\"$cleanedTranslation\"_\n($verseLabel)"
            cleanedArabic.isNotEmpty() ->
                "📖 _${cleanedArabic}_\n($verseLabel)"
            else -> "📖 ($verseLabel)"
        }

        val aiBody = aiReflection?.trim().orEmpty()
        if (aiBody.isEmpty()) return null
        if (!isReflectionAligned(reflectionForAlignment(aiBody), cleanedTranslation, tafsir)) return null

        val now = Date()
        return """
            *Al-Khatib | Quran Foundation*
            _1 Verse, 1 Day 📖 Read, Reflect, Share_

            ${dynamicAddressLine(now)}
            ${dynamicTransitionLine(now)}

            $ALLAH_SAYS_ARABIC
            $verseBlock

            $aiBody

            ${hashtagsBlock(now)}
        """.trimIndent()
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
        val cleanedTranslation = translation.stripHtml()
        val cleanedArabic = arabic?.trim().orEmpty()
        val tafsirLine = tafsir?.trim().orEmpty()
        val verseBlock = when {
            cleanedTranslation.isNotEmpty() ->
                "📖 _\"$cleanedTranslation\"_\n($verseLabel)"
            cleanedArabic.isNotEmpty() ->
                "📖 _${cleanedArabic}_\n($verseLabel)"
            else -> "📖 ($verseLabel)"
        }
        val tafsirSummary = if (tafsirLine.isEmpty()) {
            "Take this verse as a direct reminder to verify before reacting and to guard your tongue with truth."
        } else {
            tafsirLine.take(220)
        }
        val dua = generatePersonalizedDua(
            verseLabel = verseLabel,
            translation = translation,
            tafsir = tafsir,
            hadith = hadith
        ).orEmpty()
        val now = Date()
        return buildString {
            appendLine("*Al-Khatib | Quran Foundation*")
            appendLine("_1 Verse, 1 Day 📖 Read, Reflect, Share_")
            appendLine()
            appendLine("_My friend..._")
            appendLine("Let this verse guide what you believe, what you repeat, and what you carry in your heart.")
            appendLine()
            appendLine(ALLAH_SAYS_ARABIC)
            appendLine(verseBlock)
            appendLine()
            appendLine("Reflection:")
            appendLine(tafsirSummary)
            if (dua.isNotBlank()) {
                appendLine()
                append(dua)
            }
            appendLine()
            appendLine()
            append(hashtagsBlock(now))
        }.trim()
    }

    private fun reflectionForAlignment(aiBody: String): String =
        aiBody.substringBefore(DUA_HEADER_MARKER).trim()

    private fun dynamicAddressLine(now: Date): String {
        val options = when (ShareDayPeriod.forDate(now)) {
            ShareDayPeriod.MORNING -> listOf(
                "_My brother this morning..._",
                "_My sister this morning..._",
                "_Dear soul this morning..._"
            )
            ShareDayPeriod.AFTERNOON -> listOf(
                "_My friend this afternoon..._",
                "_Dear heart this afternoon..._",
                "_Beloved seeker this afternoon..._"
            )
            ShareDayPeriod.EVENING -> listOf(
                "_My friend this evening..._",
                "_Dear soul this evening..._",
                "_My brother this evening..._"
            )
            ShareDayPeriod.NIGHT -> listOf(
                "_Dear heart tonight..._",
                "_My friend tonight..._",
                "_Beloved seeker tonight..._"
            )
        }
        return options.random()
    }

    private fun dynamicTransitionLine(now: Date): String =
        "${dayAwareHook(now)} ${periodAwareReminder(now)}"

    private fun dayAwareHook(now: Date): String {
        val cal = java.util.Calendar.getInstance().apply { time = now }
        return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "*As a new week begins,*"
            java.util.Calendar.TUESDAY -> "*As this Tuesday moves on,*"
            java.util.Calendar.WEDNESDAY -> "*Midweek reminder,*"
            java.util.Calendar.THURSDAY -> "*As Thursday passes,*"
            java.util.Calendar.FRIDAY -> "*As Jumu'ah approaches,*"
            java.util.Calendar.SATURDAY -> "*This weekend,*"
            else -> "*On this Sunday,*"
        }
    }

    private fun periodAwareReminder(now: Date): String {
        val reminders = when (ShareDayPeriod.forDate(now)) {
            ShareDayPeriod.MORNING -> listOf(
                "start your day with clarity before you absorb every voice around you.",
                "set your intention early: verify before believing, and reflect before reacting.",
                "let this verse anchor your mind before the rush begins."
            )
            ShareDayPeriod.AFTERNOON -> listOf(
                "pause in the middle of your day and realign your heart with what is true.",
                "protect your peace by filtering what you hear and what you repeat.",
                "let this verse interrupt assumptions and restore clarity."
            )
            ShareDayPeriod.EVENING -> listOf(
                "before the day closes, return your heart to truth and humility.",
                "slow down tonight and release conclusions you built without certainty.",
                "let this verse cleanse today's noise before it enters your heart."
            )
            ShareDayPeriod.NIGHT -> listOf(
                "before you rest, leave rumours behind and hold on to what is clear.",
                "close your night with reflection, not assumptions.",
                "let this verse be your final filter before sleep."
            )
        }
        return reminders.random()
    }

    private fun hashtagsBlock(now: Date): String {
        val cal = java.util.Calendar.getInstance().apply { time = now }
        val daySpecific = when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "#MondayMotivation 🌅"
            java.util.Calendar.TUESDAY -> "#TuesdayTadabbur 🌿"
            java.util.Calendar.WEDNESDAY -> "#WednesdayWisdom ✨"
            java.util.Calendar.THURSDAY -> "#ThursdayReflection 📚"
            java.util.Calendar.FRIDAY -> "#JumuahReminder 🌙"
            java.util.Calendar.SATURDAY -> "#SaturdayReflection 🍃"
            else -> "#SundaySerenity ☁️"
        }
        return """
            #QuranReminder 🌿
            #ReadReflectShare 🤍
            $daySpecific
        """.trimIndent()
    }

    private fun isReflectionAligned(reflection: String, translation: String, tafsir: String?): Boolean {
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
        verse.verseKey?.takeIf { it.isNotBlank() }?.let { return it }
        verse.id?.let { return "id-$it" }
        return null
    }

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
        private const val ALLAH_SAYS_ARABIC = "قَالَ اللَّهُ تَعَالَى"
        private const val DUA_HEADER_MARKER = "🤲 *Ya Allah,*"

        fun humanLabel(verseKey: String?, referenceLabel: String?): String {
            if (!referenceLabel.isNullOrBlank()) {
                return referenceLabel.replace(" - ", "・")
            }
            if (verseKey.isNullOrBlank()) return "Quran"
            val parts = verseKey.split(":")
            if (parts.size == 2) {
                val chapter = parts[0].toIntOrNull()
                val ayah = parts[1].toIntOrNull()
                if (chapter != null && ayah != null) {
                    return "Surah $chapter・$ayah"
                }
            }
            return verseKey
        }

        private fun formatHadithForPrompt(hadith: HadithReference): String {
            val body = hadith.hadith
                ?.firstOrNull { it.lang.equals("en", ignoreCase = true) }
                ?.body
                ?: hadith.hadith?.firstOrNull()?.body
            val citation = listOfNotNull(
                hadith.collection?.takeIf { it.isNotBlank() },
                hadith.hadithNumber?.takeIf { it.isNotBlank() }
            ).joinToString(" ")
            return buildString {
                if (!body.isNullOrBlank()) append(body.stripHtml().take(350))
                if (citation.isNotBlank()) {
                    if (isNotEmpty()) append(" ")
                    append("($citation)")
                }
            }.trim()
        }

        private fun String?.stripHtml(): String =
            this?.replace(Regex("<[^>]+>"), " ")
                ?.replace(Regex("\\s+"), " ")
                ?.trim()
                .orEmpty()
    }
}
