package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.domain.model.HifzEntry
import app.kamy.qalbuApp.domain.model.HifzStatus
import app.kamy.qalbuApp.domain.model.VerseBookmark
import app.kamy.qalbuApp.domain.model.VerseNote
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object QuranPersonalStore {
    private const val PREFS = "qalbu_quran_personal"
    private const val KEY_BOOKMARKS = "bookmarks_json"
    private const val KEY_NOTES = "notes_json"
    private const val KEY_HIFZ = "hifz_json"
    private const val KEY_HIFZ_MODE = "hifz_mode_enabled"

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
        val current = hifzEntries(context).toMutableList()
        val index = current.indexOfFirst { it.verseKey == verseKey }
        val previous = if (index >= 0) {
            runCatching { HifzStatus.valueOf(current[index].status) }.getOrDefault(HifzStatus.NONE)
        } else {
            HifzStatus.NONE
        }
        val next = previous.nextOnTap()
        if (index >= 0) current.removeAt(index)
        if (next != HifzStatus.NONE) {
            current.add(
                HifzEntry(
                    verseKey = verseKey,
                    chapterNumber = chapterNumber,
                    verseNumber = verseNumber,
                    status = next.name
                )
            )
        }
        saveList(context, KEY_HIFZ, HifzList(current))
        return next
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
