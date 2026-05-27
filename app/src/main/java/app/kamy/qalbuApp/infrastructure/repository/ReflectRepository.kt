package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.ActivityDayInput
import app.kamy.qalbuApp.domain.model.PostCreatePayload
import app.kamy.qalbuApp.domain.model.PostCreateReference
import app.kamy.qalbuApp.domain.model.PostCreateRequest
import app.kamy.qalbuApp.domain.model.ReflectFeedEnvelope
import app.kamy.qalbuApp.domain.model.ReflectFeedPost
import app.kamy.qalbuApp.domain.model.UserPost
import app.kamy.qalbuApp.domain.model.UserProfilePayload
import app.kamy.qalbuApp.infrastructure.network.api.ReflectApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors iOS Infrastructure/Services/ReflectRepository.swift +
 * UserHabitRepository.swift. Wraps the Reflect API.
 */
@Singleton
class ReflectRepository @Inject constructor(
    private val api: ReflectApiService
) {
    /** Mirrors iOS UserHabitRepository.fetchMyProfile. */
    suspend fun fetchMyProfile(): UserProfilePayload = qfCall { api.getMyProfile() }

    /** Mirrors iOS ReflectRepository feed paging. */
    suspend fun fetchAllReflectFeed(page: Int, limit: Int = 20): ReflectFeedEnvelope =
        qfCall { api.getPostsFeed(page = page, limit = limit) }

    suspend fun fetchMyReflections(page: Int, limit: Int = 20): ReflectFeedEnvelope =
        qfCall { api.getMyPosts(page = page, limit = limit) }

    /** Mirrors iOS ReflectRepository.togglePostLike with optimistic semantics handled by VM. */
    suspend fun togglePostLike(postId: String): Boolean =
        qfCall { api.togglePostLike(postId).liked }

    /**
     * Mirrors iOS ReflectRepository.createReflectionPost + UserHabitRepository.logActivityDay.
     * Builds the post payload from the verse key, then logs activity_day with `verses_read=1`.
     */
    suspend fun createReflectionPost(
        body: String,
        verseKey: String,
        authorId: String,
        idempotencyKey: String? = null
    ): UserPost? {
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

        val envelope = qfCall {
            api.createPost(PostCreateRequest(payload), idempotencyKey = idempotencyKey)
        }

        // Best-effort activity_day logging (iOS UserHabitRepository pattern).
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

        return envelope.createdPost
    }

    /** Helpers ------------------------------------------------------------- */
    private fun parseVerseKey(key: String): Pair<Int, Int>? {
        val parts = key.split(':')
        if (parts.size != 2) return null
        val chapter = parts[0].toIntOrNull() ?: return null
        val ayah = parts[1].toIntOrNull() ?: return null
        return chapter to ayah
    }
}
