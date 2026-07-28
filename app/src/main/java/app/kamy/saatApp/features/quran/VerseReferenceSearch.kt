package app.kamy.saatApp.features.quran

data class VerseReference(
    val chapter: Int,
    val ayah: Int
)

private val verseRefPatterns = listOf(
    Regex("""^(\d{1,3})\s*[:.\-/]\s*(\d{1,3})$"""),
    Regex("""^(\d{1,3})\s+(\d{1,3})$""")
)

private val SPECIAL_VERSE_ALIASES = mapOf(
    // Ayat Kursi / Ayatul Kursi / Throne Verse (2:255)
    "ayatul kursi" to VerseReference(2, 255),
    "ayat kursi" to VerseReference(2, 255),
    "throne verse" to VerseReference(2, 255),
    "kursi" to VerseReference(2, 255),

    // 1000 Dinar Verse (65:2)
    "1000 dinar verse" to VerseReference(65, 2),
    "1000 dinar" to VerseReference(65, 2),
    "thousand dinar verse" to VerseReference(65, 2),
    "ayat 1000 dinar" to VerseReference(65, 2),
    "ayat seribu dinar" to VerseReference(65, 2),
    "seribu dinar" to VerseReference(65, 2),

    // Dua for Goodness / Doa Sapujagat (2:201)
    "dua for goodness" to VerseReference(2, 201),
    "comprehensive dua" to VerseReference(2, 201),
    "doa sapujagat" to VerseReference(2, 201),
    "doa sapu jagat" to VerseReference(2, 201),
    "sapujagat" to VerseReference(2, 201),
    "sapu jagat" to VerseReference(2, 201),

    // Dua of Prophet Yunus / Doa Nabi Yunus (21:87)
    "dua of prophet yunus" to VerseReference(21, 87),
    "dua nabi yunus" to VerseReference(21, 87),
    "doa nabi yunus" to VerseReference(21, 87),
    "prophet yunus" to VerseReference(21, 87),
    "nabi yunus" to VerseReference(21, 87),

    // Dua for Ease / Doa Lapang Dada (20:25)
    "dua for ease" to VerseReference(20, 25),
    "dua for ease & heart" to VerseReference(20, 25),
    "doa lapang dada" to VerseReference(20, 25),
    "lapang dada" to VerseReference(20, 25),

    // Dua for Parents / Doa Orang Tua / Doa Ibu Bapa (14:41)
    "dua for parents" to VerseReference(14, 41),
    "doa for parents" to VerseReference(14, 41),
    "doa orang tua" to VerseReference(14, 41),
    "doa orangtua" to VerseReference(14, 41),
    "doa ibu bapa" to VerseReference(14, 41),

    // Direct Surah mappings to verse 1
    "ya-sin" to VerseReference(36, 1),
    "yasin" to VerseReference(36, 1),
    "al-mulk" to VerseReference(67, 1),
    "al mulk" to VerseReference(67, 1),
    "al-kahf" to VerseReference(18, 1),
    "al-kahfi" to VerseReference(18, 1),
    "al kahf" to VerseReference(18, 1),
    "al kahfi" to VerseReference(18, 1),

    // Extra Doa aliases
    "doa rahmat" to VerseReference(18, 10),
    "doa petunjuk" to VerseReference(18, 10),
    "doa khusnul khatimah" to VerseReference(3, 193),
    "khusnul khatimah" to VerseReference(3, 193)
)

fun parseVerseReference(query: String): VerseReference? {
    val raw = query.trim().lowercase()
    if (raw.isEmpty()) return null

    val q = raw
        .removePrefix("surah ")
        .removePrefix("surat ")
        .removePrefix("qs. ")
        .removePrefix("qs ")
        .removePrefix("ayat ")
        .trim()

    if (q.isEmpty()) return null

    // 1. Check special alias dictionary
    SPECIAL_VERSE_ALIASES[raw]?.let { return it }
    SPECIAL_VERSE_ALIASES[q]?.let { return it }

    // 2. Check standard numeric patterns (e.g. 2:255, 2.255, 2 255)
    for (pattern in verseRefPatterns) {
        val match = pattern.matchEntire(q) ?: continue
        val chapter = match.groupValues[1].toIntOrNull() ?: continue
        val ayah = match.groupValues[2].toIntOrNull() ?: continue
        if (chapter in 1..114 && ayah > 0) {
            return VerseReference(chapter = chapter, ayah = ayah)
        }
    }

    // 3. Check "[surah name/alias] [verse number]" or "[surah name/alias]:[verse number]"
    val namedVersePattern = Regex("""^(.+?)[\s:]+(\d{1,3})$""")
    namedVersePattern.matchEntire(q)?.let { match ->
        val namePart = match.groupValues[1].trim()
        val ayah = match.groupValues[2].toIntOrNull()
        if (ayah != null && ayah > 0) {
            val chapterId = findChapterIdByNameOrAlias(namePart)
            if (chapterId != null) {
                return VerseReference(chapter = chapterId, ayah = ayah)
            }
        }
    }

    return null
}

private val juzRefPattern = Regex(
    """^(?:juz|jus|para)\s*(\d{1,2})$""",
    RegexOption.IGNORE_CASE
)

fun parseJuzReference(query: String): Int? {
    val q = query.trim()
    if (q.isEmpty()) return null
    return juzRefPattern.matchEntire(q)?.groupValues?.getOrNull(1)?.toIntOrNull()?.takeIf { it in 1..30 }
}
