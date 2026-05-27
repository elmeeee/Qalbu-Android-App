package app.kamy.qalbuApp.infrastructure.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors iOS Infrastructure/Networking/QFRefreshTokenManager.swift.
 *
 * Deduplicates concurrent token refresh requests: while one coroutine is
 * refreshing, other 401-retry coroutines wait for the same result instead of
 * each kicking off their own refresh (which would fail with `invalid_grant`
 * once the refresh token rotates).
 */
@Singleton
class RefreshTokenManager @Inject constructor() {
    private val mutex = Mutex()

    suspend fun refreshIfNeeded(refresh: suspend () -> Unit) {
        mutex.withLock {
            refresh()
        }
    }
}
