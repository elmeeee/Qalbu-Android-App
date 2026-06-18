package app.kamy.saatApp.infrastructure.cache

import app.kamy.saatApp.domain.model.InlineTranslation
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.infrastructure.preferences.DailyVerseSnapshot

fun DailyVerseSnapshot.toVersePayload(): RandomAyahPayload = RandomAyahPayload(
    chapterId = chapterNumber,
    verseNumber = ayahNumber,
    verseKey = verseKey ?: "$chapterNumber:$ayahNumber",
    textUthmani = arabic.takeIf { it.isNotBlank() },
    transliterationId = transliterationId,
    transliterationEn = transliterationEn,
    translations = listOf(InlineTranslation(text = translation))
)
