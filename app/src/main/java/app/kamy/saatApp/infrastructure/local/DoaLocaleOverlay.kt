package app.kamy.saatApp.infrastructure.local

import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.DhikrBundle
import app.kamy.saatApp.domain.model.DoaCatalogEntry
import app.kamy.saatApp.domain.model.DoaItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoaLocaleOverlay @Inject constructor() {

    fun localizeCatalog(entries: List<DoaCatalogEntry>, language: AppLanguage): List<DoaCatalogEntry> {
        val overlay = overlayFor(language) ?: return entries
        return entries.map { entry ->
            val title = overlay.categories[entry.slug] ?: entry.title
            entry.copy(title = title)
        }
    }

    fun localizeDoas(items: List<DoaItem>, language: AppLanguage): List<DoaItem> {
        val overlay = overlayFor(language) ?: return items
        return items.map { item ->
            val localized = item.id?.let { overlay.doa[it] }
            item.copy(
                title = localized?.title ?: item.title,
                translation = localized?.translation ?: item.translation
            )
        }
    }

    fun localizeDhikr(
        slug: String,
        bundles: List<DhikrBundle>,
        language: AppLanguage
    ): List<DhikrBundle> {
        val overlay = overlayFor(language) ?: return bundles
        return bundles.mapIndexed { bundleIndex, bundle ->
            val titleKey = "$slug:$bundleIndex"
            val localizedTitle = overlay.dhikrTitles[titleKey] ?: bundle.title
            val localizedContent = bundle.content.orEmpty().mapIndexed { contentIndex, item ->
                val contentKey = "$slug:$bundleIndex:$contentIndex"
                val localized = overlay.dhikrContent[contentKey]
                item.copy(translation = localized?.translation ?: item.translation)
            }
            bundle.copy(title = localizedTitle, content = localizedContent)
        }
    }

    fun invalidateCache() = Unit

    private fun overlayFor(language: AppLanguage): DoaLocaleData? = when (language) {
        AppLanguage.ENGLISH -> DoaBuiltinLocales.english
        AppLanguage.MALAY -> DoaBuiltinLocales.malay
        AppLanguage.INDONESIAN -> null
    }
}
