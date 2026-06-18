package app.kamy.qalbuApp.infrastructure.offline

import android.content.Context
import app.kamy.qalbuApp.core.config.LocalQuranConfig
import java.io.File

object MurottalOfflineStore {
    private const val PREFS = "qalbu_murottal_offline"
    private const val KEY_IN_PROGRESS = "in_progress"
    private const val KEY_LAST_ERROR = "last_error"
    private const val KEY_ACTIVE_RECITER = "active_reciter"
    private const val KEY_DOWNLOADED_COUNT = "downloaded_count"
    private const val KEY_TOTAL_COUNT = "total_count"
    private const val KEY_LAST_PROGRESS_AT = "last_progress_at"
    private const val STALE_PROGRESS_MS = 3 * 60 * 1000L

    fun rootDir(context: Context): File =
        File(context.filesDir, "murottal").apply { mkdirs() }

    fun reciterDir(context: Context, recitationId: Int): File {
        val slug = LocalQuranConfig.reciterSlug(recitationId)
        return File(rootDir(context), slug).apply { mkdirs() }
    }

    fun ayahFile(context: Context, recitationId: Int, globalAyahNumber: Int): File =
        File(reciterDir(context, recitationId), "$globalAyahNumber.mp3")

    fun isAyahDownloaded(context: Context, recitationId: Int, globalAyahNumber: Int): Boolean {
        val file = ayahFile(context, recitationId, globalAyahNumber)
        return file.exists() && file.length() > 1024L
    }

    fun downloadedCount(context: Context, recitationId: Int): Int {
        val dir = reciterDir(context, recitationId)
        return dir.listFiles()?.count { it.isFile && it.length() > 1024L } ?: 0
    }

    fun localUrlIfDownloaded(context: Context, recitationId: Int, globalAyahNumber: Int): String? {
        val file = ayahFile(context, recitationId, globalAyahNumber)
        return if (file.exists() && file.length() > 1024L) file.toURI().toString() else null
    }

    fun setInProgress(context: Context, inProgress: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_IN_PROGRESS, inProgress)
            .apply()
        if (inProgress) touchProgress(context)
    }

    fun isInProgress(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IN_PROGRESS, false)

    fun touchProgress(context: Context) {
        prefs(context).edit()
            .putLong(KEY_LAST_PROGRESS_AT, System.currentTimeMillis())
            .apply()
    }

    fun recoverStaleProgress(context: Context) {
        if (!isInProgress(context)) return
        val last = prefs(context).getLong(KEY_LAST_PROGRESS_AT, 0L)
        if (last == 0L || System.currentTimeMillis() - last > STALE_PROGRESS_MS) {
            setInProgress(context, false)
        }
    }

    fun setLastError(context: Context, message: String?) {
        prefs(context).edit().putString(KEY_LAST_ERROR, message).apply()
    }

    fun lastError(context: Context): String? =
        prefs(context).getString(KEY_LAST_ERROR, null)

    fun setActiveReciter(context: Context, recitationId: Int) {
        prefs(context).edit().putInt(KEY_ACTIVE_RECITER, recitationId).apply()
    }

    fun activeReciter(context: Context): Int =
        prefs(context).getInt(KEY_ACTIVE_RECITER, LocalQuranConfig.DEFAULT_RECITATION_ID)

    fun updateProgress(context: Context, downloaded: Int, total: Int) {
        prefs(context).edit()
            .putInt(KEY_DOWNLOADED_COUNT, downloaded)
            .putInt(KEY_TOTAL_COUNT, total)
            .apply()
        touchProgress(context)
    }

    fun progressCounts(context: Context): Pair<Int, Int> {
        val p = prefs(context)
        return p.getInt(KEY_DOWNLOADED_COUNT, 0) to p.getInt(KEY_TOTAL_COUNT, 6236)
    }

    fun clearReciter(context: Context, recitationId: Int) {
        reciterDir(context, recitationId).deleteRecursively()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
