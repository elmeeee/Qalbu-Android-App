package app.kamy.qalbuApp.infrastructure.network.api

import app.kamy.qalbuApp.domain.model.ChaptersResponse
import app.kamy.qalbuApp.domain.model.HadithsByAyahResponse
import app.kamy.qalbuApp.domain.model.RandomAyahResponse
import app.kamy.qalbuApp.domain.model.RecitationsResponse
import app.kamy.qalbuApp.domain.model.SingleVerseResponse
import app.kamy.qalbuApp.domain.model.TafsirResponse
import app.kamy.qalbuApp.domain.model.TranslationsResponse
import app.kamy.qalbuApp.domain.model.VersesByChapterResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ContentApiService {

    @GET("chapters")
    suspend fun getChapters(
        @Query("language") language: String = "en"
    ): ChaptersResponse

    @GET("verses/random")
    suspend fun getRandomVerse(
        @Query("language") language: String = "en",
        @Query("translations") translations: String = "22",
        @Query("audio") audio: Int = 6,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translation_fields") translationFields: String = "resource_name"
    ): RandomAyahResponse

    @GET("verses/by_chapter/{chapter}")
    suspend fun getVersesByChapter(
        @Path("chapter") chapterNumber: Int,
        @Query("language") language: String = "en",
        @Query("translations") translations: String = "22",
        @Query("audio") audio: Int = 6,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translation_fields") translationFields: String = "resource_name",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50
    ): VersesByChapterResponse

    @GET("verses/by_key/{key}")
    suspend fun getVerseByKey(
        @Path("key") verseKey: String,
        @Query("language") language: String = "en",
        @Query("translations") translations: String = "22",
        @Query("audio") audio: Int = 6,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translation_fields") translationFields: String = "resource_name"
    ): SingleVerseResponse

    @GET("resources/recitations")
    suspend fun getRecitations(
        @Query("language") language: String = "en"
    ): RecitationsResponse

    @GET("resources/translations")
    suspend fun getTranslations(
        @Query("language") language: String = "en"
    ): TranslationsResponse

    @GET("tafsirs/{resourceId}/by_ayah/{ayahKey}")
    suspend fun getTafsirByAyah(
        @Path("resourceId") resourceId: String,
        @Path("ayahKey") ayahKey: String
    ): TafsirResponse

    @GET("hadith_references/by_ayah/{ayahKey}/hadiths")
    suspend fun getHadithsByAyah(
        @Path("ayahKey") ayahKey: String,
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5
    ): HadithsByAyahResponse
}
