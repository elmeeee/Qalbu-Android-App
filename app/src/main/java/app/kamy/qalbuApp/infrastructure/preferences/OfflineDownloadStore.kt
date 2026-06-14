package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context

object OfflineDownloadStore {
    private const val PREFS = "qalbu_offline_quran"
    private const val KEY_DOWNLOADED_CHAPTERS = "downloaded_chapters"
    private const val KEY_IN_PROGRESS = "in_progress"
    private const val KEY_LAST_ERROR = "last_error"
    private const val KEY_COMPLETED_AT = "completed_at"
    private const val KEY_LAST_PROGRESS_AT = "last_progress_at"
    private const val STALE_PROGRESS_MS = 3 * 60 * 1000L

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun downloadedChapterCount(context: Context): Int = downloadedChapterIds(context).size

    fun downloadedChapterIds(context: Context): Set<Int> =
        prefs(context)
            .getStringSet(KEY_DOWNLOADED_CHAPTERS, emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()

    fun isChapterDownloaded(context: Context, chapter: Int): Boolean =
        chapter.toString() in prefs(context).getStringSet(KEY_DOWNLOADED_CHAPTERS, emptySet()).orEmpty()

    fun markChapterDownloaded(context: Context, chapter: Int) {
        val current = prefs(context).getStringSet(KEY_DOWNLOADED_CHAPTERS, emptySet()).orEmpty().toMutableSet()
        current.add(chapter.toString())
        prefs(context).edit()
            .putStringSet(KEY_DOWNLOADED_CHAPTERS, current)
            .apply()
    }

    fun unmarkChapterDownloaded(context: Context, chapter: Int) {
        val current = prefs(context).getStringSet(KEY_DOWNLOADED_CHAPTERS, emptySet()).orEmpty().toMutableSet()
        current.remove(chapter.toString())
        prefs(context).edit()
            .putStringSet(KEY_DOWNLOADED_CHAPTERS, current)
            .apply()
    }

    fun isFullyDownloaded(context: Context, totalChapters: Int = 114): Boolean =
        downloadedChapterCount(context) >= totalChapters

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

    /** Clears stuck downloads after app kill or network hang. */
    fun recoverStaleProgress(context: Context) {
        if (!isInProgress(context)) return
        val last = prefs(context).getLong(KEY_LAST_PROGRESS_AT, 0L)
        if (last == 0L || System.currentTimeMillis() - last > STALE_PROGRESS_MS) {
            setInProgress(context, false)
        }
    }

    fun setLastError(context: Context, message: String?) {
        prefs(context).edit()
            .putString(KEY_LAST_ERROR, message)
            .apply()
    }

    fun lastError(context: Context): String? =
        prefs(context).getString(KEY_LAST_ERROR, null)

    fun markCompleted(context: Context) {
        prefs(context).edit()
            .putLong(KEY_COMPLETED_AT, System.currentTimeMillis())
            .putBoolean(KEY_IN_PROGRESS, false)
            .putString(KEY_LAST_ERROR, null)
            .apply()
    }

    fun completedAt(context: Context): Long =
        prefs(context).getLong(KEY_COMPLETED_AT, 0L)

    fun reset(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
