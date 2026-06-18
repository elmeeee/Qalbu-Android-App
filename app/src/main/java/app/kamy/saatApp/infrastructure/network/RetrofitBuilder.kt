package app.kamy.saatApp.infrastructure.network

import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter
import retrofit2.Retrofit
import kotlinx.serialization.json.Json
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

internal fun buildRetrofit(
    baseUrl: String,
    prefix: String?,
    okHttpClient: okhttp3.OkHttpClient,
    json: Json
): Retrofit {
    val withSlash = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
    val effective = if (prefix.isNullOrEmpty()) withSlash else "$withSlash$prefix/"
    val factory: Converter.Factory = json.asConverterFactory("application/json".toMediaType())
    return Retrofit.Builder()
        .baseUrl(effective)
        .client(okHttpClient)
        .addConverterFactory(factory)
        .build()
}
