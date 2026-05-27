package app.kamy.qalbuApp.infrastructure.network.api

import app.kamy.qalbuApp.domain.model.ActivityDayEnvelope
import app.kamy.qalbuApp.domain.model.ActivityDayInput
import app.kamy.qalbuApp.domain.model.PostCreateEnvelope
import app.kamy.qalbuApp.domain.model.PostCreateRequest
import app.kamy.qalbuApp.domain.model.ReflectFeedEnvelope
import app.kamy.qalbuApp.domain.model.ReflectToggleLikeResponse
import app.kamy.qalbuApp.domain.model.UserProfilePayload
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Quran Reflect API (prefix: `quran-reflect/v1`).
 * Mirrors iOS Networking/Endpoints/ReflectEndpoints.swift. Requires user OAuth token.
 */
interface ReflectApiService {

    @GET("users/profile")
    suspend fun getMyProfile(): UserProfilePayload

    @GET("posts/feed")
    suspend fun getPostsFeed(
        @Query("tab") tab: String = "feed",
        @Query("sortBy") sortBy: String = "latest",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("filter[postTypeIds]") postTypeIds: String = "1"
    ): ReflectFeedEnvelope

    @GET("posts/my-posts")
    suspend fun getMyPosts(
        @Query("tab") tab: String = "my_reflections",
        @Query("sortBy") sortBy: String = "latest",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ReflectFeedEnvelope

    @POST("posts")
    suspend fun createPost(
        @Body request: PostCreateRequest,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): PostCreateEnvelope

    @POST("posts/{postId}/toggle-like")
    suspend fun togglePostLike(@Path("postId") postId: String): ReflectToggleLikeResponse

    @POST("activity_days")
    suspend fun logActivityDay(@Body input: ActivityDayInput): ActivityDayEnvelope
}
