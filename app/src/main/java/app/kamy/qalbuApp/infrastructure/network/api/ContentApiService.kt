package app.kamy.qalbuApp.infrastructure.network.api

import app.kamy.qalbuApp.domain.model.ChaptersResponse
import app.kamy.qalbuApp.domain.model.JuzsResponse
import app.kamy.qalbuApp.domain.model.PagesLookupResponse
import app.kamy.qalbuApp.domain.model.SingleJuzResponse
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

    @GET("juzs")
    suspend fun getJuzs(
        @Query("mushaf") mushaf: Int? = null
    ): JuzsResponse

    @GET("juzs/{id}")
    suspend fun getJuzById(
        @Path("id") juzNumber: Int,
        @Query("mushaf") mushaf: Int? = null
    ): SingleJuzResponse

    @GET("verses/random")
    suspend fun getRandomVerse(
        @Query("language") language: String = "en",
        @Query("translations") translations: String = "22",
        @Query("audio") audio: Int = 6,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translation_fields") translationFields: String = "resource_name"
    ): RandomAyahResponse

    @GET("verses/by_juz/{juz_number}")
    suspend fun getVersesByJuz(
        @Path("juz_number") juzNumber: Int,
        @Query("language") language: String = "en",
        @Query("translations") translations: String = "22",
        @Query("audio") audio: Int = 6,
        @Query("fields") fields: String = "text_uthmani",
        @Query("translation_fields") translationFields: String = "resource_name",
        @Query("words") words: String = "false",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50
    ): VersesByChapterResponse

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

    @GET("verses/by_page/{page_number}")
    suspend fun getVersesByPage(
        @Path("page_number") pageNumber: Int,
        @Query("language") language: String = "en",
        @Query("mushaf") mushaf: Int = 1,
        @Query("translations") translations: String = "22",
        @Query("audio") audio: Int = 6,
        @Query("fields") fields: String = "text_uthmani,text_uthmani_tajweed",
        @Query("translation_fields") translationFields: String = "resource_name",
        @Query("words") words: String = "true",
        @Query("word_fields") wordFields: String = "text_uthmani,text_uthmani_tajweed,line_number,page_number,char_type_name",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50
    ): VersesByChapterResponse

    @GET("pages/lookup")
    suspend fun getPagesLookup(
        @Query("mushaf") mushaf: Int = 1,
        @Query("chapter_number") chapterNumber: Int? = null,
        @Query("juz_number") juzNumber: Int? = null,
        @Query("page_number") pageNumber: Int? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): PagesLookupResponse

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

    @GET("hadith-references/by-ayah/{ayahKey}/hadiths")
    suspend fun getHadithsByAyah(
        @Path(value = "ayahKey", encoded = true) ayahKey: String,
        @Query("language") language: String = "en",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 5
    ): HadithsByAyahResponse
}
