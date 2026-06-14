package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.ui.common.toVerseTranslationPlainText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyVerseSnapshot(
    val dayKey: String,
    val chapterNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val translation: String,
    val verseKey: String?
) {
    fun notificationBody(): String {
        val reference = buildString {
            append("Today — ")
            append(surahName)
            append(' ')
            append(ayahNumber)
        }
        val excerpt = translation.take(180).trim()
        return if (excerpt.isEmpty()) {
            reference
        } else {
            "$reference — $excerpt"
        }
    }
}

object DailyVerseSnapshotStore {

    private const val PREFS = "daily_verse_snapshot"
    private const val KEY_DAY = "day_key"
    private const val KEY_CHAPTER = "chapter"
    private const val KEY_AYAH = "ayah"
    private const val KEY_SURAH = "surah_name"
    private const val KEY_TRANSLATION = "translation"
    private const val KEY_VERSE_KEY = "verse_key"

    fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun save(context: Context, verse: RandomAyahPayload, surahName: String?) {
        val chapter = verse.chapterNumber ?: return
        val ayah = verse.resolvedVerseNumber ?: return
        val name = surahName?.trim().orEmpty().ifBlank { "Surah $chapter" }
        val translation = verse.translations?.firstOrNull()?.text?.toVerseTranslationPlainText().orEmpty()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DAY, todayKey())
            .putInt(KEY_CHAPTER, chapter)
            .putInt(KEY_AYAH, ayah)
            .putString(KEY_SURAH, name)
            .putString(KEY_TRANSLATION, translation)
            .putString(KEY_VERSE_KEY, verse.verseKey)
            .apply()
        runCatching {
            app.kamy.qalbuApp.infrastructure.widget.DailyVerseWidgetUpdater.updateAll(context)
        }
    }

    fun load(context: Context): DailyVerseSnapshot? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val dayKey = prefs.getString(KEY_DAY, null) ?: return null
        val chapter = prefs.getInt(KEY_CHAPTER, -1)
        val ayah = prefs.getInt(KEY_AYAH, -1)
        val surah = prefs.getString(KEY_SURAH, null)?.trim().orEmpty()
        if (chapter <= 0 || ayah <= 0 || surah.isEmpty()) return null
        return DailyVerseSnapshot(
            dayKey = dayKey,
            chapterNumber = chapter,
            ayahNumber = ayah,
            surahName = surah,
            translation = prefs.getString(KEY_TRANSLATION, "").orEmpty(),
            verseKey = prefs.getString(KEY_VERSE_KEY, null)
        )
    }

    fun loadForToday(context: Context): DailyVerseSnapshot? {
        val snapshot = load(context) ?: return null
        return snapshot.takeIf { it.dayKey == todayKey() }
    }
}
