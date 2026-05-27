package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.HadithsByAyahResponse
import app.kamy.qalbuApp.domain.model.QFTranslation
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.domain.model.VersesByChapterResponse
import app.kamy.qalbuApp.infrastructure.network.api.ContentApiService
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors iOS Infrastructure/Services/QuranContentRepository.swift.
 *
 * In-memory cache for chapters (1h TTL — iOS APICache).
 * All calls go through [qfCall] which maps Retrofit exceptions to QFError.
 */
@Singleton
class ContentRepository @Inject constructor(
    private val api: ContentApiService,
    private val translationStore: TranslationPreferencesStore
) {
    private fun selectedTranslationId(): Int = translationStore.currentTranslationId()
    private val chaptersTtlMs = 60 * 60 * 1000L
    private var cachedChapters: List<QuranChapter>? = null
    private var chaptersCachedAt: Long = 0L
    private val chaptersMutex = Mutex()

    suspend fun getChapters(force: Boolean = false): List<QuranChapter> = chaptersMutex.withLock {
        val now = System.currentTimeMillis()
        if (!force) {
            cachedChapters?.let {
                if (now - chaptersCachedAt < chaptersTtlMs) return it
            }
        }
        val response = qfCall { api.getChapters() }
        cachedChapters = response.chapters
        chaptersCachedAt = now
        response.chapters
    }

    suspend fun getRandomAyah(translationId: Int = selectedTranslationId()): RandomAyahPayload? =
        qfCall { api.getRandomVerse(translations = translationId.toString()).verse }

    suspend fun getVersesByChapter(
        chapterNumber: Int,
        page: Int = 1,
        perPage: Int = 50,
        translationId: Int = selectedTranslationId()
    ): VersesByChapterResponse = qfCall {
        api.getVersesByChapter(
            chapterNumber = chapterNumber,
            page = page,
            perPage = perPage,
            translations = translationId.toString()
        )
    }

    suspend fun getVerseByKey(
        verseKey: String,
        translationId: Int = selectedTranslationId()
    ) = qfCall {
        api.getVerseByKey(verseKey, translations = translationId.toString())
    }

    suspend fun getRecitations(): List<RecitationPayload> = qfCall {
        api.getRecitations().recitations.orEmpty()
    }

    suspend fun getTranslations(): List<QFTranslation> = qfCall {
        api.getTranslations().translations
    }

    suspend fun getTafsirByAyah(resourceId: String, ayahKey: String): TafsirPayload? =
        qfCall { api.getTafsirByAyah(resourceId, ayahKey).tafsir }

    suspend fun getHadithsByAyah(
        ayahKey: String,
        page: Int = 1,
        limit: Int = 5
    ): HadithsByAyahResponse = qfCall { api.getHadithsByAyah(ayahKey, page = page, limit = limit) }

    fun clearCache() {
        cachedChapters = null
        chaptersCachedAt = 0L
    }
}
