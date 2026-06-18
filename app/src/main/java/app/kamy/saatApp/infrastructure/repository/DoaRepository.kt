package app.kamy.saatApp.infrastructure.repository

import app.kamy.saatApp.domain.model.DhikrBundle
import app.kamy.saatApp.domain.model.DoaCatalogEntry
import app.kamy.saatApp.domain.model.DoaItem
import app.kamy.saatApp.infrastructure.local.DoaLocaleOverlay
import app.kamy.saatApp.infrastructure.local.LocalDoaDataSource
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoaRepository @Inject constructor(
    private val local: LocalDoaDataSource,
    private val localeOverlay: DoaLocaleOverlay,
    private val appLanguageStore: AppLanguageStore
) {
    suspend fun getCatalog(): List<DoaCatalogEntry> =
        localeOverlay.localizeCatalog(local.getCatalog(), appLanguageStore.current())

    suspend fun getDailyDoas(): List<DoaItem> =
        localeOverlay.localizeDoas(local.getDailyDoas(), appLanguageStore.current())

    suspend fun getDoas(slug: String): List<DoaItem> =
        localeOverlay.localizeDoas(local.getDoasBySlug(slug), appLanguageStore.current())

    suspend fun getDhikr(slug: String): List<DhikrBundle> =
        localeOverlay.localizeDhikr(slug, local.getDhikrBySlug(slug), appLanguageStore.current())

    fun invalidateLocaleCache() = localeOverlay.invalidateCache()
}
