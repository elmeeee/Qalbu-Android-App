package app.kamy.qalbuApp.infrastructure.cache

import app.kamy.qalbuApp.domain.model.InlineTranslation
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.infrastructure.preferences.DailyVerseSnapshot

fun DailyVerseSnapshot.toVersePayload(): RandomAyahPayload = RandomAyahPayload(
    chapterId = chapterNumber,
    verseNumber = ayahNumber,
    verseKey = verseKey ?: "$chapterNumber:$ayahNumber",
    textUthmani = arabic.takeIf { it.isNotBlank() },
    transliterationId = transliterationId,
    transliterationEn = transliterationEn,
    translations = listOf(InlineTranslation(text = translation))
)
