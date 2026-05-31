package app.kamy.qalbuApp.infrastructure.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshTokenManager @Inject constructor() {
    private val mutex = Mutex()

    suspend fun refreshIfNeeded(refresh: suspend () -> Unit) {
        mutex.withLock {
            refresh()
        }
    }
}
