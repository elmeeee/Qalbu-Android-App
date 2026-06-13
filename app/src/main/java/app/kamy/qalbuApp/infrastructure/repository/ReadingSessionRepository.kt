package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.ReadingSession
import app.kamy.qalbuApp.domain.model.ReadingSessionInput
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.network.api.AuthV1ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingSessionRepository @Inject constructor(
    private val api: AuthV1ApiService,
    private val userSession: UserSession
) {
    suspend fun fetchMostRecent(): ReadingSession? {
        if (!userSession.isSignedIn.value) return null
        return qfCall { api.listReadingSessions(first = 1).data?.firstOrNull() }
    }

    suspend fun logReadingSession(chapterNumber: Int, verseNumber: Int) {
        if (!userSession.isSignedIn.value) return
        qfCall { api.logReadingSession(ReadingSessionInput(chapterNumber, verseNumber)) }
    }
}
