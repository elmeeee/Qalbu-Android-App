package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context

import app.kamy.qalbuApp.core.config.MushafConfig

object MushafReadingStore {
    private const val PREFS = "qalbu_mushaf_reading"
    private const val KEY_LAST_PAGE = "last_page"
    private const val KEY_PAGES_READ = "pages_read"
    private const val KEY_SWIPE_HINT_SEEN = "swipe_hint_seen"
    private val totalMushafPages = MushafConfig.TOTAL_PAGES

    fun lastPage(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_LAST_PAGE, 1)
            .coerceIn(1, totalMushafPages)

    fun saveLastPage(context: Context, page: Int, markRead: Boolean = true) {
        val safe = page.coerceIn(1, totalMushafPages)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LAST_PAGE, safe)
            .apply()
        if (markRead) markPageRead(context, safe)
    }

    fun markPageRead(context: Context, page: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_PAGES_READ, emptySet()).orEmpty()
        prefs.edit().putStringSet(KEY_PAGES_READ, current + page.toString()).apply()
    }

    fun pagesReadCount(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_PAGES_READ, emptySet())
            ?.size ?: 0

    fun progressFraction(context: Context): Float =
        pagesReadCount(context).toFloat() / totalMushafPages.toFloat()

    fun hasSeenSwipeHint(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SWIPE_HINT_SEEN, false)

    fun markSwipeHintSeen(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SWIPE_HINT_SEEN, true)
            .apply()
    }

    const val totalPages: Int = MushafConfig.TOTAL_PAGES
}
