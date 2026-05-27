package app.kamy.qalbuApp.di

import android.content.Context
import app.kamy.qalbuApp.BuildConfig
import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.infrastructure.auth.ContentTokenManager
import app.kamy.qalbuApp.infrastructure.auth.OAuthService
import app.kamy.qalbuApp.infrastructure.auth.RefreshTokenManager
import app.kamy.qalbuApp.infrastructure.auth.SecureTokenStorage
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.network.AlAdhanApi
import app.kamy.qalbuApp.infrastructure.network.AuthV1Api
import app.kamy.qalbuApp.infrastructure.network.ContentApi
import app.kamy.qalbuApp.infrastructure.network.ContentAuthInterceptor
import app.kamy.qalbuApp.infrastructure.network.HostFallbackInterceptor
import app.kamy.qalbuApp.infrastructure.network.ReflectApi
import app.kamy.qalbuApp.infrastructure.network.UserAuthInterceptor
import app.kamy.qalbuApp.infrastructure.network.buildRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import net.openid.appauth.AuthorizationService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.chuckerteam.chucker.api.ChuckerInterceptor
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
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideHostFallbackInterceptor(): HostFallbackInterceptor = HostFallbackInterceptor()

    /** Bare OkHttp for OAuth token endpoint requests (no Authorization injection). */
    @Provides
    @Singleton
    @Named("oauth")
    fun provideOauthClient(
        hostFallbackInterceptor: HostFallbackInterceptor,
        logging: HttpLoggingInterceptor,
        @ApplicationContext context: Context
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(hostFallbackInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(ChuckerInterceptor.Builder(context).build())
                }
            }
            .addInterceptor(logging)
            .build()

    /** OkHttp client for Content API (client_credentials token). */
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
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(ChuckerInterceptor.Builder(context).build())
                }
            }
            .addInterceptor(logging)
            .build()

    /** OkHttp client for user-token endpoints (Reflect, Auth v1). */
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
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(ChuckerInterceptor.Builder(context).build())
                }
            }
            .addInterceptor(logging)
            .build()

    /** OkHttp for Al-Adhan public prayer times API (no auth). */
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
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(ChuckerInterceptor.Builder(context).build())
                }
            }
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
        json: Json
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

    /**
     * AppAuth's [AuthorizationService] holds a Chrome Custom Tabs warm-up connection
     * and must be disposed when the Activity dies. We keep a singleton-scoped instance
     * because token refresh happens off-Activity (inside an OkHttp interceptor).
     */
    @Provides
    @Singleton
    fun provideAuthorizationService(@ApplicationContext context: Context): AuthorizationService =
        AuthorizationService(context)
}
