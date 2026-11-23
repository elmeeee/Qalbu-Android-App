package app.kamy.saatApp.infrastructure.preferences

import android.content.Context

object EncyclopediaReadingProgressStore {
    private const val PREFERENCES_NAME = "saat_encyclopedia_reading"
    private const val PAGE_KEY_PREFIX = "page_"

    fun loadPage(context: Context, topicId: String): Int? {
        if (topicId.isBlank()) return null
        val page = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getInt("$PAGE_KEY_PREFIX$topicId", -1)
        return page.takeIf { it >= 0 }
    }

    fun savePage(context: Context, topicId: String, page: Int) {
        require(topicId.isNotBlank()) { "Topic ID must not be blank" }
        require(page >= 0) { "Page index must not be negative" }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt("$PAGE_KEY_PREFIX$topicId", page)
            .apply()
    }

    fun allPages(context: Context): Map<String, Int> {
        val all = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).all
        val result = mutableMapOf<String, Int>()
        for ((key, value) in all) {
            if (key.startsWith(PAGE_KEY_PREFIX) && value is Number) {
                val topicId = key.removePrefix(PAGE_KEY_PREFIX)
                val pageIndex = value.toInt()
                if (topicId.isNotBlank() && pageIndex >= 0) {
                    result[topicId] = pageIndex
                }
            }
        }
        return result
    }

    fun restorePages(context: Context, pages: Map<String, Int>) {
        if (pages.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for ((topicId, page) in pages) {
            if (topicId.isNotBlank() && page >= 0) {
                val current = prefs.getInt("$PAGE_KEY_PREFIX$topicId", -1)
                // Monotonically advance or adopt cloud progress if local not set
                if (current < 0 || page > current) {
                    editor.putInt("$PAGE_KEY_PREFIX$topicId", page)
                }
            }
        }
        editor.apply()
    }

    fun clearAll(context: Context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
