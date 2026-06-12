package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.HadithsByAyahResponse
import app.kamy.qalbuApp.domain.model.QFTranslation
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.QuranJuz
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.domain.model.VersesByChapterResponse
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
    private val appLanguageStore: AppLanguageStore
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
        }
        val response = qfCall { api.getChapters(language = language) }
        cachedChapters = response.chapters
        chaptersCachedAt = now
        chaptersCachedLanguage = language
        response.chapters
    }

    suspend fun getJuzs(force: Boolean = false): List<QuranJuz> = juzsMutex.withLock {
        val now = System.currentTimeMillis()
        if (!force) {
            cachedJuzs?.let {
                if (now - juzsCachedAt < juzsTtlMs) return it
            }
        }
        val response = qfCall { api.getJuzs() }
        val sorted = response.juzs.sortedBy { it.juzNumber }
        cachedJuzs = sorted
        juzsCachedAt = now
        sorted
    }

    suspend fun getJuz(juzNumber: Int): QuranJuz? {
        cachedJuzs?.find { it.juzNumber == juzNumber }?.let { return it }
        return qfCall { api.getJuzById(juzNumber) }.juz
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
    ): VersesByChapterResponse = qfCall {
        api.getVersesByChapter(
            chapterNumber = chapterNumber,
            page = page,
            perPage = perPage,
            language = apiLanguage(),
            translations = translationId.toString(),
            audio = audioRecitationId
        )
    }

    suspend fun getVersesByJuz(
        juzNumber: Int,
        page: Int = 1,
        perPage: Int = 50,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = translationStore.currentRecitationId()
    ): VersesByChapterResponse = qfCall {
        api.getVersesByJuz(
            juzNumber = juzNumber,
            page = page,
            perPage = perPage,
            language = apiLanguage(),
            translations = translationId.toString(),
            audio = audioRecitationId
        )
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
    }

    fun currentApiLanguage(): String = apiLanguage()
}
