package app.kamy.saatApp.infrastructure.repository

import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.EncyclopediaCategory
import app.kamy.saatApp.domain.model.EncyclopediaTopic
import app.kamy.saatApp.domain.model.GlossaryTerm
import app.kamy.saatApp.infrastructure.local.LocalEncyclopediaDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncyclopediaRepository @Inject constructor(
    private val localDataSource: LocalEncyclopediaDataSource
) {
    suspend fun getTopicsByCategory(
        category: EncyclopediaCategory,
        query: String = "",
        language: AppLanguage = AppLanguage.INDONESIAN
    ): List<EncyclopediaTopic> {
        val all = localDataSource.getTopics()
        val filteredCategory = when (category) {
            EncyclopediaCategory.ALL -> all
            EncyclopediaCategory.GLOSSARY -> emptyList()
            else -> all.filter { it.categoryId.equals(category.id, ignoreCase = true) }
        }

        if (query.isBlank()) return filteredCategory

        val q = query.trim().lowercase()
        return filteredCategory.filter { topic ->
            topic.localizedTitle(language).lowercase().contains(q) ||
                    topic.localizedSubtitle(language).lowercase().contains(q) ||
                    topic.localizedSummary(language).lowercase().contains(q) ||
                    topic.localizedContent(language).lowercase().contains(q)
        }
    }

    suspend fun getTopicById(id: String): EncyclopediaTopic? {
        return localDataSource.getTopics().firstOrNull { it.id.equals(id, ignoreCase = true) }
    }

    suspend fun getGlossaryTerms(
        query: String = "",
        language: AppLanguage = AppLanguage.INDONESIAN
    ): List<GlossaryTerm> {
        val all = localDataSource.getGlossary()
        if (query.isBlank()) return all

        val q = query.trim().lowercase()
        return all.filter { term ->
            term.term.lowercase().contains(q) ||
                    term.termAr.contains(q) ||
                    term.localizedDefinition(language).lowercase().contains(q)
        }
    }
}
