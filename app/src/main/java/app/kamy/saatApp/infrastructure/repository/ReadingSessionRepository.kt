package app.kamy.saatApp.infrastructure.repository

import android.content.Context
import app.kamy.saatApp.domain.model.LocalReadingProgress
import app.kamy.saatApp.domain.model.ReadingSession
import app.kamy.saatApp.infrastructure.preferences.LocalReadingProgressStore
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reading progress ("continue reading") backed entirely by on-device storage.
 *
 * Progress lives in two preference stores: [LocalReadingProgressStore] holds the plain
 * chapter/verse position, while [QuranPersonalStore] tracks it alongside Khatam data. The most
 * recently updated of the two wins.
 */
@Singleton
class ReadingSessionRepository @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    suspend fun fetchMostRecent(): ReadingSession? = withContext(Dispatchers.IO) {
        bestDeviceProgress()
            ?.also { LocalReadingProgressStore.save(appContext, it.chapterNumber, it.verseNumber) }
            ?.toReadingSession()
    }

    suspend fun logReadingSession(chapterNumber: Int, verseNumber: Int) {
        if (chapterNumber <= 0 || verseNumber <= 0) return
        withContext(Dispatchers.IO) {
            LocalReadingProgressStore.save(appContext, chapterNumber, verseNumber)
        }
    }

    private fun bestDeviceProgress(): LocalReadingProgress? {
        val local = LocalReadingProgressStore.load(appContext)
        val personal = QuranPersonalStore.lastReadProgress(appContext)
        return when {
            local == null -> personal
            personal == null -> local
            personal.updatedAtMillis == 0L -> {
                // Legacy Khatam entries carry no timestamp. Treat them as current only when they
                // actually point somewhere else, otherwise the plain store is the better source.
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
}
