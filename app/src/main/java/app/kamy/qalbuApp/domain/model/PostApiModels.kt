package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---- Post creation (Reflect API) ----

@Serializable
data class PostCreateRequest(val post: PostCreatePayload)

@Serializable
data class PostCreatePayload(
    val body: String,
    val draft: Boolean,
    val references: List<PostCreateReference>,
    val mentions: List<PostCreateMention> = emptyList(),
    val roomPostStatus: Int = 1,
    val roomId: Int = 0,
    val postAsAuthorId: String,
    val publishedAt: String
)

@Serializable
class PostCreateMention

@Serializable
data class PostCreateReference(
    val chapterId: Int,
    val from: Int,
    val to: Int,
    val id: String = "surah-$chapterId-$from:$to"
)

@Serializable
data class PostCreateEnvelope(
    val success: Boolean? = null,
    val data: UserPost? = null,
    val post: UserPost? = null
) {
    val createdPost: UserPost? get() = data ?: post
}

@Serializable
data class UserPost(
    val id: Int,
    val body: String
)

// ---- Activity day (Reflect API) ----

@Serializable
data class ActivityDayInput(
    val type: String,
    val day: String,
    val timezone: String,
    val versesRead: Int? = null
)

@Serializable
data class ActivityDayEnvelope(val success: Boolean? = null)

// ---- Likes ----

@Serializable
data class ReflectToggleLikeResponse(val liked: Boolean)

// ---- Feed envelope (data OR posts) ----

@Serializable
data class ReflectFeedEnvelope(
    val total: Int? = null,
    val currentPage: Int? = null,
    val limit: Int? = null,
    val pages: Int? = null,
    private val data: List<ReflectFeedPost>? = null,
    private val posts: List<ReflectFeedPost>? = null
) {
    val items: List<ReflectFeedPost>
        get() = data ?: posts ?: emptyList()
}

// ---- Feed post ----

@Serializable
data class ReflectFeedPost(
    @Serializable(with = StringFromAnyScalarSerializer::class)
    val id: String,
    val body: String? = null,
    val author: ReflectFeedAuthor? = null,
    val references: List<ReflectFeedReference>? = null,
    val tags: List<ReflectFeedTag>? = null,
    val recentComment: ReflectFeedComment? = null,
    var isLiked: Boolean? = null,
    val createdAt: String? = null,
    val draft: Boolean? = null,
    var likesCount: Int? = null,
    val commentsCount: Int? = null,
    val postTypeName: String? = null
)

@Serializable
data class ReflectFeedTag(
    val language: String? = null,
    val id: Int? = null,
    val name: String? = null
)

@Serializable
data class ReflectFeedComment(
    @Serializable(with = NullableStringFromAnyScalarSerializer::class)
    val id: String? = null,
    val body: String? = null,
    val createdAt: String? = null,
    val author: ReflectFeedAuthor? = null
)

@Serializable
data class ReflectFeedAuthor(
    @Serializable(with = NullableStringFromAnyScalarSerializer::class)
    val id: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val verified: Boolean? = null,
    val avatarUrls: UserProfileAvatarUrls? = null
) {
    /** Mirrors iOS `ReflectFeedAuthor.displayName`. */
    val displayName: String
        get() {
            val parts = listOfNotNull(firstName, lastName).filter { it.isNotEmpty() }
            if (parts.isNotEmpty()) return parts.joinToString(" ")
            if (!username.isNullOrEmpty()) return username
            return id ?: "Contributor"
        }

    /** Mirrors iOS `ReflectFeedAuthor.avatarURL`. */
    val avatarUrl: String?
        get() = avatarUrls?.medium ?: avatarUrls?.large ?: avatarUrls?.small
}

@Serializable
data class ReflectFeedReference(
    val id: String? = null,
    val from: Int? = null,
    val to: Int? = null,
    val chapterId: Int? = null
) {
    val verseKey: String?
        get() {
            if (chapterId != null && from != null) return "$chapterId:$from"
            if (!id.isNullOrEmpty()) return VerseKeyFormat.canonical(id)
            return null
        }
}
