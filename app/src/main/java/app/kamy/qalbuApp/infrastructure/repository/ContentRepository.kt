package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.config.LocalQuranConfig
import app.kamy.qalbuApp.core.config.MushafConfig
import app.kamy.qalbuApp.domain.model.HadithsByAyahResponse
import app.kamy.qalbuApp.domain.model.PagesLookupResponse
import app.kamy.qalbuApp.domain.model.QFTranslation
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.QuranJuz
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.domain.model.VersesByChapterResponse
import app.kamy.qalbuApp.infrastructure.local.LocalHadithDataSource
import app.kamy.qalbuApp.infrastructure.local.LocalQuranDataSource
import app.kamy.qalbuApp.infrastructure.preferences.AppLanguageStore
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quran content from bundled SQLite ([qurannew.db]) — no Quran Foundation Content API.
 */
@Singleton
class ContentRepository @Inject constructor(
    private val local: LocalQuranDataSource,
    private val hadith: LocalHadithDataSource,
    private val translationStore: TranslationPreferencesStore,
    private val appLanguageStore: AppLanguageStore
) {
    private fun selectedTranslationId(): Int =
        LocalQuranConfig.normalizeTranslationId(translationStore.currentTranslationId())

    private fun selectedRecitationId(): Int =
        LocalQuranConfig.normalizeRecitationId(translationStore.currentRecitationId())

    suspend fun getChapters(force: Boolean = false): List<QuranChapter> =
        local.getChapters()

    suspend fun getJuzs(force: Boolean = false): List<QuranJuz> =
        normalizeJuzs(local.getJuzs())

    suspend fun getJuz(juzNumber: Int): QuranJuz? =
        local.getJuz(juzNumber)

    suspend fun getRandomAyah(
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = selectedRecitationId()
    ): RandomAyahPayload? =
        local.getRandomAyah(translationId, audioRecitationId)

    suspend fun getDailyAyah(
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = selectedRecitationId()
    ): RandomAyahPayload? =
        local.getDailyAyah(translationId, audioRecitationId)

    suspend fun getVersesByChapter(
        chapterNumber: Int,
        page: Int = 1,
        perPage: Int = 50,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = selectedRecitationId()
    ): VersesByChapterResponse =
        local.getVersesByChapter(chapterNumber, page, perPage, translationId, audioRecitationId)

    suspend fun getVersesByJuz(
        juzNumber: Int,
        page: Int = 1,
        perPage: Int = 50,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = selectedRecitationId()
    ): VersesByChapterResponse =
        local.getVersesByJuz(juzNumber, page, perPage, translationId, audioRecitationId)

    suspend fun getVersesByMushafPage(
        mushafPage: Int,
        perPage: Int = 50,
        translationId: Int = selectedTranslationId(),
        mushafId: Int = MushafConfig.MUSHAF_ID,
        forceRefresh: Boolean = false
    ): VersesByChapterResponse =
        local.getVersesByMushafPage(mushafPage, translationId, selectedRecitationId())

    suspend fun getPagesLookup(
        mushafId: Int = MushafConfig.MUSHAF_ID,
        chapterNumber: Int? = null,
        juzNumber: Int? = null,
        pageNumber: Int? = null,
        fromVerse: String? = null,
        toVerse: String? = null
    ): PagesLookupResponse =
        local.getPagesLookup(chapterNumber, juzNumber, pageNumber, fromVerse, toVerse)

    suspend fun firstMushafPageForJuz(juzNumber: Int, mushafId: Int = MushafConfig.MUSHAF_ID): Int? =
        local.firstMushafPageForJuz(juzNumber)

    suspend fun firstMushafPageForChapter(chapterNumber: Int, mushafId: Int = MushafConfig.MUSHAF_ID): Int? =
        local.firstMushafPageForChapter(chapterNumber)

    suspend fun mushafPageForVerse(
        chapterNumber: Int,
        verseNumber: Int,
        mushafId: Int = MushafConfig.MUSHAF_ID
    ): Int? = local.mushafPageForVerse(chapterNumber, verseNumber)

    suspend fun mushafPageForVerseKey(verseKey: String, mushafId: Int = MushafConfig.MUSHAF_ID): Int? {
        val parts = verseKey.split(":")
        if (parts.size != 2) return null
        val chapter = parts[0].toIntOrNull() ?: return null
        val ayah = parts[1].toIntOrNull() ?: return null
        return local.mushafPageForVerse(chapter, ayah)
    }

    suspend fun getVerseByKey(
        verseKey: String,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = selectedRecitationId()
    ) = local.getVerseByKey(verseKey, translationId, audioRecitationId)

    suspend fun getRecitations(): List<RecitationPayload> =
        LocalQuranConfig.recitations

    suspend fun getTranslations(): List<QFTranslation> =
        LocalQuranConfig.translations

    suspend fun getTafsirByAyah(ayahKey: String): TafsirPayload? {
        val translationId = selectedTranslationId()
        if (!LocalQuranConfig.supportsTafsir(translationId)) return null
        return local.getTafsirByAyah(ayahKey, LocalQuranConfig.TAFSIR_JALALAYN_ID)
            ?: local.getTafsirByAyah(ayahKey, LocalQuranConfig.TAFSIR_RESOURCE_ID)
    }

    suspend fun getJalalaynByAyah(ayahKey: String): TafsirPayload? {
        if (!LocalQuranConfig.supportsTafsir(selectedTranslationId())) return null
        return local.getTafsirByAyah(ayahKey, LocalQuranConfig.TAFSIR_JALALAYN_ID)
    }

    suspend fun getHadithsByAyah(
        ayahKey: String,
        page: Int = 1,
        limit: Int = 5
    ): HadithsByAyahResponse = hadith.getHadithsByAyah(
        ayahKey = ayahKey,
        page = page,
        limit = limit,
        language = currentApiLanguage()
    )

    fun clearCache() {
        // Local bundle — nothing to clear.
    }

    fun currentApiLanguage(): String = appLanguageStore.current().apiCode
}

private fun normalizeJuzs(juzs: List<QuranJuz>): List<QuranJuz> =
    juzs
        .asSequence()
        .filter { it.juzNumber in 1..30 }
        .groupBy { it.juzNumber }
        .map { (_, group) -> group.maxByOrNull { it.verseMapping.size } ?: group.first() }
        .sortedBy { it.juzNumber }
