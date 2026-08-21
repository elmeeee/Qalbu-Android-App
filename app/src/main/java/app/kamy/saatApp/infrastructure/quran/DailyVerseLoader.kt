package app.kamy.saatApp.infrastructure.quran

import android.content.Context
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.quran.DailyVerseOccasion
import app.kamy.saatApp.domain.quran.DailyVerseResolver
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache
import app.kamy.saatApp.infrastructure.cache.toVersePayload
import app.kamy.saatApp.infrastructure.preferences.DailyVerseSnapshotStore
import app.kamy.saatApp.infrastructure.preferences.RamadanPreferencesStore
import app.kamy.saatApp.infrastructure.repository.KhgtCalendarRepository
import app.kamy.saatApp.infrastructure.repository.QuranRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class DailyVerseLoadResult(
    val verse: RandomAyahPayload,
    val referenceLabel: String,
    val occasion: DailyVerseOccasion,
    val fromCache: Boolean
)

@Singleton
class DailyVerseLoader @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val quranRepository: QuranRepository,
    private val khgtCalendarRepository: KhgtCalendarRepository
) {

    suspend fun loadForToday(refreshTranslation: Boolean = false): DailyVerseLoadResult? {
        val cached = DailyVerseSnapshotStore.loadForToday(appContext)
        if (cached != null && !refreshTranslation) {
            var verse = cached.toVersePayload()
            if (verse.audio?.url.isNullOrBlank() || (verse.transliterationId.isNullOrBlank() && verse.transliterationEn.isNullOrBlank())) {
                val key = cached.verseKey ?: "${cached.chapterNumber}:${cached.ayahNumber}"
                quranRepository.getVerseByKey(key)?.let { fresh ->
                    val chapterName = fresh.chapterNumber?.let { num ->
                        quranRepository.getChapters().find { it.id == num }?.displayComplexName
                    }
                    DailyVerseSnapshotStore.save(
                        context = appContext,
                        verse = fresh,
                        surahName = chapterName,
                        occasionKey = cached.occasionKey
                    )
                    verse = fresh
                }
            }
            val occasion = DailyVerseOccasion.fromKey(cached.occasionKey) ?: DailyVerseOccasion.Daily
            return DailyVerseLoadResult(
                verse = verse,
                referenceLabel = "${cached.surahName} - ${cached.ayahNumber}",
                occasion = occasion,
                fromCache = true
            )
        }

        val verseKey = cached?.verseKey ?: resolveTodayVerseKey()
        val verse = quranRepository.getVerseByKey(verseKey) ?: run {
            if (cached != null) {
                return DailyVerseLoadResult(
                    verse = cached.toVersePayload(),
                    referenceLabel = "${cached.surahName} - ${cached.ayahNumber}",
                    occasion = DailyVerseOccasion.fromKey(cached.occasionKey) ?: DailyVerseOccasion.Daily,
                    fromCache = true
                )
            }
            return quranRepository.getDailyAyah()?.let { fallback ->
                buildResult(fallback, DailyVerseOccasion.Daily, fromCache = false)
            }
        }

        val result = buildResult(
            verse = verse,
            occasion = resolveOccasionForKey(verseKey, cached?.occasionKey),
            fromCache = false
        )
        val chapterName = verse.chapterNumber?.let { num ->
            quranRepository.getChapters().find { it.id == num }?.displayComplexName
        }
        DailyVerseSnapshotStore.save(
            context = appContext,
            verse = verse,
            surahName = chapterName,
            occasionKey = result.occasion.key
        )
        return result
    }

    private suspend fun resolveTodayVerseKey(): String {
        val khgt = runCatching { khgtCalendarRepository.todayInfo() }.getOrNull()
        val prayerCache = PrayerDayCache.load(appContext)
        val context = DailyVerseResolver.contextForToday(
            dayKey = DailyVerseSnapshotStore.todayKey(),
            hijriLabel = khgt?.hijriLabel ?: prayerCache?.hijriLabel,
            eventTitle = khgt?.eventTitle,
            isRamadanSeason = RamadanPreferencesStore.isRamadanSeason(appContext)
        )
        return DailyVerseResolver.resolve(context).verseKey
    }

    private suspend fun buildResult(
        verse: RandomAyahPayload,
        occasion: DailyVerseOccasion,
        fromCache: Boolean
    ): DailyVerseLoadResult {
        val chapters = quranRepository.getChapters()
        val chapterName = verse.chapterNumber?.let { num ->
            chapters.find { it.id == num }?.displayComplexName
        }
        return DailyVerseLoadResult(
            verse = verse,
            referenceLabel = verse.referenceLabel(chapterName).orEmpty(),
            occasion = occasion,
            fromCache = fromCache
        )
    }

    private suspend fun resolveOccasionForKey(verseKey: String, cachedOccasionKey: String?): DailyVerseOccasion {
        cachedOccasionKey?.let { DailyVerseOccasion.fromKey(it) }?.let { return it }
        val khgt = runCatching { khgtCalendarRepository.todayInfo() }.getOrNull()
        val prayerCache = PrayerDayCache.load(appContext)
        val context = DailyVerseResolver.contextForToday(
            dayKey = DailyVerseSnapshotStore.todayKey(),
            hijriLabel = khgt?.hijriLabel ?: prayerCache?.hijriLabel,
            eventTitle = khgt?.eventTitle,
            isRamadanSeason = RamadanPreferencesStore.isRamadanSeason(appContext)
        )
        val pick = DailyVerseResolver.resolve(context)
        return if (pick.verseKey == verseKey) pick.occasion else DailyVerseOccasion.Daily
    }
}
