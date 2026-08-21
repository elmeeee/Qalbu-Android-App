package app.kamy.saatApp.infrastructure.repository

import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.domain.model.HadithsByAyahResponse
import app.kamy.saatApp.domain.model.QuranTranslation
import app.kamy.saatApp.domain.model.QuranChapter
import app.kamy.saatApp.domain.model.QuranJuz
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.RecitationPayload
import app.kamy.saatApp.domain.model.TafsirPayload
import app.kamy.saatApp.domain.model.VersesByChapterResponse
import app.kamy.saatApp.infrastructure.local.LocalHadithDataSource
import app.kamy.saatApp.infrastructure.local.LocalQuranDataSource
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quran content from bundled SQLite ([qurannew.db]) and local Tafsir/Hadith assets.
 */
@Singleton
class QuranRepository @Inject constructor(
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
        local.getChapters(appLanguageStore.current())

    suspend fun getJuzs(force: Boolean = false): List<QuranJuz> =
        normalizeJuzs(local.getJuzs())

    suspend fun getJuz(juzNumber: Int): QuranJuz? =
        local.getJuz(juzNumber)

    suspend fun getRandomAyah(
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = selectedRecitationId(),
        force: Boolean = false
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
        audioRecitationId: Int = selectedRecitationId(),
        forceRefresh: Boolean = false
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

    suspend fun getVerseByKey(
        verseKey: String,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = selectedRecitationId()
    ) = local.getVerseByKey(verseKey, translationId, audioRecitationId)

    suspend fun getVersesByRange(
        chapterNumber: Int,
        startAyah: Int,
        endAyah: Int,
        translationId: Int = selectedTranslationId(),
        audioRecitationId: Int = selectedRecitationId()
    ): List<RandomAyahPayload> =
        local.getVersesByRange(chapterNumber, startAyah, endAyah, translationId, audioRecitationId)

    suspend fun getRecitations(): List<RecitationPayload> =
        LocalQuranConfig.recitations

    suspend fun getTranslations(): List<QuranTranslation> =
        LocalQuranConfig.translations

    suspend fun getTafsirByAyah(
        ayahKey: String,
        sourceId: String = LocalQuranConfig.TAFSIR_WAJIZ_ID
    ): TafsirPayload? {
        return local.getTafsirByAyah(ayahKey, sourceId, language = appLanguageStore.current().tag)
    }

    suspend fun getJalalaynByAyah(ayahKey: String): TafsirPayload? {
        return local.getTafsirByAyah(ayahKey, LocalQuranConfig.TAFSIR_JALALAYN_ID, language = appLanguageStore.current().tag)
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
