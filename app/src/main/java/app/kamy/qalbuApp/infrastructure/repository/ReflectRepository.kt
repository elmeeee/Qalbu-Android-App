package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.ActivityDayInput
import app.kamy.qalbuApp.domain.model.PostCreatePayload
import app.kamy.qalbuApp.domain.model.PostCreateReference
import app.kamy.qalbuApp.domain.model.PostCreateRequest
import app.kamy.qalbuApp.domain.model.ReflectFeedEnvelope
import app.kamy.qalbuApp.domain.model.ReflectFeedPost
import app.kamy.qalbuApp.domain.model.UserProfilePayload
import app.kamy.qalbuApp.infrastructure.network.api.ReflectApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReflectRepository @Inject constructor(
    private val api: ReflectApiService
) {
    suspend fun fetchMyProfile(): UserProfilePayload = qfCall { api.getMyProfile() }

    suspend fun fetchAllReflectFeed(page: Int, limit: Int = 20): ReflectFeedEnvelope =
        qfCall { api.getPostsFeed(page = page, limit = limit) }

    suspend fun fetchMyReflections(page: Int, limit: Int = 20): ReflectFeedEnvelope =
        qfCall { api.getMyPosts(page = page, limit = limit) }

    suspend fun togglePostLike(postId: String): Boolean =
        qfCall { api.togglePostLike(postId).liked }

    suspend fun createReflectionPost(
        body: String,
        verseKey: String,
        authorId: String,
        idempotencyKey: String? = null
    ): ReflectFeedPost? {
        val (chapter, verse) = parseVerseKey(verseKey)
            ?: throw IllegalArgumentException("invalid verseKey '$verseKey'")
        val now = Date()
        val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }

        val payload = PostCreatePayload(
            body = body,
            draft = false,
            references = listOf(PostCreateReference(chapterId = chapter, from = verse, to = verse)),
            postAsAuthorId = authorId,
            publishedAt = isoFmt.format(now)
        )

        val response = qfCall {
            api.createPost(PostCreateRequest(payload), idempotencyKey = idempotencyKey)
        }
        if (!response.isSuccessful) {
            throw IllegalStateException("create post failed: HTTP ${response.code()}")
        }

        runCatching {
            qfCall {
                api.logActivityDay(
                    ActivityDayInput(
                        type = "QURAN",
                        day = dayFmt.format(now),
                        timezone = TimeZone.getDefault().id,
                        versesRead = 1
                    )
                )
            }
        }

        return response.body()?.createdPost
    }

    private fun parseVerseKey(key: String): Pair<Int, Int>? {
        val parts = key.split(':')
        if (parts.size != 2) return null
        val chapter = parts[0].toIntOrNull() ?: return null
        val ayah = parts[1].toIntOrNull() ?: return null
        return chapter to ayah
    }
}
