package app.kamy.saatApp.infrastructure.network.api

import app.kamy.saatApp.domain.model.ActivityDayEnvelope
import app.kamy.saatApp.domain.model.ActivityDayInput
import app.kamy.saatApp.domain.model.PostCreateEnvelope
import app.kamy.saatApp.domain.model.PostCreateRequest
import app.kamy.saatApp.domain.model.ReflectFeedEnvelope
import app.kamy.saatApp.domain.model.ReflectToggleLikeResponse
import app.kamy.saatApp.domain.model.UserProfilePayload
import app.kamy.saatApp.domain.model.ToggleFollowRequest
import app.kamy.saatApp.domain.model.ToggleFollowResponse
import app.kamy.saatApp.domain.model.UserFollowersEnvelope
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReflectApiService {

    @GET("users/profile")
    suspend fun getMyProfile(): UserProfilePayload

    @GET("posts/feed")
    suspend fun getPostsFeed(
        @Query("tab") tab: String = "feed",
        @Query("sortBy") sortBy: String = "latest",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("filter[postTypeIds]") postTypeIds: String? = null
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

    @GET("users/{id}/followers")
    suspend fun getUserFollowers(
        @Path("id") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): UserFollowersEnvelope

    @POST("users/{followeeId}/toggle-follow")
    suspend fun toggleFollow(
        @Path("followeeId") followeeId: String,
        @Body request: ToggleFollowRequest
    ): ToggleFollowResponse

    @POST("activity_days")
    suspend fun logActivityDay(@Body input: ActivityDayInput): ActivityDayEnvelope
}
