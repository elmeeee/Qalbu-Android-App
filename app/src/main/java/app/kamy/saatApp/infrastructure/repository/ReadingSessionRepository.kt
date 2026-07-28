package app.kamy.saatApp.infrastructure.repository

import android.content.Context
import app.kamy.saatApp.core.error.qfCall
import app.kamy.saatApp.domain.model.LocalReadingProgress
import app.kamy.saatApp.domain.model.ReadingSession
import app.kamy.saatApp.domain.model.ReadingSessionInput
import app.kamy.saatApp.infrastructure.auth.UserSession
import app.kamy.saatApp.infrastructure.network.api.AuthV1ApiService
import app.kamy.saatApp.infrastructure.preferences.LocalReadingProgressStore
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
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
        val device = bestDeviceProgress()?.also {
            LocalReadingProgressStore.save(appContext, it.chapterNumber, it.verseNumber)
        }
        val local = device?.toReadingSession()
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

    private fun bestDeviceProgress(): LocalReadingProgress? {
        val local = LocalReadingProgressStore.load(appContext)
        val personal = QuranPersonalStore.lastReadProgress(appContext)
        return when {
            local == null -> personal
            personal == null -> local
            personal.updatedAtMillis == 0L -> {
                if (
                    personal.chapterNumber != local.chapterNumber ||
                    personal.verseNumber != local.verseNumber
                ) {
                    personal.copy(updatedAtMillis = System.currentTimeMillis())
                } else {
                    local
                }
            }
            personal.updatedAtMillis >= local.updatedAtMillis -> personal
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
