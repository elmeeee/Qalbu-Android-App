package app.kamy.saatApp.core.config

/**
 * Quran Foundation mushaf layout IDs.
 * @see <a href="https://api-docs.quran.foundation/docs/tutorials/fonts/page-layout/">Page layout guide</a>
 */
object MushafConfig {
    /** QCF Tajweed V4 — Madani 604-page layout with Tajweed colour markup. */
    const val MUSHAF_ID = 19

    /** Tajweed-coloured Madani mushaf (fallback if V4 is unavailable). */
    const val MUSHAF_ID_TAJWEED = 11

    const val TOTAL_PAGES = 604

    /** Bump when mushaf API params or cache shape change. */
    const val CACHE_VERSION = 2

    /** Verse-level fields for mushaf page requests (QF docs). */
    const val VERSE_FIELDS = "text_uthmani,text_uthmani_tajweed"

    /** Word-level fields for line layout (QF page-layout guide). */
    const val WORD_FIELDS =
        "text_uthmani,line_number,page_number,char_type_name,verse_key,code_v2"

    val mushafFallbackIds: List<Int> = listOf(MUSHAF_ID, MUSHAF_ID_TAJWEED, 1)
}
