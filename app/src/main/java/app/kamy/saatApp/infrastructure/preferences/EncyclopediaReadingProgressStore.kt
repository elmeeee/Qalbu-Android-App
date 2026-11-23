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
}
