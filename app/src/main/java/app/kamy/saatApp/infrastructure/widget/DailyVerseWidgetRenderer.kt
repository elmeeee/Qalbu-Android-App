package app.kamy.saatApp.infrastructure.widget

data class DailyVerseWidgetSnapshot(
    val label: String,
    val reference: String,
    val arabic: String,
    val excerpt: String,
    val chapterNumber: Int,
    val ayahNumber: Int
)

object DailyVerseWidgetRenderer {
    fun snapshot(context: android.content.Context): DailyVerseWidgetSnapshot {
        val verse = app.kamy.saatApp.infrastructure.preferences.DailyVerseSnapshotStore
            .loadForToday(context)
            ?: app.kamy.saatApp.infrastructure.preferences.DailyVerseSnapshotStore
                .load(context)

        return verse?.toWidgetSnapshot(context) ?: fallbackSnapshot(context)
    }

    private fun fallbackSnapshot(context: android.content.Context): DailyVerseWidgetSnapshot {
        return DailyVerseWidgetSnapshot(
            label = context.getString(app.kamy.saatApp.R.string.verse_of_day),
            reference = "Al-Fatihah · 1",
            arabic = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            excerpt = "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang.",
            chapterNumber = 1,
            ayahNumber = 1
        )
    }

    private fun app.kamy.saatApp.infrastructure.preferences.DailyVerseSnapshot.toWidgetSnapshot(
        context: android.content.Context
    ): DailyVerseWidgetSnapshot {
        return DailyVerseWidgetSnapshot(
            label = context.getString(app.kamy.saatApp.R.string.verse_of_day),
            reference = "$surahName · $ayahNumber",
            arabic = arabic.trim(),
            excerpt = translation.trim(),
            chapterNumber = chapterNumber,
            ayahNumber = ayahNumber
        )
    }
}
