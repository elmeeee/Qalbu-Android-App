package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.ReadingSession
import app.kamy.qalbuApp.domain.model.ReadingSessionInput
import app.kamy.qalbuApp.infrastructure.network.api.AuthV1ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingSessionRepository @Inject constructor(
    private val api: AuthV1ApiService
) {
    suspend fun fetchMostRecent(): ReadingSession? = qfCall {
        api.listReadingSessions(first = 1).data?.firstOrNull()
    }

    suspend fun logReadingSession(chapterNumber: Int, verseNumber: Int) {
        qfCall { api.logReadingSession(ReadingSessionInput(chapterNumber, verseNumber)) }
    }
}
