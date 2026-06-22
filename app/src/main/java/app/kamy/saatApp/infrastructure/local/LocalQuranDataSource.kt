package app.kamy.saatApp.infrastructure.local

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.core.config.MushafConfig
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.AudioPayload
import app.kamy.saatApp.domain.model.ContentPagination
import app.kamy.saatApp.domain.model.InlineTranslation
import app.kamy.saatApp.domain.model.LookupRange
import app.kamy.saatApp.domain.model.PageInfo
import app.kamy.saatApp.domain.model.PagesLookupResponse
import app.kamy.saatApp.domain.model.QuranChapter
import app.kamy.saatApp.domain.model.QuranJuz
import app.kamy.saatApp.domain.model.QuranWord
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.TafsirPayload
import app.kamy.saatApp.domain.model.VersesByChapterResponse
import app.kamy.saatApp.domain.model.ChapterTranslatedName
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import app.kamy.saatApp.infrastructure.offline.MurottalOfflineStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class LocalQuranDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: LocalQuranDatabase
) {
    private companion object {
        const val AYAH_SELECT = """
            a.sura, a.aya, a.text, a.indonesian, a.malay, a.translation_en, a.kemenag,
            a.jalalayn, a.transliteration_id, a.transliteration_en,
            a.page, a.juz, a."index", CAST(s.start AS INTEGER) + a.aya AS global_ayah
        """
    }

    @Volatile
    private var tafsirByIndex: List<String>? = null

    suspend fun getChapters(language: AppLanguage = AppLanguage.INDONESIAN): List<QuranChapter> =
        withContext(Dispatchers.IO) {
        val db = database.openReadable()
        db.rawQuery(
            """
            SELECT "index", tname, ename, ename_english, ayas, type, first_page, last_page, name
            FROM suras
            ORDER BY "index"
            """.trimIndent(),
            null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toChapter(language))
                }
            }
        }
    }

    suspend fun getJuzs(): List<QuranJuz> = withContext(Dispatchers.IO) {
        (1..30).mapNotNull { juzNumber -> loadJuz(db = database.openReadable(), juzNumber) }
    }

    suspend fun getJuz(juzNumber: Int): QuranJuz? = withContext(Dispatchers.IO) {
        if (juzNumber !in 1..30) return@withContext null
        loadJuz(database.openReadable(), juzNumber)
    }

    suspend fun getVersesByChapter(
        chapterNumber: Int,
        page: Int,
        perPage: Int,
        translationId: Int,
        recitationId: Int
    ): VersesByChapterResponse = withContext(Dispatchers.IO) {
        val db = database.openReadable()
        val total = db.intQuery(
            "SELECT COUNT(*) FROM ayas WHERE sura = ?",
            arrayOf(chapterNumber.toString())
        )
        val offset = (page - 1).coerceAtLeast(0) * perPage
        val verses = db.rawQuery(
            """
            SELECT $AYAH_SELECT
            FROM ayas a
            JOIN suras s ON s."index" = a.sura
            WHERE a.sura = ?
            ORDER BY a.aya
            LIMIT ? OFFSET ?
            """.trimIndent(),
            arrayOf(chapterNumber.toString(), perPage.toString(), offset.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toVersePayload(translationId, recitationId, loadWords = false))
                }
            }
        }
        paginatedResponse(verses, page, perPage, total)
    }

    suspend fun getVersesByJuz(
        juzNumber: Int,
        page: Int,
        perPage: Int,
        translationId: Int,
        recitationId: Int
    ): VersesByChapterResponse = withContext(Dispatchers.IO) {
        val db = database.openReadable()
        val total = db.intQuery(
            "SELECT COUNT(*) FROM ayas WHERE juz = ?",
            arrayOf(juzNumber.toString())
        )
        val offset = (page - 1).coerceAtLeast(0) * perPage
        val verses = db.rawQuery(
            """
            SELECT $AYAH_SELECT
            FROM ayas a
            JOIN suras s ON s."index" = a.sura
            WHERE a.juz = ?
            ORDER BY a."index"
            LIMIT ? OFFSET ?
            """.trimIndent(),
            arrayOf(juzNumber.toString(), perPage.toString(), offset.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toVersePayload(translationId, recitationId, loadWords = false))
                }
            }
        }
        paginatedResponse(verses, page, perPage, total)
    }

    suspend fun getVersesByMushafPage(
        mushafPage: Int,
        translationId: Int,
        recitationId: Int
    ): VersesByChapterResponse = withContext(Dispatchers.IO) {
        val db = database.openReadable()
        val verses = db.rawQuery(
            """
            SELECT DISTINCT $AYAH_SELECT
            FROM ayas a
            JOIN suras s ON s."index" = a.sura
            WHERE a.page = ?
            ORDER BY a."index"
            """.trimIndent(),
            arrayOf(mushafPage.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val sura = cursor.getInt(cursor.getColumnIndexOrThrow("sura"))
                    val aya = cursor.getInt(cursor.getColumnIndexOrThrow("aya"))
                    val verse = cursor.toVersePayload(translationId, recitationId, loadWords = false)
                    val words = loadWordsForVerse(db, sura, aya, mushafPage)
                    add(verse.copy(words = words))
                }
            }
        }
        VersesByChapterResponse(verses = verses)
    }

    suspend fun getRandomAyah(
        translationId: Int,
        recitationId: Int
    ): RandomAyahPayload? = withContext(Dispatchers.IO) {
        loadAyahAtOffset(randomAyahOffset(), translationId, recitationId)
            ?: loadAyahAtOffset(dailyAyahOffset(), translationId, recitationId)
    }

    suspend fun getDailyAyah(
        translationId: Int,
        recitationId: Int
    ): RandomAyahPayload? = withContext(Dispatchers.IO) {
        loadAyahAtOffset(dailyAyahOffset(), translationId, recitationId)
    }

    private fun randomAyahOffset(): Int {
        val db = database.openReadable()
        val total = db.intQuery("SELECT COUNT(*) FROM ayas", null)
        if (total <= 0) return 0
        return Random.nextInt(total)
    }

    private fun dailyAyahOffset(): Int {
        val db = database.openReadable()
        val total = db.intQuery("SELECT COUNT(*) FROM ayas", null)
        if (total <= 0) return 0
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return (dayOfYear - 1) % total
    }

    private fun loadAyahAtOffset(
        offset: Int,
        translationId: Int,
        recitationId: Int
    ): RandomAyahPayload? {
        val db = database.openReadable()
        db.rawQuery(
            """
            SELECT $AYAH_SELECT
            FROM ayas a
            JOIN suras s ON s."index" = a.sura
            ORDER BY a."index"
            LIMIT 1 OFFSET ?
            """.trimIndent(),
            arrayOf(offset.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return cursor.toVersePayload(translationId, recitationId, loadWords = false)
        }
    }

    suspend fun getVersesByRange(
        chapterNumber: Int,
        startAyah: Int,
        endAyah: Int,
        translationId: Int,
        recitationId: Int
    ): List<RandomAyahPayload> = withContext(Dispatchers.IO) {
        if (chapterNumber < 1 || startAyah < 1 || endAyah < startAyah) return@withContext emptyList()
        val db = database.openReadable()
        db.rawQuery(
            """
            SELECT $AYAH_SELECT
            FROM ayas a
            JOIN suras s ON s."index" = a.sura
            WHERE a.sura = ? AND a.aya BETWEEN ? AND ?
            ORDER BY a.aya
            """.trimIndent(),
            arrayOf(chapterNumber.toString(), startAyah.toString(), endAyah.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toVersePayload(translationId, recitationId, loadWords = false))
                }
            }
        }
    }

    suspend fun getVerseByKey(
        verseKey: String,
        translationId: Int,
        recitationId: Int
    ): RandomAyahPayload? = withContext(Dispatchers.IO) {
        val parts = verseKey.split(":")
        if (parts.size != 2) return@withContext null
        val sura = parts[0].toIntOrNull() ?: return@withContext null
        val aya = parts[1].toIntOrNull() ?: return@withContext null
        val db = database.openReadable()
        db.rawQuery(
            """
            SELECT $AYAH_SELECT
            FROM ayas a
            JOIN suras s ON s."index" = a.sura
            WHERE a.sura = ? AND a.aya = ?
            """.trimIndent(),
            arrayOf(sura.toString(), aya.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@withContext null
            cursor.toVersePayload(translationId, recitationId, loadWords = false)
        }
    }

    suspend fun mushafPageForVerse(chapterNumber: Int, verseNumber: Int): Int? =
        withContext(Dispatchers.IO) {
            database.openReadable().intQueryOrNull(
                "SELECT page FROM ayas WHERE sura = ? AND aya = ?",
                arrayOf(chapterNumber.toString(), verseNumber.toString())
            )
        }

    suspend fun firstMushafPageForChapter(chapterNumber: Int): Int? =
        withContext(Dispatchers.IO) {
            database.openReadable().intQueryOrNull(
                "SELECT MIN(page) FROM ayas WHERE sura = ?",
                arrayOf(chapterNumber.toString())
            )
        }

    suspend fun firstMushafPageForJuz(juzNumber: Int): Int? = withContext(Dispatchers.IO) {
        val db = database.openReadable()
        db.rawQuery(
            "SELECT sura, aya FROM juzs WHERE \"index\" = ?",
            arrayOf(juzNumber.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@withContext null
            val sura = cursor.getInt(0)
            val aya = cursor.getInt(1)
            db.intQueryOrNull(
                "SELECT page FROM ayas WHERE sura = ? AND aya = ?",
                arrayOf(sura.toString(), aya.toString())
            )
        }
    }

    suspend fun getPagesLookup(
        chapterNumber: Int? = null,
        juzNumber: Int? = null,
        pageNumber: Int? = null,
        fromVerse: String? = null,
        toVerse: String? = null
    ): PagesLookupResponse = withContext(Dispatchers.IO) {
        val page = when {
            pageNumber != null -> pageNumber
            fromVerse != null -> mushafPageForVerseKey(fromVerse)
            chapterNumber != null -> firstMushafPageForChapter(chapterNumber)
            juzNumber != null -> firstMushafPageForJuz(juzNumber)
            else -> null
        } ?: return@withContext PagesLookupResponse()
        val db = database.openReadable()
        val first = db.stringQuery(
            """
            SELECT sura || ':' || aya FROM ayas
            WHERE page = ? ORDER BY "index" LIMIT 1
            """.trimIndent(),
            arrayOf(page.toString())
        ) ?: return@withContext PagesLookupResponse()
        val last = db.stringQuery(
            """
            SELECT sura || ':' || aya FROM ayas
            WHERE page = ? ORDER BY "index" DESC LIMIT 1
            """.trimIndent(),
            arrayOf(page.toString())
        ) ?: first
        PagesLookupResponse(
            lookupRange = LookupRange(from = first, to = last),
            pages = mapOf(page.toString() to PageInfo(from = first, to = last)),
            totalPage = MushafConfig.TOTAL_PAGES
        )
    }

    suspend fun getTafsirByAyah(ayahKey: String, resourceId: String = LocalQuranConfig.TAFSIR_RESOURCE_ID): TafsirPayload? =
        withContext(Dispatchers.IO) {
        val parts = ayahKey.split(":")
        if (parts.size != 2) return@withContext null
        val sura = parts[0].toIntOrNull() ?: return@withContext null
        val aya = parts[1].toIntOrNull() ?: return@withContext null

        if (resourceId == LocalQuranConfig.TAFSIR_JALALAYN_ID) {
            val jalalayn = database.openReadable().stringQuery(
                "SELECT jalalayn FROM ayas WHERE sura = ? AND aya = ?",
                arrayOf(sura.toString(), aya.toString())
            )?.takeIf { it.isNotBlank() } ?: return@withContext null
            return@withContext TafsirPayload(
                text = jalalayn,
                resourceId = 0,
                resourceName = "Tafsir Jalalayn"
            )
        }

        val index = database.openReadable().intQueryOrNull(
            "SELECT \"index\" FROM ayas WHERE sura = ? AND aya = ?",
            arrayOf(sura.toString(), aya.toString())
        ) ?: return@withContext null
        val text = tafsirTexts().getOrNull(index - 1)?.takeIf { it.isNotBlank() }
            ?: return@withContext null
        TafsirPayload(
            text = text,
            resourceId = 0,
            resourceName = "Tafsir Indonesia (lokal)"
        )
    }

    suspend fun searchVerses(
        query: String,
        translationId: Int,
        limit: Int = 10
    ): List<RandomAyahPayload> = withContext(Dispatchers.IO) {
        val column = translationColumn(translationId)
        val db = database.openReadable()
        db.rawQuery(
            """
            SELECT $AYAH_SELECT
            FROM ayas a
            JOIN suras s ON s."index" = a.sura
            WHERE $column LIKE ?
            ORDER BY a."index"
            LIMIT ?
            """.trimIndent(),
            arrayOf("%$query%", limit.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toVersePayload(translationId, LocalQuranConfig.DEFAULT_RECITATION_ID, false))
                }
            }
        }
    }

    private fun loadJuz(db: SQLiteDatabase, juzNumber: Int): QuranJuz? {
        db.rawQuery(
            """
            SELECT sura, MIN(aya) AS min_aya, MAX(aya) AS max_aya, COUNT(*) AS cnt
            FROM ayas
            WHERE juz = ?
            GROUP BY sura
            ORDER BY sura
            """.trimIndent(),
            arrayOf(juzNumber.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val mapping = linkedMapOf<String, String>()
            var total = 0
            do {
                val sura = cursor.getInt(0)
                val minAya = cursor.getInt(1)
                val maxAya = cursor.getInt(2)
                total += cursor.getInt(3)
                mapping[sura.toString()] = if (minAya == maxAya) "$minAya" else "$minAya-$maxAya"
            } while (cursor.moveToNext())
            return QuranJuz(
                id = juzNumber,
                juzNumber = juzNumber,
                verseMapping = mapping,
                versesCount = total
            )
        }
    }

    private fun loadWordsForVerse(
        db: SQLiteDatabase,
        sura: Int,
        aya: Int,
        mushafPage: Int
    ): List<QuranWord> {
        return db.rawQuery(
            """
            SELECT position, text_madani, line_number, page_number, char_type, translate_id
            FROM words
            WHERE sura = ? AND aya = ?
            ORDER BY position
            """.trimIndent(),
            arrayOf(sura.toString(), aya.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val pageNum = cursor.getIntOrNull("page_number") ?: mushafPage
                    add(
                        QuranWord(
                            position = cursor.getIntOrNull("position"),
                            charTypeName = cursor.getStringOrNull("char_type"),
                            lineNumber = cursor.getIntOrNull("line_number"),
                            pageNumber = pageNum,
                            textUthmani = cursor.getStringOrNull("text_madani"),
                            verseKey = "$sura:$aya"
                        )
                    )
                }
            }
        }
    }

    private fun tafsirTexts(): List<String> {
        tafsirByIndex?.let { return it }
        val assetNames = context.assets.list("quran").orEmpty().toSet()
        val loaded = when {
            "tafsir.json" in assetNames -> readTafsirJson(context.assets.open("quran/tafsir.json"))
            "tafsir.json.gz" in assetNames -> context.assets.open("quran/tafsir.json.gz").use { input ->
                GZIPInputStream(input).use { gzip -> readTafsirJson(gzip) }
            }
            else -> emptyList()
        }
        tafsirByIndex = loaded
        return loaded
    }

    private fun readTafsirJson(input: java.io.InputStream): List<String> {
        val text = input.bufferedReader().readText()
        val array = org.json.JSONArray(text)
        return buildList {
            for (i in 0 until array.length()) {
                add(array.optString(i, ""))
            }
        }
    }

    private suspend fun mushafPageForVerseKey(verseKey: String): Int? {
        val parts = verseKey.split(":")
        if (parts.size != 2) return null
        val sura = parts[0].toIntOrNull() ?: return null
        val aya = parts[1].toIntOrNull() ?: return null
        return mushafPageForVerse(sura, aya)
    }

    private fun paginatedResponse(
        verses: List<RandomAyahPayload>,
        page: Int,
        perPage: Int,
        total: Int
    ): VersesByChapterResponse {
        val totalPages = if (total == 0) 0 else ((total + perPage - 1) / perPage)
        val nextPage = if (page < totalPages) page + 1 else null
        return VersesByChapterResponse(
            verses = verses,
            pagination = ContentPagination(
                perPage = perPage,
                currentPage = page,
                nextPage = nextPage,
                totalPages = totalPages,
                totalRecords = total
            )
        )
    }

    private fun Cursor.toChapter(language: AppLanguage): QuranChapter {
        val id = getInt(getColumnIndexOrThrow("index"))
        val tname = getStringOrNull("tname").orEmpty()
        val enameId = getStringOrNull("ename").orEmpty()
        val enameEnglish = getStringOrNull("ename_english").orEmpty()
        val meaning = when (language) {
            AppLanguage.ENGLISH -> enameEnglish.ifBlank { enameId }
            AppLanguage.MALAY, AppLanguage.INDONESIAN -> enameId.ifBlank { enameEnglish }
        }
        val meaningLanguage = when (language) {
            AppLanguage.ENGLISH -> "english"
            AppLanguage.MALAY -> "malay"
            AppLanguage.INDONESIAN -> "indonesian"
        }
        val ayas = getIntOrNull("ayas") ?: 0
        val type = getStringOrNull("type").orEmpty()
        val firstPage = getIntOrNull("first_page") ?: 1
        val lastPage = getIntOrNull("last_page") ?: firstPage
        val revelation = when (type.lowercase()) {
            "mekkah", "mecca", "makkah" -> "makkah"
            "madinah", "medina" -> "madinah"
            else -> type.lowercase()
        }
        return QuranChapter(
            id = id,
            revelationPlace = revelation,
            pages = (firstPage..lastPage).toList(),
            nameSimple = tname,
            nameComplex = tname,
            nameArabic = getStringOrNull("name"),
            versesCount = ayas,
            translatedName = ChapterTranslatedName(name = meaning, languageName = meaningLanguage)
        )
    }

    private fun Cursor.toVersePayload(
        translationId: Int,
        recitationId: Int,
        loadWords: Boolean
    ): RandomAyahPayload {
        val sura = getInt(getColumnIndexOrThrow("sura"))
        val aya = getInt(getColumnIndexOrThrow("aya"))
        val globalAyah = getInt(getColumnIndexOrThrow("global_ayah"))
        val translationText = translationText(translationId)
        val audioUrl = MurottalOfflineStore.localUrlIfDownloaded(context, recitationId, globalAyah)
            ?: LocalQuranConfig.murottalUrl(recitationId, globalAyah)
        return RandomAyahPayload(
            chapterId = sura,
            verseNumber = aya,
            verseKey = "$sura:$aya",
            textUthmani = getStringOrNull("text"),
            pageNumber = getIntOrNull("page"),
            juzNumber = getIntOrNull("juz"),
            transliterationId = getStringOrNull("transliteration_id"),
            transliterationEn = getStringOrNull("transliteration_en"),
            jalalayn = getStringOrNull("jalalayn"),
            audio = AudioPayload(url = audioUrl),
            translations = translationText?.let { text ->
                listOf(
                    InlineTranslation(
                        id = translationId,
                        resourceId = translationId,
                        text = text,
                        resourceName = translationResourceName(translationId)
                    )
                )
            }
        )
    }

    private fun Cursor.translationText(translationId: Int): String? {
        val column = when (translationId) {
            LocalQuranConfig.TRANSLATION_ENGLISH -> "translation_en"
            LocalQuranConfig.TRANSLATION_MALAY -> "malay"
            LocalQuranConfig.TRANSLATION_KEMENAG -> "kemenag"
            else -> "indonesian"
        }
        return getStringOrNull(column)?.takeIf { it.isNotBlank() }
    }

    private fun translationResourceName(translationId: Int): String = when (translationId) {
        LocalQuranConfig.TRANSLATION_ENGLISH -> "English"
        LocalQuranConfig.TRANSLATION_MALAY -> "Malay"
        LocalQuranConfig.TRANSLATION_KEMENAG -> "Kemenag"
        else -> "Indonesian"
    }

    private fun translationColumn(translationId: Int): String = when (translationId) {
        LocalQuranConfig.TRANSLATION_ENGLISH -> "a.translation_en"
        LocalQuranConfig.TRANSLATION_MALAY -> "a.malay"
        LocalQuranConfig.TRANSLATION_KEMENAG -> "a.kemenag"
        else -> "a.indonesian"
    }

    private fun SQLiteDatabase.stringQuery(sql: String, args: Array<String>?): String? =
        rawQuery(sql, args).use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getString(0) else null
        }

    private fun SQLiteDatabase.intQuery(sql: String, args: Array<String>?): Int =
        rawQuery(sql, args).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

    private fun SQLiteDatabase.intQueryOrNull(sql: String, args: Array<String>?): Int? =
        rawQuery(sql, args).use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getInt(0) else null
        }

    private fun Cursor.getStringOrNull(column: String): String? {
        val idx = getColumnIndex(column)
        if (idx < 0 || isNull(idx)) return null
        return getString(idx)
    }

    private fun Cursor.getIntOrNull(column: String): Int? {
        val idx = getColumnIndex(column)
        if (idx < 0 || isNull(idx)) return null
        return when (getType(idx)) {
            Cursor.FIELD_TYPE_INTEGER -> getInt(idx)
            Cursor.FIELD_TYPE_STRING -> getString(idx).toIntOrNull()
            else -> getString(idx)?.toIntOrNull()
        }
    }
}
