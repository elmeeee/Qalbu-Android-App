package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.QFError
import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.HadithsByAyahResponse
import app.kamy.qalbuApp.domain.model.QFTranslation
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.QuranJuz
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.domain.model.VersesByChapterResponse
import app.kamy.qalbuApp.infrastructure.cache.ContentDiskCache
import app.kamy.qalbuApp.infrastructure.network.api.ContentApiService
import app.kamy.qalbuApp.infrastructure.preferences.AppLanguageStore
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val api: ContentApiService,
    private val translationStore: TranslationPreferencesStore,
    private val appLanguageStore: AppLanguageStore,
    private val diskCache: ContentDiskCache
) {
    private fun selectedTranslationId(): Int = translationStore.currentTranslationId()
    private fun apiLanguage(): String = appLanguageStore.current().apiCode
    private val chaptersTtlMs = 60 * 60 * 1000L
    private var cachedChapters: List<QuranChapter>? = null
    private var chaptersCachedAt: Long = 0L
    private var chaptersCachedLanguage: String? = null
    private val chaptersMutex = Mutex()
    private val juzsTtlMs = 24 * 60 * 60 * 1000L
    private var cachedJuzs: List<QuranJuz>? = null
    private var juzsCachedAt: Long = 0L
    private val juzsMutex = Mutex()

    suspend fun getChapters(force: Boolean = false): List<QuranChapter> = chaptersMutex.withLock {
        val now = System.currentTimeMillis()
        val language = apiLanguage()
        if (!force) {
            cachedChapters?.let {
                if (chaptersCachedLanguage == language && now - chaptersCachedAt < chaptersTtlMs) return it
            }
            diskCache.loadChapters(language)?.let {
                cachedChapters = it
                chaptersCachedAt = now
                chaptersCachedLanguage = language
                return it
            }
        }
        try {
            val response = qfCall { api.getChapters(language = language) }
            cachedChapters = response.chapters
            chaptersCachedAt = now
            chaptersCachedLanguage = language
            diskCache.saveChapters(language, response.chapters)
            response.chapters
        } catch (e: QFError.Network) {
            loadChaptersFallback(language) ?: throw e
        }
    }

    private fun loadChaptersFallback(language: String): List<QuranChapter>? {
        cachedChapters?.takeIf { chaptersCachedLanguage == language }?.let { return it }
        return diskCache.loadChapters(language)?.also {
            cachedChapters = it
            chaptersCachedLanguage = language
            chaptersCachedAt = System.currentTimeMillis()
        }
    }

    suspend fun getJuzs(force: Boolean = false): List<QuranJuz> = juzsMutex.withLock {
        val now = System.currentTimeMillis()
        if (!force) {
            cachedJuzs?.let {
                if (now - juzsCachedAt < juzsTtlMs) return normalizeJuzs(it)
            }
            diskCache.loadJuzs()?.let {
                val normalized = normalizeJuzs(it)
                cachedJuzs = normalized
                juzsCachedAt = now
                return normalized
            }
        }
        try {
            val response = qfCall { api.getJuzs(mushaf = DEFAULT_MUSHAF_ID) }
            val normalized = normalizeJuzs(response.juzs)
            cachedJuzs = normalized
            juzsCachedAt = now
            diskCache.saveJuzs(normalized)
            normalized
        } catch (e: QFError.Network) {
            loadJuzsFallback() ?: throw e
        }
    }

    private fun loadJuzsFallback(): List<QuranJuz>? {
        cachedJuzs?.let { return normalizeJuzs(it) }
        return diskCache.loadJuzs()?.also {
            cachedJuzs = normalizeJuzs(it)
            juzsCachedAt = System.currentTimeMillis()
        }?.let(::normalizeJuzs)
    }

    suspend fun getJuz(juzNumber: Int): QuranJuz? {
        cachedJuzs?.find { it.juzNumber == juzNumber }?.let { return it }
        diskCache.loadJuz(juzNumber)?.let { return it }
        return try {
            qfCall { api.getJuzById(juzNumber, mushaf = DEFAULT_MUSHAF_ID) }.juz?.also {
                diskCache.saveJuz(it)
            }
        } catch (e: QFError.Network) {
            diskCache.loadJuz(juzNumber)
        }
    }

    suspend fun getRandomAyah(
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = translationStore.currentRecitationId()
    ): RandomAyahPayload? = qfCall {
        api.getRandomVerse(
            language = apiLanguage(),
            translations = translationId.toString(),
            audio = audioRecitationId
        ).verse
    }

    suspend fun getVersesByChapter(
        chapterNumber: Int,
        page: Int = 1,
        perPage: Int = 50,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = translationStore.currentRecitationId()
    ): VersesByChapterResponse {
        val language = apiLanguage()
        val cacheKey = diskCache.verseCacheKey("chapter", chapterNumber, page, translationId, language)
        return try {
            qfCall {
                api.getVersesByChapter(
                    chapterNumber = chapterNumber,
                    page = page,
                    perPage = perPage,
                    language = language,
                    translations = translationId.toString(),
                    audio = audioRecitationId
                )
            }.also { diskCache.saveVerses(cacheKey, it) }
        } catch (e: QFError.Network) {
            diskCache.loadVerses(cacheKey) ?: throw e
        }
    }

    suspend fun getVersesByJuz(
        juzNumber: Int,
        page: Int = 1,
        perPage: Int = 50,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = translationStore.currentRecitationId()
    ): VersesByChapterResponse {
        val language = apiLanguage()
        val cacheKey = diskCache.verseCacheKey("juz", juzNumber, page, translationId, language)
        return try {
            qfCall {
                api.getVersesByJuz(
                    juzNumber = juzNumber,
                    page = page,
                    perPage = perPage,
                    language = language,
                    translations = translationId.toString(),
                    audio = audioRecitationId
                )
            }.also { diskCache.saveVerses(cacheKey, it) }
        } catch (e: QFError.Network) {
            diskCache.loadVerses(cacheKey) ?: throw e
        }
    }

    suspend fun getVerseByKey(
        verseKey: String,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = translationStore.currentRecitationId()
    ) = qfCall {
        api.getVerseByKey(
            verseKey,
            language = apiLanguage(),
            translations = translationId.toString(),
            audio = audioRecitationId
        )
    }

    suspend fun getRecitations(): List<RecitationPayload> = qfCall {
        api.getRecitations(language = apiLanguage()).recitations.orEmpty()
    }

    suspend fun getTranslations(): List<QFTranslation> = qfCall {
        api.getTranslations(language = apiLanguage()).translations.orEmpty().filter { it.id > 0 }
    }

    suspend fun getTafsirByAyah(resourceId: String, ayahKey: String): TafsirPayload? =
        qfCall { api.getTafsirByAyah(resourceId, ayahKey).tafsir }

    suspend fun getHadithsByAyah(
        ayahKey: String,
        page: Int = 1,
        limit: Int = 5
    ): HadithsByAyahResponse = qfCall {
        api.getHadithsByAyah(ayahKey, language = apiLanguage(), page = page, limit = limit)
    }

    fun clearCache() {
        cachedChapters = null
        chaptersCachedAt = 0L
        chaptersCachedLanguage = null
        cachedJuzs = null
        juzsCachedAt = 0L
        diskCache.clearAll()
    }

    fun currentApiLanguage(): String = apiLanguage()

    companion object {
        /** QCF V2 (Uthmani) — avoids duplicate juz rows when the API returns multiple mushaf editions. */
        private const val DEFAULT_MUSHAF_ID = 1
    }
}

/** API may return one row per mushaf; keep a single entry per juz number (1–30). */
private fun normalizeJuzs(juzs: List<QuranJuz>): List<QuranJuz> =
    juzs
        .asSequence()
        .filter { it.juzNumber in 1..30 }
        .groupBy { it.juzNumber }
        .map { (_, group) -> group.maxByOrNull { it.verseMapping.size } ?: group.first() }
        .sortedBy { it.juzNumber }
