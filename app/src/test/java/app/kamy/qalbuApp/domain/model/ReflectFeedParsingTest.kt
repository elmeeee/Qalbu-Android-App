package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflectFeedParsingTest {

    private val snakeJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        @OptIn(ExperimentalSerializationApi::class)
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    private val camelJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val apiSample = """
        {
          "total": 10,
          "currentPage": 1,
          "limit": 10,
          "pages": 1,
          "data": [
            {
              "tags": [{"language": "english", "id": 1, "name": "patience"}],
              "references": [{"id": "2:255", "from": 255, "to": 255, "chapterId": 2}],
              "author": {
                "id": "user-123",
                "username": "reader",
                "verified": true,
                "firstName": "Ahmad",
                "lastName": "Ali",
                "avatarUrls": {"small": "https://example.com/s.jpg"}
              },
              "recentComment": {
                "id": 1,
                "author": {"id": "c1", "username": "commenter"},
                "body": "Ameen",
                "createdAt": "2026-04-02T00:00:00.000Z"
              },
              "room": {"isAdmin": {}, "isOwner": {}, "isPublic": {}, "id": 1},
              "mentions": [],
              "isLiked": true,
              "id": 101,
              "authorId": "user-123",
              "body": "<p>Reflection text</p>",
              "draft": false,
              "createdAt": "2026-04-02T00:00:00.000Z",
              "commentsCount": 2,
              "likesCount": 5,
              "postTypeName": "reflection"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun feed_parsesWithCamelCaseJson() {
        val envelope = camelJson.decodeFromString<ReflectFeedEnvelope>(apiSample)
        assertEquals(1, envelope.items.size)
        val post = envelope.items.first()
        assertEquals("101", post.id)
        assertEquals(true, post.isLiked)
        assertEquals(5, post.likesCount)
        assertEquals("Ahmad Ali", post.author?.displayName)
        assertEquals("2:255", post.references?.first()?.verseKey)
    }

    @Test
    fun feed_parsesWithSnakeCaseJsonConfig() {
        val envelope = snakeJson.decodeFromString<ReflectFeedEnvelope>(apiSample)
        assertEquals(1, envelope.items.size)
    }

    @Test
    fun feed_referenceNumericId_doesNotCrash() {
        val payload = apiSample.replace("\"id\": \"2:255\"", "\"id\": 2255")
        val envelope = camelJson.decodeFromString<ReflectFeedEnvelope>(payload)
        assertTrue(envelope.items.isNotEmpty())
        assertEquals("2255", envelope.items.first().references?.first()?.id)
    }

    @Test
    fun feed_stringTags_parses() {
        val payload = apiSample.replace(
            "\"tags\": [{\"language\": \"english\", \"id\": 1, \"name\": \"patience\"}]",
            "\"tags\": [\"patience\", \"gratitude\"]"
        )
        val envelope = camelJson.decodeFromString<ReflectFeedEnvelope>(payload)
        assertEquals(listOf("patience", "gratitude"), envelope.items.first().tags?.mapNotNull { it.name })
    }
}
