package app.kamy.qalbuApp.infrastructure.offline

import android.content.Context
import app.kamy.qalbuApp.infrastructure.preferences.OfflineDownloadStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
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

@Singleton
class OfflineQuranDownloader @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository
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

        if (pending.isEmpty()) {
            onProgress(
                OfflineDownloadProgress(
                    completedInBatch = 0,
                    totalInBatch = 0,
                    overallDownloaded = OfflineDownloadStore.downloadedChapterCount(appContext)
                )
            )
            return@withContext Result.success(Unit)
        }

        cancelRequested.set(false)
        OfflineDownloadStore.setInProgress(appContext, true)
        OfflineDownloadStore.setLastError(appContext, null)

        try {
            var batchDone = 0
            val total = pending.size
            for (chapter in pending) {
                if (cancelRequested.get()) {
                    OfflineDownloadStore.setInProgress(appContext, false)
                    return@withContext Result.failure(IllegalStateException("Download cancelled"))
                }
                downloadChapterPages(chapter)
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

            if (includeTranslations) {
                runCatching { contentRepository.getTranslations() }
            }
            runCatching { contentRepository.getJuzs(force = true) }

            if (OfflineDownloadStore.isFullyDownloaded(appContext)) {
                OfflineDownloadStore.markCompleted(appContext)
            } else {
                OfflineDownloadStore.setInProgress(appContext, false)
            }
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
        val chapters = contentRepository.getChapters(force = false).map { it.id }
        return downloadChapters(chapters) { progress ->
            onProgress(progress.overallDownloaded, 114)
        }
    }

    private suspend fun downloadChapterPages(chapter: Int) {
        var page = 1
        val maxPages = 40
        while (page <= maxPages) {
            if (cancelRequested.get()) return
            val response = contentRepository.getVersesByChapter(
                chapterNumber = chapter,
                page = page,
                perPage = 50
            )
            if (response.verses.isEmpty()) break
            val pagination = response.pagination ?: break
            if (!pagination.hasNextPage) break
            val next = pagination.nextPage ?: break
            if (next <= page) break
            page = next
        }
    }
}
