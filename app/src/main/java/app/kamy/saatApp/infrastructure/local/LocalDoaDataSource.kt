package app.kamy.saatApp.infrastructure.local

import android.content.Context
import app.kamy.saatApp.domain.model.DhikrBundle
import app.kamy.saatApp.domain.model.DhikrListResponse
import app.kamy.saatApp.domain.model.DoaCatalogEntry
import app.kamy.saatApp.domain.model.DoaCatalogKind
import app.kamy.saatApp.domain.model.DoaCategoriesResponse
import app.kamy.saatApp.domain.model.DoaItem
import app.kamy.saatApp.domain.model.DoaListResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class LocalDoaDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    private val dhikrSlugs = setOf(
        "morning-dhikir",
        "evening-dhikir",
        "dhikir-after-salah",
        "sleep-dhikir",
        "dzikiralmathuratkubro",
        "dzikiralmathuratsughro"
    )

    suspend fun getCatalog(): List<DoaCatalogEntry> = withContext(Dispatchers.IO) {
        val categories = loadCategories().data.orEmpty()
        categories.mapNotNull { cat ->
            val slug = cat.slug?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val title = cat.name?.takeIf { it.isNotBlank() } ?: slug
            val kind = if (slug in dhikrSlugs) DoaCatalogKind.DHIKR else DoaCatalogKind.DOA
            DoaCatalogEntry(slug = slug, title = title, kind = kind, nameData = cat.nameData)
        }
    }

    suspend fun getDailyDoas(): List<DoaItem> = withContext(Dispatchers.IO) {
        loadDoaResponse("daily.json").data.orEmpty()
    }

    suspend fun getDoasBySlug(slug: String): List<DoaItem> = withContext(Dispatchers.IO) {
        val file = "duas_$slug.json"
        if (assetExists("doa/$file")) {
            loadDoaResponse(file).data.orEmpty()
        } else if (slug == "daily") {
            getDailyDoas()
        } else {
            emptyList()
        }
    }

    suspend fun getDhikrBySlug(slug: String): List<DhikrBundle> = withContext(Dispatchers.IO) {
        val file = "dhikir_$slug.json"
        if (!assetExists("doa/$file")) return@withContext emptyList()
        readAsset("doa/$file").let { text ->
            json.decodeFromString(DhikrListResponse.serializer(), text).data.orEmpty()
        }
    }

    private fun loadCategories(): DoaCategoriesResponse =
        readAsset("doa/categories.json").let {
            json.decodeFromString(DoaCategoriesResponse.serializer(), it)
        }

    private fun loadDoaResponse(fileName: String): DoaListResponse =
        readAsset("doa/$fileName").let {
            json.decodeFromString(DoaListResponse.serializer(), it)
        }

    private fun assetExists(path: String): Boolean =
        runCatching { context.assets.open(path).close(); true }.getOrDefault(false)

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }
}
