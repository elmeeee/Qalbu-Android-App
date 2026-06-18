package app.kamy.qalbuApp.infrastructure.offline

import android.content.Context
import app.kamy.qalbuApp.infrastructure.preferences.OfflineDownloadStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OfflineDownloadProgress(
    val completedInBatch: Int,
    val totalInBatch: Int,
    val overallDownloaded: Int,
    val currentChapter: Int? = null
)

/**
 * Quran text is bundled in assets — mark all chapters available without network.
 */
@Singleton
class OfflineQuranDownloader @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val cancelRequested = AtomicBoolean(false)

    fun requestCancel() {
        cancelRequested.set(true)
    }

    suspend fun downloadChapters(
        chapterIds: List<Int>,
        includeTranslations: Boolean = true,
        onProgress: (OfflineDownloadProgress) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        OfflineDownloadStore.recoverStaleProgress(appContext)
        if (OfflineDownloadStore.isInProgress(appContext)) {
            return@withContext Result.failure(IllegalStateException("Download already in progress"))
        }

        val pending = chapterIds
            .distinct()
            .sorted()
            .filter { it in 1..114 && !OfflineDownloadStore.isChapterDownloaded(appContext, it) }

        cancelRequested.set(false)
        OfflineDownloadStore.setInProgress(appContext, true)
        OfflineDownloadStore.setLastError(appContext, null)

        try {
            var batchDone = 0
            val total = pending.size.coerceAtLeast(1)
            if (pending.isEmpty()) {
                ensureAllChaptersMarked()
                onProgress(
                    OfflineDownloadProgress(
                        completedInBatch = 0,
                        totalInBatch = 0,
                        overallDownloaded = OfflineDownloadStore.downloadedChapterCount(appContext)
                    )
                )
                OfflineDownloadStore.markCompleted(appContext)
                return@withContext Result.success(Unit)
            }
            for (chapter in pending) {
                if (cancelRequested.get()) {
                    OfflineDownloadStore.setInProgress(appContext, false)
                    return@withContext Result.failure(IllegalStateException("Download cancelled"))
                }
                OfflineDownloadStore.markChapterDownloaded(appContext, chapter)
                OfflineDownloadStore.touchProgress(appContext)
                batchDone++
                onProgress(
                    OfflineDownloadProgress(
                        completedInBatch = batchDone,
                        totalInBatch = total,
                        overallDownloaded = OfflineDownloadStore.downloadedChapterCount(appContext),
                        currentChapter = chapter
                    )
                )
            }
            OfflineDownloadStore.markCompleted(appContext)
            Result.success(Unit)
        } catch (t: Throwable) {
            OfflineDownloadStore.setInProgress(appContext, false)
            OfflineDownloadStore.setLastError(appContext, t.message)
            Result.failure(t)
        }
    }

    suspend fun downloadAllChapters(
        onProgress: (downloaded: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<Unit> {
        ensureAllChaptersMarked()
        onProgress(114, 114)
        return downloadChapters((1..114).toList()) { progress ->
            onProgress(progress.overallDownloaded, 114)
        }
    }

    private fun ensureAllChaptersMarked() {
        for (chapter in 1..114) {
            OfflineDownloadStore.markChapterDownloaded(appContext, chapter)
        }
        OfflineDownloadStore.markCompleted(appContext)
    }
}
