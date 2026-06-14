package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PostCreateParsingTest {

    private val reflectJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Test
    fun postCreateEnvelope_parsesFullPostInData() {
        val payload = """
            {
              "success": true,
              "data": {
                "tags": [{"language": "english", "id": 1, "name": "patience"}],
                "references": [{"id": "2:255", "from": 255, "to": 255, "chapterId": 2}],
                "author": {},
                "room": {"isAdmin": {}, "isOwner": {}, "isPublic": {}, "id": 1},
                "id": 101,
                "authorId": "user-123",
                "body": "Reflection text",
                "draft": false,
                "createdAt": "2026-04-02T00:00:00.000Z",
                "likesCount": 0,
                "commentsCount": 0,
                "postTypeName": "reflection"
              }
            }
        """.trimIndent()

        val envelope = reflectJson.decodeFromString<PostCreateEnvelope>(payload)
        assertEquals(true, envelope.success)
        assertNotNull(envelope.createdPost)
        assertEquals("101", envelope.createdPost?.id)
        assertEquals("Reflection text", envelope.createdPost?.body)
    }
}
