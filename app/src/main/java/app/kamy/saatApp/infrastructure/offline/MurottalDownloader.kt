package app.kamy.saatApp.infrastructure.offline

import android.content.Context
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.infrastructure.local.LocalQuranDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MurottalDownloadProgress(
    val completedInBatch: Int,
    val totalInBatch: Int,
    val overallDownloaded: Int,
    val currentGlobalAyah: Int? = null,
    val recitationId: Int
)

@Singleton
class MurottalDownloader @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val database: LocalQuranDatabase
) {
    private val cancelRequested = AtomicBoolean(false)

    fun requestCancel() {
        cancelRequested.set(true)
    }

    suspend fun downloadChapters(
        recitationId: Int,
        chapterIds: List<Int>,
        onProgress: (MurottalDownloadProgress) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        MurottalOfflineStore.recoverStaleProgress(appContext)
        if (MurottalOfflineStore.isInProgress(appContext)) {
            return@withContext Result.failure(IllegalStateException("Download already in progress"))
        }

        val globalAyahs = globalAyahNumbersForChapters(chapterIds)
        downloadAyahs(recitationId, globalAyahs, onProgress)
    }

    suspend fun downloadAll(
        recitationId: Int,
        onProgress: (MurottalDownloadProgress) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val all = globalAyahNumbersForChapters((1..114).toList())
        downloadAyahs(recitationId, all, onProgress)
    }

    private suspend fun downloadAyahs(
        recitationId: Int,
        globalAyahs: List<Int>,
        onProgress: (MurottalDownloadProgress) -> Unit
    ): Result<Unit> {
        val pending = globalAyahs
            .distinct()
            .sorted()
            .filter { !MurottalOfflineStore.isAyahDownloaded(appContext, recitationId, it) }

        cancelRequested.set(false)
        MurottalOfflineStore.setInProgress(appContext, true)
        MurottalOfflineStore.setLastError(appContext, null)
        MurottalOfflineStore.setActiveReciter(appContext, recitationId)

        try {
            var batchDone = 0
            val total = pending.size.coerceAtLeast(1)
            if (pending.isEmpty()) {
                val downloaded = MurottalOfflineStore.downloadedCount(appContext, recitationId)
                onProgress(
                    MurottalDownloadProgress(
                        completedInBatch = 0,
                        totalInBatch = 0,
                        overallDownloaded = downloaded,
                        recitationId = recitationId
                    )
                )
                MurottalOfflineStore.setInProgress(appContext, false)
                return Result.success(Unit)
            }

            for (globalAyah in pending) {
                if (cancelRequested.get()) {
                    MurottalOfflineStore.setInProgress(appContext, false)
                    return Result.failure(IllegalStateException("Download cancelled"))
                }
                val remoteUrl = LocalQuranConfig.murottalUrl(recitationId, globalAyah)
                val target = MurottalOfflineStore.ayahFile(appContext, recitationId, globalAyah)
                downloadFile(remoteUrl, target)
                batchDone++
                val overall = MurottalOfflineStore.downloadedCount(appContext, recitationId)
                MurottalOfflineStore.updateProgress(appContext, overall, 6236)
                onProgress(
                    MurottalDownloadProgress(
                        completedInBatch = batchDone,
                        totalInBatch = total,
                        overallDownloaded = overall,
                        currentGlobalAyah = globalAyah,
                        recitationId = recitationId
                    )
                )
            }
            MurottalOfflineStore.setInProgress(appContext, false)
            return Result.success(Unit)
        } catch (t: Throwable) {
            MurottalOfflineStore.setInProgress(appContext, false)
            MurottalOfflineStore.setLastError(appContext, t.message)
            return Result.failure(t)
        }
    }

    private fun globalAyahNumbersForChapters(chapterIds: List<Int>): List<Int> {
        val db = database.openReadable()
        val placeholders = chapterIds.joinToString(",") { "?" }
        val args = chapterIds.map { it.toString() }.toTypedArray()
        db.rawQuery(
            """
            SELECT CAST(s.start AS INTEGER) + a.aya AS global_ayah
            FROM ayas a
            JOIN suras s ON s."index" = a.sura
            WHERE a.sura IN ($placeholders)
            ORDER BY a."index"
            """.trimIndent(),
            args
        ).use { cursor ->
            return buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getInt(0))
                }
            }
        }
    }

    private fun downloadFile(url: String, target: java.io.File) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 20_000
        connection.readTimeout = 60_000
        connection.instanceFollowRedirects = true
        connection.connect()
        if (connection.responseCode !in 200..299) {
            connection.disconnect()
            throw IllegalStateException("HTTP ${connection.responseCode} for $url")
        }
        val tmp = File(target.parentFile, "${target.name}.part")
        connection.inputStream.use { input ->
            tmp.outputStream().use { output -> input.copyTo(output) }
        }
        connection.disconnect()
        if (!tmp.renameTo(target)) {
            tmp.copyTo(target, overwrite = true)
            tmp.delete()
        }
    }
}
