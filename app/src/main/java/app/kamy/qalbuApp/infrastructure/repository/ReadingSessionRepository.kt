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
        if (!userSession.isSignedIn.value) return local

        val cloud = runCatching {
            qfCall { api.listReadingSessions(first = 1).data?.firstOrNull() }
        }.getOrNull()
        return mergeSessions(local, cloud)?.also { best ->
            persistMerged(best, local, cloud)
        }
    }

    suspend fun logReadingSession(chapterNumber: Int, verseNumber: Int) {
        if (chapterNumber <= 0 || verseNumber <= 0) return
        LocalReadingProgressStore.save(appContext, chapterNumber, verseNumber)
        if (!userSession.isSignedIn.value) return
        runCatching { qfCall { api.logReadingSession(ReadingSessionInput(chapterNumber, verseNumber)) } }
    }

    suspend fun syncAfterSignIn() {
        if (!userSession.isSignedIn.value) return
        fetchMostRecent()
    }

    private suspend fun persistMerged(
        best: ReadingSession,
        local: ReadingSession?,
        cloud: ReadingSession?
    ) {
        LocalReadingProgressStore.save(appContext, best.chapterNumber, best.verseNumber)
        val localTime = local?.updatedAtMillis() ?: 0L
        val cloudTime = cloud?.updatedAtMillis() ?: 0L
        if (localTime > cloudTime && userSession.isSignedIn.value) {
            runCatching {
                qfCall {
                    api.logReadingSession(
                        ReadingSessionInput(best.chapterNumber, best.verseNumber)
                    )
                }
            }
        }
    }

    private fun mergeSessions(local: ReadingSession?, cloud: ReadingSession?): ReadingSession? {
        return when {
            local == null -> cloud
            cloud == null -> local
            cloud.updatedAtMillis() >= local.updatedAtMillis() -> cloud
            else -> local
        }
    }

    private fun ReadingSession.updatedAtMillis(): Long {
        updatedAt?.toLongOrNull()?.let { return it }
        return parseIsoMillis(updatedAt.orEmpty())
    }

    private fun parseIsoMillis(raw: String): Long = runCatching {
        java.time.Instant.parse(raw).toEpochMilli()
    }.getOrDefault(0L)
}
