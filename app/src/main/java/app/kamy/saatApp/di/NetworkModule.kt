package app.kamy.saatApp.di

import android.content.Context
import app.kamy.saatApp.BuildConfig
import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.infrastructure.auth.ContentTokenManager
import app.kamy.saatApp.infrastructure.auth.OAuthService
import app.kamy.saatApp.infrastructure.auth.RefreshTokenManager
import app.kamy.saatApp.infrastructure.auth.SecureTokenStorage
import app.kamy.saatApp.infrastructure.auth.UserSession
import app.kamy.saatApp.infrastructure.network.AlAdhanApi
import app.kamy.saatApp.infrastructure.network.AuthV1Api
import app.kamy.saatApp.infrastructure.network.ContentApi
import app.kamy.saatApp.infrastructure.network.ContentAuthInterceptor
import app.kamy.saatApp.infrastructure.network.HostFallbackInterceptor
import app.kamy.saatApp.infrastructure.network.ReflectApi
import app.kamy.saatApp.infrastructure.network.ReflectJson
import app.kamy.saatApp.infrastructure.network.UserAuthInterceptor
import app.kamy.saatApp.infrastructure.network.NetworkDebugger
import app.kamy.saatApp.infrastructure.network.buildRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import net.openid.appauth.AuthorizationService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @ReflectJson
    fun provideReflectJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

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
    fun provideOauthClient(
        hostFallbackInterceptor: HostFallbackInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(hostFallbackInterceptor)
            .let(NetworkDebugger::applyTo)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @ContentApi
    fun provideContentOkHttp(
        @ApplicationContext context: Context,
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
    @Named("user")
    fun provideUserOkHttp(
        @ApplicationContext context: Context,
        userSession: UserSession,
        refreshManager: RefreshTokenManager,
        oauthService: OAuthService,
        authServiceProvider: Provider<AuthorizationService>,
        hostFallbackInterceptor: HostFallbackInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(hostFallbackInterceptor)
            .addInterceptor(
                UserAuthInterceptor(userSession, refreshManager, oauthService, authServiceProvider)
            )
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
    @AlAdhanApi
    fun provideAlAdhanOkHttp(
        @ApplicationContext context: Context,
        logging: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
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

    @Provides
    @Singleton
    @ReflectApi
    fun provideReflectRetrofit(
        @Named("user") okHttp: OkHttpClient,
        @ReflectJson json: Json
    ): Retrofit = buildRetrofit(
        baseUrl = AppConfig.qfApiBaseUrl,
        prefix = AppConfig.Prefix.quranReflect,
        okHttpClient = okHttp,
        json = json
    )

    @Provides
    @Singleton
    @AuthV1Api
    fun provideAuthV1Retrofit(
        @Named("user") okHttp: OkHttpClient,
        json: Json
    ): Retrofit = buildRetrofit(
        baseUrl = AppConfig.qfApiBaseUrl,
        prefix = AppConfig.Prefix.authV1,
        okHttpClient = okHttp,
        json = json
    )

    @Provides
    @Singleton
    @AlAdhanApi
    fun provideAlAdhanRetrofit(
        @AlAdhanApi okHttp: OkHttpClient,
        json: Json
    ): Retrofit = buildRetrofit(
        baseUrl = AppConfig.alAdhanRoot,
        prefix = "v1",
        okHttpClient = okHttp,
        json = json
    )

    @Provides
    @Singleton
    fun provideAuthorizationService(@ApplicationContext context: Context): AuthorizationService =
        AuthorizationService(context)
}
