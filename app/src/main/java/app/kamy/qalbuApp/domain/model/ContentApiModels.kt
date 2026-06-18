package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- Random ayah / verse payloads (Content API) ----

@Serializable
data class RandomAyahResponse(val verse: RandomAyahPayload? = null)

@Serializable
data class SingleVerseResponse(val verse: RandomAyahPayload? = null)

@Serializable
data class RandomAyahPayload(
    val id: Int? = null,
    @SerialName("chapter_id") val chapterId: Int? = null,
    @SerialName("verse_number") val verseNumber: Int? = null,
    @SerialName("verse_key") val verseKey: String? = null,
    val textIndopak: String? = null,
    val textImlaeiSimple: String? = null,
    val textImlaei: String? = null,
    val textUthmani: String? = null,
    val textUthmaniSimple: String? = null,
    val textUthmaniTajweed: String? = null,
    val textQpcHafs: String? = null,
    val textQpcNastaleeqHafs: String? = null,
    val textQpcNastaleeq: String? = null,
    val textIndopakNastaleeq: String? = null,
    @SerialName("page_number") val pageNumber: Int? = null,
    @SerialName("juz_number") val juzNumber: Int? = null,
    val audio: AudioPayload? = null,
    val translations: List<InlineTranslation>? = null,
    val words: List<QuranWord>? = null,
    @SerialName("transliteration_id") val transliterationId: String? = null,
    @SerialName("transliteration_en") val transliterationEn: String? = null,
    val jalalayn: String? = null
) {
    val resolvedVerseNumber: Int?
        get() {
            verseKey?.substringAfterLast(':', missingDelimiterValue = "")
                ?.trim()
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
                ?.let { return it }
            return verseNumber?.takeIf { it > 0 }
        }

    /** Surah-local reference, e.g. "1:2" — stable across juz boundaries. */
    val displayVerseReference: String?
        get() = verseKey?.takeIf { it.isNotBlank() }
            ?: chapterNumber?.let { chapter ->
                resolvedVerseNumber?.let { verse -> "$chapter:$verse" }
            }

    val listIdentity: String
        get() = when {
            !verseKey.isNullOrEmpty() -> verseKey
            id != null -> "id-$id"
            verseNumber != null -> "ayah-$verseNumber"
            else -> "verse-0"
        }

    val chapterNumber: Int?
        get() = chapterId?.takeIf { it > 0 }
            ?: verseKey
                ?.substringBefore(':', missingDelimiterValue = "")
                ?.trim()
                ?.toIntOrNull()
                ?.takeIf { it > 0 }

    fun referenceLabel(chapterDisplayName: String?): String? {
        val ayah = resolvedVerseNumber
        val surah = chapterDisplayName?.trim()?.takeIf { it.isNotEmpty() }
        return when {
            surah != null && ayah != null -> "$surah - $ayah"
            surah != null -> surah
            ayah != null -> ayah.toString()
            else -> null
        }
    }
}

@Serializable
data class AudioPayload(val url: String? = null)

@Serializable
data class InlineTranslation(
    val id: Int? = null,
    val resourceId: Int? = null,
    val text: String? = null,
    val resourceName: String? = null
)

@Serializable
data class QuranWord(
    val id: Int? = null,
    val position: Int? = null,
    @SerialName("char_type_name") val charTypeName: String? = null,
    @SerialName("line_number") val lineNumber: Int? = null,
    @SerialName("page_number") val pageNumber: Int? = null,
    @SerialName("text_uthmani") val textUthmani: String? = null,
    @SerialName("text_uthmani_tajweed") val textUthmaniTajweed: String? = null,
    @SerialName("code_v1") val codeV1: String? = null,
    @SerialName("code_v2") val codeV2: String? = null,
    val translation: WordTranslation? = null,
    @SerialName("verse_key") val verseKey: String? = null
) {
    val displayText: String
        get() = textUthmaniTajweed ?: textUthmani.orEmpty()

    val isEndMarker: Boolean
        get() = charTypeName == "end"
}

@Serializable
data class WordTranslation(
    val text: String? = null,
    @SerialName("language_name") val languageName: String? = null
)

// ---- Mushaf pages ----

@Serializable
data class PagesLookupResponse(
    @SerialName("lookup_range") val lookupRange: LookupRange? = null,
    val pages: Map<String, PageInfo>? = null,
    @SerialName("total_page") val totalPage: Int? = null
)

@Serializable
data class LookupRange(
    val from: String,
    val to: String
)

@Serializable
data class PageInfo(
    val from: String,
    val to: String,
    @SerialName("first_verse_key") val firstVerseKey: String? = null,
    @SerialName("last_verse_key") val lastVerseKey: String? = null
)

data class MushafLine(
    val lineNumber: Int,
    val words: List<QuranWord>
)

fun List<RandomAyahPayload>.groupIntoMushafLines(mushafPage: Int): List<MushafLine> {
    val allWords = flatMap { verse ->
        verse.words.orEmpty()
            .filter { word ->
                word.pageNumber == null || word.pageNumber == mushafPage
            }
    }
    if (allWords.isEmpty()) {
        return mapIndexed { index, verse ->
            MushafLine(
                lineNumber = index + 1,
                words = listOf(
                    QuranWord(
                        textUthmani = verse.textUthmaniTajweed ?: verse.textUthmani,
                        textUthmaniTajweed = verse.textUthmaniTajweed,
                        charTypeName = "word",
                        verseKey = verse.verseKey,
                        pageNumber = mushafPage
                    )
                )
            )
        }
    }
    return allWords
        .groupBy { it.lineNumber ?: 0 }
        .toSortedMap()
        .map { (lineNum, words) ->
            MushafLine(lineNumber = lineNum, words = words)
        }
}

// ---- Tafsir ----

@Serializable
data class TafsirResponse(val tafsir: TafsirPayload? = null)

@Serializable
data class TafsirPayload(
    val id: Int? = null,
    val text: String? = null,
    val resourceId: Int? = null,
    val resourceName: String? = null
)

// ---- Hadith references ----

@Serializable
data class HadithsByAyahResponse(
    val hadiths: List<HadithReference>? = null,
    val page: Int? = null,
    val limit: Int? = null,
    val hasMore: Boolean? = null,
    val language: String? = null,
    val direction: String? = null
)

@Serializable
data class HadithReference(
    val urn: Int? = null,
    val collection: String? = null,
    val bookNumber: String? = null,
    val chapterId: String? = null,
    val hadithNumber: String? = null,
    val name: String? = null,
    val hadith: List<HadithText>? = null
)

@Serializable
data class HadithText(
    val lang: String? = null,
    val chapterNumber: String? = null,
    val chapterTitle: String? = null,
    val body: String? = null,
    val urn: Int? = null,
    val grades: List<HadithGrade>? = null
)

@Serializable
data class HadithGrade(
    val gradedBy: String? = null,
    val grade: String? = null
)

// ---- Recitations ----

@Serializable
data class RecitationsResponse(val recitations: List<RecitationPayload>? = null)

@Serializable
data class RecitationPayload(
    val id: Int? = null,
    val reciterName: String? = null,
    val translatedName: RecitationTranslatedName? = null
) {
    val identifiableId: Int get() = id ?: 0
    val displayName: String
        get() = translatedName?.name ?: reciterName ?: "Reciter $identifiableId"
}

@Serializable
data class RecitationTranslatedName(val name: String? = null)

// ---- Juz ----

@Serializable
data class JuzsResponse(val juzs: List<QuranJuz> = emptyList())

@Serializable
data class SingleJuzResponse(val juz: QuranJuz? = null)

@Serializable
data class QuranJuz(
    val id: Int = 0,
    @SerialName("juz_number") val juzNumber: Int = 0,
    @SerialName("verse_mapping") val verseMapping: Map<String, String> = emptyMap(),
    @SerialName("first_verse_id") val firstVerseId: Int? = null,
    @SerialName("last_verse_id") val lastVerseId: Int? = null,
    @SerialName("verses_count") val versesCount: Int? = null
) {
    fun startChapterAndAyah(): Pair<Int, Int>? {
        if (verseMapping.isEmpty()) return null
        val chapter = verseMapping.keys.mapNotNull { it.toIntOrNull() }.minOrNull() ?: return null
        val range = verseMapping[chapter.toString()] ?: return null
        val ayah = range.substringBefore('-').trim().toIntOrNull()?.takeIf { it > 0 } ?: 1
        return chapter to ayah
    }

    fun firstChapterNumber(): Int? =
        verseMapping.keys.mapNotNull { it.toIntOrNull() }.minOrNull()
}

// ---- Chapters ----

@Serializable
data class ChaptersResponse(val chapters: List<QuranChapter> = emptyList())

@Serializable
data class QuranChapter(
    val id: Int,
    val revelationPlace: String? = null,
    val revelationOrder: Int? = null,
    val bismillahPre: Boolean? = null,
    val pages: List<Int>? = null,
    val nameSimple: String? = null,
    val nameComplex: String? = null,
    val nameArabic: String? = null,
    val versesCount: Int? = null,
    val translatedName: ChapterTranslatedName? = null
) {
    val displayComplexName: String
        get() = nameComplex?.takeIf { it.isNotEmpty() }
            ?: nameSimple?.takeIf { it.isNotEmpty() }
            ?: "Chapter $id"

    val displayTranslatedName: String get() = translatedName?.name.orEmpty()

    val displayTitle: String
        get() = displayTranslatedName.takeIf { it.isNotEmpty() } ?: displayComplexName

    val revelationLabel: String
        get() {
            val raw = revelationPlace?.trim().orEmpty()
            if (raw.isEmpty()) return ""
            return when (raw.lowercase()) {
                "makkah", "mecca" -> "Makkah"
                "madinah", "medina" -> "Madinah"
                else -> raw.replaceFirstChar { it.titlecase() }
            }
        }

    val isMeccan: Boolean
        get() = revelationPlace?.lowercase() in setOf("makkah", "mecca")
}

@Serializable
data class ChapterTranslatedName(
    val languageName: String? = null,
    val name: String? = null
)

// ---- Verses by chapter ----

@Serializable
data class VersesByChapterResponse(
    val verses: List<RandomAyahPayload> = emptyList(),
    val pagination: ContentPagination? = null
)

@Serializable
data class ContentPagination(
    val perPage: Int? = null,
    val currentPage: Int? = null,
    val nextPage: Int? = null,
    val totalPages: Int? = null,
    val totalRecords: Int? = null
) {
    val hasNextPage: Boolean
        get() = nextPage != null && currentPage != null && nextPage > currentPage
}

// ---- Translations resource ----

@Serializable
data class QFTranslation(
    val id: Int = 0,
    val name: String = "",
    val authorName: String = "",
    val slug: String? = null,
    val languageName: String = "",
    val translatedName: TranslatedSubName? = null
)

@Serializable
data class TranslatedSubName(
    val name: String,
    val languageName: String
)

@Serializable
data class TranslationsResponse(val translations: List<QFTranslation>? = null)
