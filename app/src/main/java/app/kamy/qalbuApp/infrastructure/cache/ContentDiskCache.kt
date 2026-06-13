package app.kamy.qalbuApp.infrastructure.cache

import android.content.Context
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.QuranJuz
import app.kamy.qalbuApp.domain.model.VersesByChapterResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentDiskCache @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json
) {
    private val root = File(context.filesDir, "offline_content").apply { mkdirs() }

    fun saveChapters(language: String, chapters: List<QuranChapter>) {
        write(file("chapters_$language.json")) {
            json.encodeToString(ChaptersEntry(language, System.currentTimeMillis(), chapters))
        }
    }

    fun loadChapters(language: String): List<QuranChapter>? =
        read<ChaptersEntry>(file("chapters_$language.json"))
            ?.takeIf { it.language == language }
            ?.chapters

    fun saveJuzs(juzs: List<QuranJuz>) {
        write(file("juzs.json")) {
            json.encodeToString(JuzsEntry(System.currentTimeMillis(), juzs))
        }
    }

    fun loadJuzs(): List<QuranJuz>? = read<JuzsEntry>(file("juzs.json"))?.juzs

    fun saveJuz(juz: QuranJuz) {
        write(file("juz_${juz.juzNumber}.json")) {
            json.encodeToString(juz)
        }
    }

    fun loadJuz(juzNumber: Int): QuranJuz? = read<QuranJuz>(file("juz_$juzNumber.json"))

    fun saveVerses(key: String, response: VersesByChapterResponse) {
        write(file("verses_$key.json")) {
            json.encodeToString(response)
        }
    }

    fun loadVerses(key: String): VersesByChapterResponse? =
        read<VersesByChapterResponse>(file("verses_$key.json"))

    fun verseCacheKey(
        scope: String,
        id: Int,
        page: Int,
        translationId: Int,
        language: String
    ): String = "${scope}_${id}_p${page}_t${translationId}_$language"

    fun clearAll() {
        root.listFiles()?.forEach { it.delete() }
    }

    private fun file(name: String): File = File(root, name)

    private inline fun write(file: File, block: () -> String) {
        runCatching { file.writeText(block()) }
    }

    private inline fun <reified T> read(file: File): T? {
        if (!file.exists()) return null
        return runCatching { json.decodeFromString<T>(file.readText()) }.getOrNull()
    }

    @Serializable
    private data class ChaptersEntry(
        val language: String,
        val savedAt: Long,
        val chapters: List<QuranChapter>
    )

    @Serializable
    private data class JuzsEntry(
        val savedAt: Long,
        val juzs: List<QuranJuz>
    )
}
