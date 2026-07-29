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
        val lastReadVerseKey: String? = null,
        val lastReadUpdatedAtMillis: Long? = null
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

    fun lastReadUpdatedAtMillis(context: Context): Long? =
        loadList(context, KEY_KHATAM, KhatamList()).lastReadUpdatedAtMillis

    fun lastReadProgress(context: Context): app.kamy.saatApp.domain.model.LocalReadingProgress? {
        val data = loadList(context, KEY_KHATAM, KhatamList())
        val verseKey = data.lastReadVerseKey ?: return null
        val parts = verseKey.split(':', limit = 2)
        val chapterNumber = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val verseNumber = parts.getOrNull(1)?.toIntOrNull() ?: return null
        if (chapterNumber <= 0 || verseNumber <= 0) return null
        return app.kamy.saatApp.domain.model.LocalReadingProgress(
            chapterNumber = chapterNumber,
            verseNumber = verseNumber,
            updatedAtMillis = data.lastReadUpdatedAtMillis ?: 0L
        )
    }

    fun updateLastReadJuz(
        context: Context,
        juzNumber: Int,
        verseKey: String,
        updatedAtMillis: Long = System.currentTimeMillis()
    ) {
        val data = loadList(context, KEY_KHATAM, KhatamList())
        if (
            data.lastReadJuz != juzNumber ||
            data.lastReadVerseKey != verseKey ||
            data.lastReadUpdatedAtMillis != updatedAtMillis
        ) {
            saveList(
                context,
                KEY_KHATAM,
                data.copy(
                    lastReadJuz = juzNumber,
                    lastReadVerseKey = verseKey,
                    lastReadUpdatedAtMillis = updatedAtMillis
                )
            )
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

    @Serializable
    private data class LocalDevicePersonalBackup(
        val version: Int = 1,
        val bookmarks: List<VerseBookmark> = emptyList(),
        val notes: List<VerseNote> = emptyList(),
        val lastReadJuz: Int? = null,
        val lastReadVerseKey: String? = null,
        val lastReadUpdatedAtMillis: Long? = null
    )

    private fun getBackupFile(context: Context): java.io.File? {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        return java.io.File(dir, "qalbu_personal_backup.json")
    }

    private fun syncBackupToDevice(context: Context) {
        runCatching {
            val file = getBackupFile(context) ?: return
            val khatamData = loadListRaw<KhatamList>(context, KEY_KHATAM, KhatamList())
            val backup = LocalDevicePersonalBackup(
                bookmarks = loadListRaw<BookmarkList>(context, KEY_BOOKMARKS, BookmarkList()).items,
                notes = loadListRaw<NoteList>(context, KEY_NOTES, NoteList()).items,
                lastReadJuz = khatamData.lastReadJuz,
                lastReadVerseKey = khatamData.lastReadVerseKey,
                lastReadUpdatedAtMillis = khatamData.lastReadUpdatedAtMillis
            )
            file.writeText(json.encodeToString(backup))
        }
    }

    private fun checkRestoreFromBackup(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_BOOKMARKS) && !prefs.contains(KEY_NOTES) && !prefs.contains(KEY_KHATAM)) {
            runCatching {
                val file = getBackupFile(context) ?: return
                if (file.exists()) {
                    val content = file.readText()
                    val backup = json.decodeFromString<LocalDevicePersonalBackup>(content)
                    if (backup.bookmarks.isNotEmpty()) {
                        saveListRaw(context, KEY_BOOKMARKS, BookmarkList(backup.bookmarks))
                    }
                    if (backup.notes.isNotEmpty()) {
                        saveListRaw(context, KEY_NOTES, NoteList(backup.notes))
                    }
                    if (backup.lastReadVerseKey != null) {
                        saveListRaw(
                            context,
                            KEY_KHATAM,
                            KhatamList(
                                lastReadJuz = backup.lastReadJuz,
                                lastReadVerseKey = backup.lastReadVerseKey,
                                lastReadUpdatedAtMillis = backup.lastReadUpdatedAtMillis
                            )
                        )
                    }
                }
            }
        }
    }

    private inline fun <reified T> loadListRaw(context: Context, key: String, fallback: T): T {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null)
            ?: return fallback
        return runCatching { json.decodeFromString<T>(raw) }.getOrDefault(fallback)
    }

    private inline fun <reified T> saveListRaw(context: Context, key: String, value: T) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(key, json.encodeToString(value))
            .apply()
    }

    private inline fun <reified T> loadList(context: Context, key: String, fallback: T): T {
        checkRestoreFromBackup(context)
        return loadListRaw(context, key, fallback)
    }

    private inline fun <reified T> saveList(context: Context, key: String, value: T) {
        saveListRaw(context, key, value)
        syncBackupToDevice(context)
    }
}
