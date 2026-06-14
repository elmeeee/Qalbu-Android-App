package app.kamy.qalbuApp.infrastructure.widget

import app.kamy.qalbuApp.infrastructure.preferences.DailyVerseSnapshot

data class DailyVerseWidgetSnapshot(
    val label: String,
    val reference: String,
    val excerpt: String,
    val chapterNumber: Int,
    val ayahNumber: Int
)

object DailyVerseWidgetRenderer {
    fun snapshot(context: android.content.Context): DailyVerseWidgetSnapshot? {
        val verse = app.kamy.qalbuApp.infrastructure.preferences.DailyVerseSnapshotStore
            .loadForToday(context) ?: return null
        return verse.toWidgetSnapshot(context)
    }

    private fun DailyVerseSnapshot.toWidgetSnapshot(context: android.content.Context): DailyVerseWidgetSnapshot {
        return DailyVerseWidgetSnapshot(
            label = context.getString(app.kamy.qalbuApp.R.string.verse_of_day),
            reference = "$surahName · $ayahNumber",
            excerpt = translation.take(160).trim(),
            chapterNumber = chapterNumber,
            ayahNumber = ayahNumber
        )
    }
}
