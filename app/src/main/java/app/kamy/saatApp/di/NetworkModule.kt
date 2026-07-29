package app.kamy.saatApp.di

import app.kamy.saatApp.BuildConfig
import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.infrastructure.auth.ContentTokenManager
import app.kamy.saatApp.infrastructure.network.ContentApi
import app.kamy.saatApp.infrastructure.network.ContentAuthInterceptor
import app.kamy.saatApp.infrastructure.network.HostFallbackInterceptor
import app.kamy.saatApp.infrastructure.network.NetworkDebugger
import app.kamy.saatApp.infrastructure.network.buildRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
        namingStrategy = kotlinx.serialization.json.JsonNamingStrategy.SnakeCase
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideHostFallbackInterceptor(): HostFallbackInterceptor = HostFallbackInterceptor()

    @Provides
    @Singleton
    @Named("oauth")
    fun provideOAuthOkHttp(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .let(NetworkDebugger::applyTo)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @ContentApi
    fun provideContentOkHttp(
        tokenManager: ContentTokenManager,
        hostFallbackInterceptor: HostFallbackInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(hostFallbackInterceptor)
            .addInterceptor(ContentAuthInterceptor(tokenManager))
            .let(NetworkDebugger::applyTo)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @Named("groq")
    fun provideGroqOkHttp(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .let(NetworkDebugger::applyTo)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @ContentApi
    fun provideContentRetrofit(
        @ContentApi okHttp: OkHttpClient,
        json: Json
    ): Retrofit = buildRetrofit(
        baseUrl = AppConfig.qfApiBaseUrl,
        prefix = AppConfig.Prefix.contentAPI,
        okHttpClient = okHttp,
        json = json
    )

}
