package app.kamy.qalbuApp.infrastructure.auth

import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.core.error.QFError
import app.kamy.qalbuApp.domain.model.TokenResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Credentials
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ContentTokenManager @Inject constructor(
    @Named("oauth") private val oauthClient: OkHttpClient,
    private val storage: SecureTokenStorage,
    private val json: Json
) {
    private val mutex = Mutex()

    private val expirySkewSec = 60L

    suspend fun accessToken(): String = mutex.withLock {
        val cached = storage.read(SecureTokenStorage.Keys.CONTENT_ACCESS_TOKEN)
        val expiry = storage.read(SecureTokenStorage.Keys.CONTENT_ACCESS_EXPIRY)?.toLongOrNull() ?: 0L
        val nowSec = System.currentTimeMillis() / 1000L
        if (!cached.isNullOrEmpty() && expiry > nowSec + expirySkewSec) {
            return@withLock cached
        }
        fetchAndPersist()
    }

    suspend fun clearCache() = mutex.withLock {
        storage.remove(
            SecureTokenStorage.Keys.CONTENT_ACCESS_TOKEN,
            SecureTokenStorage.Keys.CONTENT_ACCESS_EXPIRY
        )
    }

    private fun fetchAndPersist(): String {
        val clientId = AppConfig.qfOauthClientId
        val secret = AppConfig.qfOauthClientSecret
            ?: throw QFError.MissingContentToken

        val formBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("scope", "content")
            .build()

        val req = Request.Builder()
            .url(AppConfig.qfOauthTokenUrl)
            .post(formBody)
            .addHeader("Authorization", Credentials.basic(clientId, secret))
            .addHeader("Accept", "application/json")
            .build()

        oauthClient.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw QFError.HttpStatus(resp.code, body)
            }
            val token = try {
                json.decodeFromString(TokenResponse.serializer(), body)
            } catch (t: Throwable) {
                throw QFError.Parsing("content token: ${t.message}")
            }
            val nowSec = System.currentTimeMillis() / 1000L
            val expiryEpoch = nowSec + token.expiresIn
            storage.write(SecureTokenStorage.Keys.CONTENT_ACCESS_TOKEN, token.accessToken)
            storage.write(SecureTokenStorage.Keys.CONTENT_ACCESS_EXPIRY, expiryEpoch.toString())
            return token.accessToken
        }
    }
}
