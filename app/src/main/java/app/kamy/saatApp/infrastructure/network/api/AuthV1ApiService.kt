package app.kamy.saatApp.infrastructure.network.api

import app.kamy.saatApp.domain.model.ReadingSessionInput
import app.kamy.saatApp.domain.model.ReadingSessionsPage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthV1ApiService {

    @GET("reading-sessions")
    suspend fun listReadingSessions(
        @Query("first") first: Int? = null,
        @Query("after") after: String? = null,
        @Query("last") last: Int? = null,
        @Query("before") before: String? = null
    ): ReadingSessionsPage

    @POST("reading-sessions")
    suspend fun logReadingSession(@Body input: ReadingSessionInput): ReadingSessionsPage
}
