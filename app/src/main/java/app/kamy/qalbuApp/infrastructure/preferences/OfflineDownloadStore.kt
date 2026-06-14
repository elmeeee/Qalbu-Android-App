package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context

object OfflineDownloadStore {
    private const val PREFS = "qalbu_offline_quran"
    private const val KEY_DOWNLOADED_CHAPTERS = "downloaded_chapters"
    private const val KEY_IN_PROGRESS = "in_progress"
    private const val KEY_LAST_ERROR = "last_error"
    private const val KEY_COMPLETED_AT = "completed_at"

    fun downloadedChapterCount(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_DOWNLOADED_CHAPTERS, emptySet())
            ?.size ?: 0

    fun isChapterDownloaded(context: Context, chapter: Int): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_DOWNLOADED_CHAPTERS, emptySet())
            ?.contains(chapter.toString()) == true

    fun markChapterDownloaded(context: Context, chapter: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_DOWNLOADED_CHAPTERS, emptySet()).orEmpty().toMutableSet()
        current.add(chapter.toString())
        prefs.edit()
            .putStringSet(KEY_DOWNLOADED_CHAPTERS, current)
            .apply()
    }

    fun isFullyDownloaded(context: Context, totalChapters: Int = 114): Boolean =
        downloadedChapterCount(context) >= totalChapters

    fun setInProgress(context: Context, inProgress: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IN_PROGRESS, inProgress)
            .apply()
    }

    fun isInProgress(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_IN_PROGRESS, false)

    fun setLastError(context: Context, message: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_ERROR, message)
            .apply()
    }

    fun lastError(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_ERROR, null)

    fun markCompleted(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_COMPLETED_AT, System.currentTimeMillis())
            .putBoolean(KEY_IN_PROGRESS, false)
            .putString(KEY_LAST_ERROR, null)
            .apply()
    }

    fun completedAt(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_COMPLETED_AT, 0L)

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
