package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MushafPageResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        @OptIn(ExperimentalSerializationApi::class)
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    @Test
    fun byPageResponse_parsesWordsAndTajweed() {
        val payload = """
            {
              "verses": [
                {
                  "id": 1,
                  "verse_number": 1,
                  "page_number": 1,
                  "verse_key": "1:1",
                  "text_uthmani_tajweed": "بِسْمِ <tajweed class=ham_wasl>ٱ</tajweed>للَّهِ",
                  "words": [
                    {
                      "id": 1,
                      "position": 1,
                      "char_type_name": "word",
                      "line_number": 2,
                      "page_number": 1,
                      "text_uthmani": "بِسْمِ",
                      "translation": { "text": "In (the) name", "language_name": "english" },
                      "transliteration": { "text": "bis'mi", "language_name": "english" }
                    }
                  ],
                  "translations": [
                    { "resource_id": 131, "text": "In the Name of Allah." }
                  ]
                }
              ],
              "pagination": {
                "per_page": 50,
                "current_page": 1,
                "next_page": null,
                "total_pages": 1,
                "total_records": 1
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<VersesByChapterResponse>(payload)
        assertEquals(1, response.verses.size)
        val verse = response.verses.first()
        assertTrue(verse.textUthmaniTajweed.orEmpty().contains("tajweed"))
        assertEquals(1, verse.words?.size)
        assertEquals(2, verse.words?.first()?.lineNumber)

        val lines = response.verses.groupIntoMushafLines(mushafPage = 1)
        assertEquals(1, lines.size)
        assertEquals("بِسْمِ", lines.first().words.first().textUthmani)
    }
}
