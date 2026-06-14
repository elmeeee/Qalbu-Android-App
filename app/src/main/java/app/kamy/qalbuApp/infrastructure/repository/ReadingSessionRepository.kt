package app.kamy.qalbuApp.infrastructure.repository

import android.content.Context
import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.ReadingSession
import app.kamy.qalbuApp.domain.model.ReadingSessionInput
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.network.api.AuthV1ApiService
import app.kamy.qalbuApp.infrastructure.preferences.LocalReadingProgressStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingSessionRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val api: AuthV1ApiService,
    private val userSession: UserSession
) {
    suspend fun fetchMostRecent(): ReadingSession? {
        val local = LocalReadingProgressStore.load(appContext)?.toReadingSession()
        val cloud = if (userSession.isSignedIn.value) {
            runCatching { qfCall { api.listReadingSessions(first = 1).data?.firstOrNull() } }.getOrNull()
        } else {
            null
        }
        return when {
            local == null -> cloud
            cloud == null -> local
            else -> {
                val localTime = local.updatedAt?.toLongOrNull() ?: 0L
                val cloudTime = cloud.updatedAt?.toLongOrNull()
                    ?: cloud.updatedAt?.let { parseIsoMillis(it) }
                    ?: 0L
                if (cloudTime >= localTime) cloud else local
            }
        }
    }

    suspend fun logReadingSession(chapterNumber: Int, verseNumber: Int) {
        if (chapterNumber <= 0 || verseNumber <= 0) return
        LocalReadingProgressStore.save(appContext, chapterNumber, verseNumber)
        if (!userSession.isSignedIn.value) return
        runCatching { qfCall { api.logReadingSession(ReadingSessionInput(chapterNumber, verseNumber)) } }
    }

    private fun parseIsoMillis(raw: String): Long = runCatching {
        java.time.Instant.parse(raw).toEpochMilli()
    }.getOrDefault(0L)
}
