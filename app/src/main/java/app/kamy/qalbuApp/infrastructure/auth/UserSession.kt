package app.kamy.qalbuApp.infrastructure.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors iOS Infrastructure/Networking/QFUserSession.swift.
 *
 * Holds the signed-in user's access/refresh tokens and exposes them as
 * Flows so Compose can react to auth state changes.
 */
@Singleton
class UserSession @Inject constructor(
    private val storage: SecureTokenStorage
) {
    private val mutex = Mutex()

    private val _isSignedIn = MutableStateFlow(storage.read(SecureTokenStorage.Keys.USER_ACCESS_TOKEN) != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    suspend fun userAccessToken(): String? = mutex.withLock {
        storage.read(SecureTokenStorage.Keys.USER_ACCESS_TOKEN)
    }

    suspend fun userRefreshToken(): String? = mutex.withLock {
        storage.read(SecureTokenStorage.Keys.USER_REFRESH_TOKEN)
    }

    suspend fun userIdToken(): String? = mutex.withLock {
        storage.read(SecureTokenStorage.Keys.USER_ID_TOKEN)
    }

    suspend fun setTokens(
        accessToken: String,
        refreshToken: String? = null,
        idToken: String? = null,
        expiresAtEpochSec: Long? = null
    ) = mutex.withLock {
        storage.write(SecureTokenStorage.Keys.USER_ACCESS_TOKEN, accessToken)
        if (refreshToken != null) {
            storage.write(SecureTokenStorage.Keys.USER_REFRESH_TOKEN, refreshToken)
        }
        if (idToken != null) {
            storage.write(SecureTokenStorage.Keys.USER_ID_TOKEN, idToken)
        }
        if (expiresAtEpochSec != null) {
            storage.write(SecureTokenStorage.Keys.USER_ACCESS_EXPIRY, expiresAtEpochSec.toString())
        }
        _isSignedIn.value = true
    }

    suspend fun clear() = mutex.withLock {
        storage.remove(
            SecureTokenStorage.Keys.USER_ACCESS_TOKEN,
            SecureTokenStorage.Keys.USER_REFRESH_TOKEN,
            SecureTokenStorage.Keys.USER_ID_TOKEN,
            SecureTokenStorage.Keys.USER_ACCESS_EXPIRY
        )
        _isSignedIn.value = false
    }
}
