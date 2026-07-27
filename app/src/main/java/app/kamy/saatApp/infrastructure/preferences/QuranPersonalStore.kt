package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.model.HifzEntry
import app.kamy.saatApp.domain.model.HifzStatus
import app.kamy.saatApp.domain.model.VerseBookmark
import app.kamy.saatApp.domain.model.VerseNote
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object QuranPersonalStore {
    private const val PREFS = "saat_quran_personal"
    private const val KEY_BOOKMARKS = "bookmarks_json"
    private const val KEY_NOTES = "notes_json"
    private const val KEY_HIFZ = "hifz_json"
    private const val KEY_HIFZ_MODE = "hifz_mode_enabled"
    private const val KEY_KHATAM = "khatam_json"

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class BookmarkList(val items: List<VerseBookmark> = emptyList())

    @Serializable
    private data class NoteList(val items: List<VerseNote> = emptyList())

    @Serializable
    private data class HifzList(val items: List<HifzEntry> = emptyList())

    fun isHifzModeEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_HIFZ_MODE, false)

    fun setHifzModeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HIFZ_MODE, enabled)
            .apply()
    }

    fun bookmarks(context: Context): List<VerseBookmark> =
        loadList(context, KEY_BOOKMARKS, BookmarkList()).items.sortedByDescending { it.createdAtMillis }

    fun isBookmarked(context: Context, verseKey: String): Boolean =
        bookmarks(context).any { it.verseKey == verseKey }

    fun toggleBookmark(
        context: Context,
        verseKey: String,
        chapterNumber: Int,
        verseNumber: Int,
        surahLabel: String? = null
    ): Boolean {
        val current = bookmarks(context).toMutableList()
        val existing = current.indexOfFirst { it.verseKey == verseKey }
        return if (existing >= 0) {
            current.removeAt(existing)
            saveList(context, KEY_BOOKMARKS, BookmarkList(current))
            false
        } else {
            current.add(
                VerseBookmark(
                    verseKey = verseKey,
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    createdAtMillis = System.currentTimeMillis(),
                    surahLabel = surahLabel
                )
            )
            saveList(context, KEY_BOOKMARKS, BookmarkList(current))
            true
        }
    }

    fun removeBookmark(context: Context, verseKey: String) {
        val filtered = bookmarks(context).filterNot { it.verseKey == verseKey }
        saveList(context, KEY_BOOKMARKS, BookmarkList(filtered))
    }

    // ── Khatam tracker ────────────────────────────────────────────────

    @Serializable
    private data class KhatamList(
        val chapters: List<Int> = emptyList(),
        val juzs: List<Int> = emptyList(),
        val lastReadJuz: Int? = null,
        val lastReadVerseKey: String? = null
    )

    fun readChapters(context: Context): Set<Int> =
        loadList(context, KEY_KHATAM, KhatamList()).chapters.toSet()

    fun markChapterRead(context: Context, chapterNumber: Int) {
        val data = loadList(context, KEY_KHATAM, KhatamList())
        val current = data.chapters.toMutableSet()
        if (current.add(chapterNumber)) {
            saveList(context, KEY_KHATAM, data.copy(chapters = current.toList()))
        }
    }

    fun readJuzs(context: Context): Set<Int> =
        loadList(context, KEY_KHATAM, KhatamList()).juzs.toSet()

    fun markJuzRead(context: Context, juzNumber: Int) {
        val data = loadList(context, KEY_KHATAM, KhatamList())
        val current = data.juzs.toMutableSet()
        if (current.add(juzNumber)) {
            saveList(context, KEY_KHATAM, data.copy(juzs = current.toList()))
        }
    }

    fun lastReadJuz(context: Context): Int? =
        loadList(context, KEY_KHATAM, KhatamList()).lastReadJuz

    fun lastReadVerseKey(context: Context): String? =
        loadList(context, KEY_KHATAM, KhatamList()).lastReadVerseKey

    fun updateLastReadJuz(context: Context, juzNumber: Int, verseKey: String) {
        val data = loadList(context, KEY_KHATAM, KhatamList())
        if (data.lastReadJuz != juzNumber || data.lastReadVerseKey != verseKey) {
            saveList(context, KEY_KHATAM, data.copy(lastReadJuz = juzNumber, lastReadVerseKey = verseKey))
        }
    }

    fun noteFor(context: Context, verseKey: String): VerseNote? =
        notes(context).firstOrNull { it.verseKey == verseKey }

    fun notes(context: Context): List<VerseNote> =
        loadList(context, KEY_NOTES, NoteList()).items.sortedByDescending { it.updatedAtMillis }

    fun saveNote(
        context: Context,
        verseKey: String,
        chapterNumber: Int,
        verseNumber: Int,
        text: String
    ) {
        val trimmed = text.trim()
        val current = notes(context).toMutableList()
        current.removeAll { it.verseKey == verseKey }
        if (trimmed.isNotEmpty()) {
            current.add(
                VerseNote(
                    verseKey = verseKey,
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    text = trimmed,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
        saveList(context, KEY_NOTES, NoteList(current))
    }

    fun hifzStatus(context: Context, verseKey: String): HifzStatus {
        val raw = hifzEntries(context).firstOrNull { it.verseKey == verseKey }?.status ?: return HifzStatus.NONE
        return runCatching { HifzStatus.valueOf(raw) }.getOrDefault(HifzStatus.NONE)
    }

    fun hifzEntries(context: Context): List<HifzEntry> =
        loadList(context, KEY_HIFZ, HifzList()).items

    fun cycleHifzStatus(
        context: Context,
        verseKey: String,
        chapterNumber: Int,
        verseNumber: Int
    ): HifzStatus {
        val current = hifzStatus(context, verseKey)
        return setHifzStatus(context, verseKey, chapterNumber, verseNumber, current.nextOnTap())
    }

    fun setHifzStatus(
        context: Context,
        verseKey: String,
        chapterNumber: Int,
        verseNumber: Int,
        status: HifzStatus
    ): HifzStatus {
        val current = hifzEntries(context).toMutableList()
        current.removeAll { it.verseKey == verseKey }
        if (status != HifzStatus.NONE) {
            current.add(
                HifzEntry(
                    verseKey = verseKey,
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    status = status.name
                )
            )
        }
        saveList(context, KEY_HIFZ, HifzList(current))
        return status
    }

    fun deleteNote(context: Context, verseKey: String) {
        val filtered = notes(context).filterNot { it.verseKey == verseKey }
        saveList(context, KEY_NOTES, NoteList(filtered))
    }

    data class HifzSummary(
        val learning: Int,
        val memorized: Int,
        val needsReview: Int
    ) {
        val total: Int get() = learning + memorized + needsReview
    }

    fun hifzSummary(context: Context): HifzSummary {
        var learning = 0
        var memorized = 0
        var review = 0
        hifzEntries(context).forEach { entry ->
            when (runCatching { HifzStatus.valueOf(entry.status) }.getOrDefault(HifzStatus.NONE)) {
                HifzStatus.LEARNING -> learning++
                HifzStatus.MEMORIZED -> memorized++
                HifzStatus.NEEDS_REVIEW -> review++
                HifzStatus.NONE -> Unit
            }
        }
        return HifzSummary(learning, memorized, review)
    }

    private inline fun <reified T> loadList(context: Context, key: String, fallback: T): T {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null)
            ?: return fallback
        return runCatching { json.decodeFromString<T>(raw) }.getOrDefault(fallback)
    }

    private inline fun <reified T> saveList(context: Context, key: String, value: T) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(key, json.encodeToString(value))
            .apply()
    }
}
