package app.kamy.qalbuApp.infrastructure.network

import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.core.error.QFError
import app.kamy.qalbuApp.infrastructure.auth.ContentTokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Adds `Authorization: Bearer <content-token>` and `x-auth-token` / `x-client-id` headers
 * required by the Quran Foundation Content API. Mirrors iOS QFHeadersManager.
 *
 * The Content API uses the **same** access token in both `Authorization: Bearer …` and
 * `x-auth-token` per QF's reference docs.
 */
class ContentAuthInterceptor(
    private val tokenManager: ContentTokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlockingToken { tokenManager.accessToken() }
        val req = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("x-auth-token", token)
            .header("x-client-id", AppConfig.qfOauthClientId)
            .header("Accept", "application/json")
            .build()
        val response = chain.proceed(req)
        if (response.code == 401) {
            // Invalidate cached content token so the next request fetches a fresh one.
            runBlocking { tokenManager.clearCache() }
            response.close()
            val refreshed = runBlockingToken { tokenManager.accessToken() }
            val retried = req.newBuilder()
                .header("Authorization", "Bearer $refreshed")
                .header("x-auth-token", refreshed)
                .build()
            return chain.proceed(retried)
        }
        return response
    }

    /**
     * OkHttp runs interceptors on a background thread; uncaught [QFError] there
     * kills the process. Map failures to [IOException] so Retrofit/callers can handle them.
     */
    private fun <T> runBlockingToken(block: suspend () -> T): T = try {
        runBlocking { block() }
    } catch (e: QFError) {
        throw IOException(e.message ?: "Content token unavailable", e)
    } catch (e: Throwable) {
        throw IOException(e.message ?: "Content token request failed", e)
    }
}
