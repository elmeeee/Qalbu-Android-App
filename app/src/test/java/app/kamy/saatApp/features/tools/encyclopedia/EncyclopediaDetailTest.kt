package app.kamy.saatApp.features.tools.encyclopedia

import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.QuranReference
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class EncyclopediaDetailTest {

    @Test
    fun testQuranReference_containsLatinTransliteration() {
        val ref = QuranReference(
            surahNumber = 2,
            surahName = "Al-Baqarah",
            ayahRange = "30-33",
            verseTextAr = "وَإِذْ قَالَ رَبُّكَ",
            verseTextLatin = "wa idz qâla rabbuka lil-malâ'ikati",
            verseTextLatinEn = "wa idz qala rabbuka lil-mala'ikati",
            verseTextTranslation = "Ingatlah ketika Tuhanmu berfirman"
        )

        assertEquals("wa idz qâla rabbuka lil-malâ'ikati", ref.localizedVerseLatin(AppLanguage.INDONESIAN))
        assertEquals("wa idz qala rabbuka lil-mala'ikati", ref.localizedVerseLatin(AppLanguage.ENGLISH))
        assertEquals("Al-Baqarah", ref.localizedSurahName(AppLanguage.INDONESIAN))
    }

    @Test
    fun testAllProphetsInTopicsJson_haveLatinTransliteration() {
        val file = File("src/main/assets/encyclopedia/topics.json")
        assertTrue("topics.json must exist", file.exists())

        val jsonString = file.readText()
        val json = Json { ignoreUnknownKeys = true }
        val array = json.parseToJsonElement(jsonString).jsonArray

        var prophetCount = 0
        var totalReferences = 0

        for (elem in array) {
            val topicObj = elem.jsonObject
            if (topicObj["categoryId"]?.jsonPrimitive?.content == "prophets") {
                prophetCount++
                val qRefs = topicObj["quranReferences"]?.jsonArray
                if (qRefs != null) {
                    for (refElem in qRefs) {
                        val ref = refElem.jsonObject
                        val latin = ref["verseTextLatin"]?.jsonPrimitive?.content
                        assertNotNull("Latin text must not be null for ${topicObj["id"]?.jsonPrimitive?.content}", latin)
                        assertFalse(
                            "Latin transliteration must not be blank in topic: ${topicObj["id"]?.jsonPrimitive?.content} ref: ${ref["surahName"]?.jsonPrimitive?.content}",
                            latin.isNullOrBlank()
                        )
                        totalReferences++
                    }
                }
            }
        }

        assertEquals(25, prophetCount)
        assertTrue("Must have at least 50 Quran references across 25 prophets", totalReferences >= 50)
    }

    @Test
    fun testStoryChapter_placesVersesInTheMiddle() {
        val verses = listOf(
            QuranReference(
                surahNumber = 2,
                surahName = "Al-Baqarah",
                ayahRange = "30",
                verseTextAr = "وَإِذْ قَالَ رَبُّكَ",
                verseTextLatin = "wa idz qala",
                verseTextTranslation = "Ingatlah..."
            )
        )

        val chapter = StoryChapterUiModel(
            index = 1,
            title = "Penciptaan Manusia",
            paragraphsBefore = listOf("Paragraf 1 narasi", "Paragraf 2 narasi"),
            verses = verses,
            paragraphsAfter = listOf("Paragraf 3 kelanjutan", "Paragraf 4 penutup bab")
        )

        assertEquals(1, chapter.index)
        assertEquals("Penciptaan Manusia", chapter.title)
        assertEquals(2, chapter.paragraphsBefore.size)
        assertEquals(1, chapter.verses.size)
        assertEquals(2, chapter.paragraphsAfter.size)
        assertFalse(chapter.verses.isEmpty())
        assertTrue(chapter.paragraphsBefore.isNotEmpty())
        assertTrue(chapter.paragraphsAfter.isNotEmpty())
    }
}
