package app.kamy.qalbuApp.infrastructure.network

import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.core.error.isAuthHttpFailure
import app.kamy.qalbuApp.infrastructure.auth.OAuthService
import app.kamy.qalbuApp.infrastructure.auth.RefreshTokenManager
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationService
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Provider

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
            .apply { applyUserAuthHeaders(token) }
            .build()
        val response = chain.proceed(req)
        if (!response.needsAuthRefresh()) return response

        response.close()
        val refreshed = try {
            runBlocking {
                refreshManager.refreshIfNeeded {
                    oauthService.refreshAccessToken(authServiceProvider.get())
                }
                userSession.userAccessToken()
            }
        } catch (_: Throwable) {
            runBlocking { userSession.clear() }
            null
        }

        if (refreshed.isNullOrEmpty()) {
            return chain.proceed(req)
        }

        val retried = original.newBuilder()
            .apply { applyUserAuthHeaders(refreshed) }
            .build()
        return chain.proceed(retried)
    }

    private fun Response.needsAuthRefresh(): Boolean =
        isAuthHttpFailure(code, peekBody(PEEK_BODY_BYTES).string())

    private fun okhttp3.Request.Builder.applyUserAuthHeaders(token: String?) {
        header("x-client-id", AppConfig.qfOauthClientId)
        header("Accept", "application/json")
        if (!token.isNullOrEmpty()) {
            header("x-auth-token", token)
            header("Authorization", "Bearer $token")
        }
    }

    private companion object {
        private const val PEEK_BODY_BYTES = 64L * 1024L
    }
}
