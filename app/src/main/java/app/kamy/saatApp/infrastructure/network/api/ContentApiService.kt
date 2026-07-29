package app.kamy.saatApp.infrastructure.network.api

import app.kamy.saatApp.domain.model.HadithsByAyahResponse
import app.kamy.saatApp.domain.model.TafsirResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Quran Foundation Content API.
 *
 * Quran text, chapters, juz, recitations and translations all come from the bundled SQLite
 * database, so only the two resources that are not shipped locally remain here.
 */
interface ContentApiService {

    @GET("tafsirs/{resourceId}/by_ayah/{ayahKey}")
    suspend fun getTafsirByAyah(
        @Path("resourceId") resourceId: String,
        @Path("ayahKey") ayahKey: String
    ): TafsirResponse

    @GET("hadith-references/by-ayah/{ayahKey}/hadiths")
    suspend fun getHadithsByAyah(
        @Path(value = "ayahKey", encoded = true) ayahKey: String,
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5
    ): HadithsByAyahResponse
}
