package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.model.LocalReadingProgress

object LocalReadingProgressStore {
    private const val PREFS = "saat_local_reading"
    private const val KEY_CHAPTER = "chapter"
    private const val KEY_VERSE = "verse"
    private const val KEY_UPDATED = "updated_at"

    fun save(context: Context, chapterNumber: Int, verseNumber: Int) {
        if (chapterNumber <= 0 || verseNumber <= 0) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CHAPTER, chapterNumber)
            .putInt(KEY_VERSE, verseNumber)
            .putLong(KEY_UPDATED, System.currentTimeMillis())
            .apply()
    }

    fun load(context: Context): LocalReadingProgress? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val chapter = prefs.getInt(KEY_CHAPTER, -1)
        val verse = prefs.getInt(KEY_VERSE, -1)
        if (chapter <= 0 || verse <= 0) return null
        return LocalReadingProgress(
            chapterNumber = chapter,
            verseNumber = verse,
            updatedAtMillis = prefs.getLong(KEY_UPDATED, 0L)
        )
    }
}
