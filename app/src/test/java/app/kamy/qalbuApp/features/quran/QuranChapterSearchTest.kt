package app.kamy.qalbuApp.features.quran

import app.kamy.qalbuApp.domain.model.ChapterTranslatedName
import app.kamy.qalbuApp.domain.model.QuranChapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranChapterSearchTest {

    private val chapters = listOf(
        chapter(1, "Al-Fatihah", "The Opening", "makkah"),
        chapter(2, "Al-Baqarah", "The Cow", "madinah"),
        chapter(36, "Ya-Sin", "Ya Sin", "makkah"),
        chapter(67, "Al-Mulk", "The Sovereignty", "makkah")
    )

    @Test
    fun emptyQuery_returnsAllChapters() {
        assertEquals(4, chapters.searchChapters("").size)
        assertEquals(4, chapters.searchChapters("   ").size)
    }

    @Test
    fun alias_yasin_findsSurah36() {
        val results = chapters.searchChapters("yasin")
        assertEquals(1, results.size)
        assertEquals(36, results.first().id)
    }

    @Test
    fun number_exactMatch() {
        val results = chapters.searchChapters("67")
        assertEquals(1, results.size)
        assertEquals(67, results.first().id)
    }

    @Test
    fun revelation_makkah_returnsMeccanSurahs() {
        val results = chapters.searchChapters("makkah")
        assertEquals(3, results.size)
        assertTrue(results.all { it.isMeccan })
    }

    @Test
    fun whitespaceOnly_notTreatedAsActiveSearch() {
        assertTrue("".normalizedSearchQuery().isEmpty())
        assertTrue("   ".normalizedSearchQuery().isEmpty())
    }

    private fun chapter(
        id: Int,
        complex: String,
        translated: String,
        place: String
    ) = QuranChapter(
        id = id,
        revelationPlace = place,
        nameComplex = complex,
        nameSimple = complex,
        translatedName = ChapterTranslatedName(name = translated)
    )
}
