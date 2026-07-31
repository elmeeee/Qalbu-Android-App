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
        val overlay = overlayFor(language) ?: return entries
        return entries.map { entry ->
            val title = overlay.categories[entry.slug] ?: entry.title
            entry.copy(title = title)
        }
    }

    fun localizeDoas(items: List<DoaItem>, language: AppLanguage): List<DoaItem> {
        val overlay = overlayFor(language)
        return items.map { item ->
            val localized = item.id?.let { overlay?.doa?.get(it) }
            val resolvedTranslation = localized?.translation
                ?: item.translationData?.resolve(language)
                ?: item.translation
            item.copy(
                title = localized?.title ?: item.title,
                translationData = FlexibleTranslationData(defaultTranslation = resolvedTranslation, map = item.translationData?.map)
            )
        }
    }

    fun localizeDhikr(
        slug: String,
        bundles: List<DhikrBundle>,
        language: AppLanguage
    ): List<DhikrBundle> {
        val overlay = overlayFor(language)
        return bundles.mapIndexed { bundleIndex, bundle ->
            val titleKey = "$slug:$bundleIndex"
            val localizedTitle = overlay?.dhikrTitles?.get(titleKey) ?: bundle.title
            val localizedContent = bundle.content.orEmpty().mapIndexed { contentIndex, item ->
                val contentKey = "$slug:$bundleIndex:$contentIndex"
                val localized = overlay?.dhikrContent?.get(contentKey)
                val resolvedTitle = localized?.title
                    ?: item.titleData?.resolve(language)
                    ?: item.title
                val resolvedTranslation = localized?.translation
                    ?: item.translationData?.resolve(language)
                    ?: item.translation
                val resolvedNotes = localized?.notes
                    ?: item.notesData?.resolve(language)
                    ?: item.notes
                item.copy(
                    titleData = FlexibleTranslationData(defaultTranslation = resolvedTitle, map = item.titleData?.map),
                    translationData = FlexibleTranslationData(defaultTranslation = resolvedTranslation, map = item.translationData?.map),
                    notesData = FlexibleTranslationData(defaultTranslation = resolvedNotes, map = item.notesData?.map)
                )
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
