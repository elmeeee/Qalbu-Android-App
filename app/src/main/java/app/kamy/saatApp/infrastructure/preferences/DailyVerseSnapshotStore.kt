package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.infrastructure.widget.WidgetCoordinator
import app.kamy.saatApp.ui.common.toVerseTranslationPlainText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyVerseSnapshot(
    val dayKey: String,
    val chapterNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val arabic: String,
    val translation: String,
    val transliterationId: String? = null,
    val transliterationEn: String? = null,
    val verseKey: String?,
    val occasionKey: String? = null
) {
    fun notificationBody(): String {
        val reference = buildString {
            append(surahName)
            append(' ')
            append(ayahNumber)
        }
        val excerpt = translation.trim()
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
    private const val KEY_ARABIC = "arabic"
    private const val KEY_TRANSLITERATION_ID = "transliteration_id"
    private const val KEY_TRANSLITERATION_EN = "transliteration_en"
    private const val KEY_TRANSLATION = "translation"
    private const val KEY_VERSE_KEY = "verse_key"
    private const val KEY_OCCASION = "occasion_key"

    fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun save(
        context: Context,
        verse: RandomAyahPayload,
        surahName: String?,
        occasionKey: String? = null
    ) {
        val chapter = verse.chapterNumber ?: return
        val ayah = verse.resolvedVerseNumber ?: return
        val name = surahName?.trim().orEmpty().ifBlank { "Surah $chapter" }
        val translation = verse.translations?.firstOrNull()?.text?.toVerseTranslationPlainText().orEmpty()
        val arabic = verse.textUthmani ?: verse.textUthmaniSimple ?: verse.textImlaei.orEmpty()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DAY, todayKey())
            .putInt(KEY_CHAPTER, chapter)
            .putInt(KEY_AYAH, ayah)
            .putString(KEY_SURAH, name)
            .putString(KEY_ARABIC, arabic)
            .putString(KEY_TRANSLITERATION_ID, verse.transliterationId)
            .putString(KEY_TRANSLITERATION_EN, verse.transliterationEn)
            .putString(KEY_TRANSLATION, translation)
            .putString(KEY_VERSE_KEY, verse.verseKey)
            .putString(KEY_OCCASION, occasionKey)
            .apply()
        runCatching {
            WidgetCoordinator.refreshAll(context)
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
            arabic = prefs.getString(KEY_ARABIC, "").orEmpty(),
            translation = prefs.getString(KEY_TRANSLATION, "").orEmpty(),
            transliterationId = prefs.getString(KEY_TRANSLITERATION_ID, null),
            transliterationEn = prefs.getString(KEY_TRANSLITERATION_EN, null),
            verseKey = prefs.getString(KEY_VERSE_KEY, null),
            occasionKey = prefs.getString(KEY_OCCASION, null)
        )
    }

    fun loadForToday(context: Context): DailyVerseSnapshot? {
        val snapshot = load(context) ?: return null
        return snapshot.takeIf { it.dayKey == todayKey() }
    }
}
