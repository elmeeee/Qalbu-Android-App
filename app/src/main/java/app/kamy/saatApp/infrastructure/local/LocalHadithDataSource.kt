package app.kamy.saatApp.infrastructure.local

import android.content.Context
import app.kamy.saatApp.domain.model.HadithsByAyahResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class LocalHadithDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    private val cacheDir = File(context.filesDir, "hadith_cache").apply { mkdirs() }

    suspend fun getHadithsByAyah(
        ayahKey: String,
        page: Int = 1,
        limit: Int = 5,
        language: String = "en"
    ): HadithsByAyahResponse = withContext(Dispatchers.IO) {
        loadFromCache(ayahKey, page) ?: HadithsByAyahResponse(
            hadiths = emptyList(),
            page = page,
            limit = limit,
            hasMore = false
        )
    }

    private fun cacheFile(ayahKey: String, page: Int): File {
        val safeKey = ayahKey.replace(':', '_')
        return File(cacheDir, "${safeKey}_p$page.json")
    }

    private fun loadFromCache(ayahKey: String, page: Int): HadithsByAyahResponse? {
        val file = cacheFile(ayahKey, page)
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString(HadithsByAyahResponse.serializer(), file.readText())
        }.getOrNull()
    }
}
