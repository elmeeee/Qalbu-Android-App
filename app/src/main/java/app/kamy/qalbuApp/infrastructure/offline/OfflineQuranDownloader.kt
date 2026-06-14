package app.kamy.qalbuApp.infrastructure.offline

import android.content.Context
import app.kamy.qalbuApp.infrastructure.preferences.OfflineDownloadStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class OfflineQuranDownloader @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository
) {
    suspend fun downloadAllChapters(
        onProgress: (downloaded: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (OfflineDownloadStore.isInProgress(appContext)) {
            return@withContext Result.failure(IllegalStateException("Download already in progress"))
        }
        OfflineDownloadStore.setInProgress(appContext, true)
        OfflineDownloadStore.setLastError(appContext, null)
        try {
            val chapters = contentRepository.getChapters(force = false)
            val total = chapters.size.coerceAtLeast(114)
            var downloaded = OfflineDownloadStore.downloadedChapterCount(appContext)
            onProgress(downloaded, total)
            for (chapter in chapters.sortedBy { it.id }) {
                if (OfflineDownloadStore.isChapterDownloaded(appContext, chapter.id)) continue
                var page = 1
                while (true) {
                    val response = contentRepository.getVersesByChapter(
                        chapterNumber = chapter.id,
                        page = page,
                        perPage = 50
                    )
                    val pagination = response.pagination
                    if (pagination?.hasNextPage != true) break
                    page = pagination.nextPage ?: break
                }
                OfflineDownloadStore.markChapterDownloaded(appContext, chapter.id)
                downloaded++
                onProgress(downloaded, total)
            }
            runCatching { contentRepository.getJuzs(force = true) }
            OfflineDownloadStore.markCompleted(appContext)
            Result.success(Unit)
        } catch (t: Throwable) {
            OfflineDownloadStore.setInProgress(appContext, false)
            OfflineDownloadStore.setLastError(appContext, t.message)
            Result.failure(t)
        }
    }
}
