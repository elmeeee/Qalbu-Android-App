package app.kamy.saatApp.infrastructure.network.api

import app.kamy.saatApp.domain.model.QfSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {

    @GET("search")
    suspend fun search(
        @Query("mode") mode: String,
        @Query("query") query: String,
        @Query("navigationalResultsNumber") navigationalResultsNumber: Int? = null,
        @Query("versesResultsNumber") versesResultsNumber: Int? = null,
        @Query("translation_ids") translationIds: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("highlight") highlight: Int? = null
    ): QfSearchResponse
}
