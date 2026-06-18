package app.kamy.saatApp.features.quran

data class VerseReference(
    val chapter: Int,
    val ayah: Int
)

private val verseRefPatterns = listOf(
    Regex("""^(\d{1,3})\s*[:.\-/]\s*(\d{1,3})$"""),
    Regex("""^(\d{1,3})\s+(\d{1,3})$""")
)

fun parseVerseReference(query: String): VerseReference? {
    val q = query.trim()
    if (q.isEmpty()) return null
    for (pattern in verseRefPatterns) {
        val match = pattern.matchEntire(q) ?: continue
        val chapter = match.groupValues[1].toIntOrNull() ?: continue
        val ayah = match.groupValues[2].toIntOrNull() ?: continue
        if (chapter in 1..114 && ayah > 0) {
            return VerseReference(chapter = chapter, ayah = ayah)
        }
    }
    return null
}
