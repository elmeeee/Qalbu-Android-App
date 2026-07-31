package app.kamy.saatApp.infrastructure.local

import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.DhikrBundle
import app.kamy.saatApp.domain.model.DoaCatalogEntry
import app.kamy.saatApp.domain.model.DoaItem
import app.kamy.saatApp.domain.model.FlexibleTranslationData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoaLocaleOverlay @Inject constructor() {

    fun localizeCatalog(entries: List<DoaCatalogEntry>, language: AppLanguage): List<DoaCatalogEntry> {
        return entries.map { entry ->
            val title = entry.nameData?.resolve(language) ?: entry.title
            entry.copy(title = title)
        }
    }

    fun localizeDoas(items: List<DoaItem>, language: AppLanguage): List<DoaItem> {
        return items.map { item ->
            val resolvedTitle = item.titleData?.resolve(language) ?: item.title
            val resolvedTranslation = item.translationData?.resolve(language) ?: item.translation
            item.copy(
                titleData = FlexibleTranslationData(defaultTranslation = resolvedTitle, map = item.titleData?.map),
                translationData = FlexibleTranslationData(defaultTranslation = resolvedTranslation, map = item.translationData?.map)
            )
        }
    }

    fun localizeDhikr(
        slug: String,
        bundles: List<DhikrBundle>,
        language: AppLanguage
    ): List<DhikrBundle> {
        return bundles.map { bundle ->
            val localizedContent = bundle.content.orEmpty().map { item ->
                val resolvedTitle = item.titleData?.resolve(language) ?: item.title
                val resolvedTranslation = item.translationData?.resolve(language) ?: item.translation
                val resolvedNotes = item.notesData?.resolve(language) ?: item.notes
                item.copy(
                    titleData = FlexibleTranslationData(defaultTranslation = resolvedTitle, map = item.titleData?.map),
                    translationData = FlexibleTranslationData(defaultTranslation = resolvedTranslation, map = item.translationData?.map),
                    notesData = FlexibleTranslationData(defaultTranslation = resolvedNotes, map = item.notesData?.map)
                )
            }
            bundle.copy(content = localizedContent)
        }
    }

    fun invalidateCache() = Unit
}
