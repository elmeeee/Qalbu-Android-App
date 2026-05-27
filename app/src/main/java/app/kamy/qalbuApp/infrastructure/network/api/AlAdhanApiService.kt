package app.kamy.qalbuApp.infrastructure.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.serialization.Serializable

/**
 * Al-Adhan public prayer times API (no auth, base `https://api.aladhan.com/v1`).
 * Mirrors iOS Core/Configuration/AppEndpoints.URLBuilder.alAdhanTimings.
 *
 * Method IDs follow Al-Adhan docs (Muhammadiyah/Shafi defaults wired in Phase 2).
 */
interface AlAdhanApiService {

    @GET("methods")
    suspend fun getMethods(): AlAdhanMethodsResponse

    @GET("timings/{timestamp}")
    suspend fun getTimings(
        @Path("timestamp") timestamp: Long,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("school") school: Int = 0,
        @Query("tune") tune: String,
        @Query("methodSettings") methodSettings: String? = null
    ): AlAdhanResponse
}

@Serializable
data class AlAdhanMethodsResponse(
    val code: Int? = null,
    val status: String? = null,
    val data: Map<String, AlAdhanMethodEntry>? = null
)

@Serializable
data class AlAdhanMethodEntry(
    val id: Int,
    val name: String? = null
)

@Serializable
data class AlAdhanResponse(
    val code: Int? = null,
    val status: String? = null,
    val data: AlAdhanData? = null
)

@Serializable
data class AlAdhanData(
    val timings: Map<String, String>? = null,
    val date: AlAdhanDate? = null,
    val meta: AlAdhanMeta? = null
)

@Serializable
data class AlAdhanDate(
    val readable: String? = null,
    val timestamp: String? = null,
    val gregorian: AlAdhanGregorian? = null,
    val hijri: AlAdhanHijri? = null
)

@Serializable
data class AlAdhanCalendarMonth(
    val number: Int? = null,
    val en: String? = null
)

@Serializable
data class AlAdhanGregorian(
    val date: String? = null,
    val format: String? = null,
    val day: String? = null,
    val month: AlAdhanCalendarMonth? = null,
    val year: String? = null
)

@Serializable
data class AlAdhanHijri(
    val date: String? = null,
    val format: String? = null,
    val day: String? = null,
    val month: AlAdhanCalendarMonth? = null,
    val year: String? = null
)

@Serializable
data class AlAdhanMeta(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null
)
