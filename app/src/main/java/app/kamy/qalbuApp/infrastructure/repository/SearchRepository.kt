package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.config.LocalQuranConfig
import app.kamy.qalbuApp.domain.model.QuickSearchResult
import app.kamy.qalbuApp.domain.model.SearchVerseResult
import app.kamy.qalbuApp.infrastructure.local.LocalQuranDataSource
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val local: LocalQuranDataSource,
    private val translationStore: TranslationPreferencesStore
) {

    suspend fun quickSearch(
        query: String,
        translationId: Int,
        navigationalLimit: Int = 5,
        versesLimit: Int = 10
    ): QuickSearchResult {
        val normalizedId = LocalQuranConfig.normalizeTranslationId(translationId)
        val verses = local.searchVerses(query.trim(), normalizedId, versesLimit)
        return QuickSearchResult(
            navigation = emptyList(),
            verses = verses.mapNotNull { verse ->
                val key = verse.verseKey ?: return@mapNotNull null
                val chapter = verse.chapterNumber ?: return@mapNotNull null
                val ayah = verse.resolvedVerseNumber ?: return@mapNotNull null
                val snippet = verse.translations?.firstOrNull()?.text?.take(120).orEmpty()
                SearchVerseResult(
                    verseKey = key,
                    name = snippet.ifBlank { key },
                    chapterNumber = chapter,
                    ayahNumber = ayah
                )
            }
        )
    }
}
