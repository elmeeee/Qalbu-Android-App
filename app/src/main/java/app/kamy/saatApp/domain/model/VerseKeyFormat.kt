package app.kamy.saatApp.domain.model

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
