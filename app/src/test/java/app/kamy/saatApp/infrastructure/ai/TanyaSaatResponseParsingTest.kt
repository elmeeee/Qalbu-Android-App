package app.kamy.saatApp.infrastructure.ai

import app.kamy.saatApp.domain.model.SaatAiResponseEnvelope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.junit.Assert.assertEquals
import org.junit.Test

class TanyaSaatResponseParsingTest {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    @Test
    fun decodesGroqAnswerUsingSnakeCaseFields() {
        val response = json.decodeFromString(
            SaatAiResponseEnvelope.serializer(),
            """{"empathy_text":"Al-Qur'an mengajarkan kita berbuat baik kepada sesama.","chapter_number":2,"verse_number":83,"doa_slug":null}"""
        )

        assertEquals(
            "Al-Qur'an mengajarkan kita berbuat baik kepada sesama.",
            response.empathyText
        )
        assertEquals(2, response.chapterNumber)
        assertEquals(83, response.verseNumber)
    }
}
