package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.QfSearchHit
import app.kamy.qalbuApp.domain.model.QuickSearchResult
import app.kamy.qalbuApp.domain.model.SearchNavResult
import app.kamy.qalbuApp.domain.model.SearchVerseResult
import app.kamy.qalbuApp.domain.model.keyAsString
import app.kamy.qalbuApp.infrastructure.network.api.SearchApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val api: SearchApiService
) {

    suspend fun quickSearch(
        query: String,
        translationId: Int,
        navigationalLimit: Int = 5,
        versesLimit: Int = 10
    ): QuickSearchResult {
        val response = qfCall {
            api.search(
                mode = "quick",
                query = query,
                navigationalResultsNumber = navigationalLimit,
                versesResultsNumber = versesLimit,
                translationIds = translationId.toString(),
                highlight = 0
            )
        }
        val body = response.result ?: return QuickSearchResult()
        return QuickSearchResult(
            navigation = body.navigation.orEmpty().mapNotNull(::toNavResult),
            verses = body.verses.orEmpty().mapNotNull(::toVerseResult)
        )
    }

    private fun toNavResult(hit: QfSearchHit): SearchNavResult? {
        val type = hit.resultType?.lowercase().orEmpty()
        val key = hit.keyAsString()?.takeIf { it.isNotBlank() } ?: return null
        val name = hit.name?.takeIf { it.isNotBlank() } ?: key
        val chapterNumber = when (type) {
            "surah" -> key.toIntOrNull()
            else -> null
        }
        return SearchNavResult(
            type = type,
            key = key,
            name = name,
            chapterNumber = chapterNumber
        )
    }

    private fun toVerseResult(hit: QfSearchHit): SearchVerseResult? {
        val verseKey = hit.keyAsString()?.takeIf { it.isNotBlank() } ?: return null
        val parts = verseKey.split(":")
        if (parts.size != 2) return null
        val chapter = parts[0].toIntOrNull() ?: return null
        val ayah = parts[1].toIntOrNull() ?: return null
        val name = hit.name?.takeIf { it.isNotBlank() } ?: verseKey
        return SearchVerseResult(
            verseKey = verseKey,
            name = name,
            chapterNumber = chapter,
            ayahNumber = ayah
        )
    }
}
