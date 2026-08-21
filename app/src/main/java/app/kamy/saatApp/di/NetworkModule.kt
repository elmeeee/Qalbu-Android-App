package app.kamy.saatApp.di

import app.kamy.saatApp.BuildConfig
import app.kamy.saatApp.infrastructure.network.HostFallbackInterceptor
import app.kamy.saatApp.infrastructure.network.NetworkDebugger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
    @Named("groq")
    fun provideGroqOkHttp(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .let(NetworkDebugger::applyTo)
            .addInterceptor(logging)
            .build()
}
