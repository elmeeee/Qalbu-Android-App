package app.kamy.qalbuApp.infrastructure.network

import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.infrastructure.auth.OAuthService
import app.kamy.qalbuApp.infrastructure.auth.RefreshTokenManager
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationService
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Provider

/**
 * Adds the user's access token to Reflect / Auth v1 requests, and refreshes on 401.
 * Mirrors iOS QFApiClient.withUser401RefreshRetry.
 *
 * Refresh is single-flight via [RefreshTokenManager] so concurrent 401s don't all
 * race to spend the same refresh token.
 */
class UserAuthInterceptor(
    private val userSession: UserSession,
    private val refreshManager: RefreshTokenManager,
    private val oauthService: OAuthService,
    private val authServiceProvider: Provider<AuthorizationService>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking { userSession.userAccessToken() }
        val req = original.newBuilder()
            .apply { if (!token.isNullOrEmpty()) header("Authorization", "Bearer $token") }
            .header("x-client-id", AppConfig.qfOauthClientId)
            .header("Accept", "application/json")
            .build()
        val response = chain.proceed(req)
        if (response.code != 401) return response

        response.close()
        // Attempt single-flight refresh; if it fails, surface 401 to the caller.
        val refreshed = try {
            runBlocking {
                refreshManager.refreshIfNeeded {
                    oauthService.refreshAccessToken(authServiceProvider.get())
                }
                userSession.userAccessToken()
            }
        } catch (t: Throwable) {
            null
        }

        if (refreshed.isNullOrEmpty()) {
            // Refresh failed — replay original 401 by issuing the same request again
            // (the server will produce its 401 body which the caller can inspect).
            return chain.proceed(req)
        }

        val retried = req.newBuilder()
            .header("Authorization", "Bearer $refreshed")
            .build()
        return chain.proceed(retried)
    }
}
