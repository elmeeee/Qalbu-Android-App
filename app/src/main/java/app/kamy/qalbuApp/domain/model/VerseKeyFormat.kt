package app.kamy.qalbuApp.domain.model

/**
 * Mirrors iOS Domain/Enums/VerseKeyFormat.swift.
 *
 * Canonical form is "chapter:ayah" (e.g., "3:7"). API responses sometimes use
 * "surah-3-7:7" (range form). This helper normalizes either form to canonical.
 */
object VerseKeyFormat {
    fun canonical(raw: String): String {
        if (!raw.startsWith("surah-")) return raw
        val rest = raw.removePrefix("surah-")
        val colon = rest.indexOf(':')
        if (colon < 0) return raw
        val chapterAyah = rest.substring(0, colon)
        val segments = chapterAyah.split('-')
        if (segments.size < 2) return raw
        val chapter = segments[0].toIntOrNull() ?: return raw
        val ayah = segments.last().toIntOrNull() ?: return raw
        return "$chapter:$ayah"
    }
}
