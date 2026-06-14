package app.kamy.qalbuApp.features.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerseReferenceSearchTest {

    @Test
    fun parseVerseReference_colonFormat() {
        val ref = parseVerseReference("2:255")
        assertEquals(VerseReference(2, 255), ref)
    }

    @Test
    fun parseVerseReference_dotFormat() {
        assertEquals(VerseReference(36, 1), parseVerseReference("36.1"))
    }

    @Test
    fun parseVerseReference_spaceFormat() {
        assertEquals(VerseReference(67, 15), parseVerseReference("67 15"))
    }

    @Test
    fun parseVerseReference_invalidChapter() {
        assertNull(parseVerseReference("0:1"))
        assertNull(parseVerseReference("115:1"))
    }
}
