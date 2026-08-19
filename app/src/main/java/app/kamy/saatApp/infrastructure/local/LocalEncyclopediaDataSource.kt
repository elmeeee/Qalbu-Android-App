package app.kamy.saatApp.infrastructure.local

import android.content.Context
import app.kamy.saatApp.domain.model.EncyclopediaTopic
import app.kamy.saatApp.domain.model.GlossaryTerm
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalEncyclopediaDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile
    private var cachedTopics: List<EncyclopediaTopic>? = null

    @Volatile
    private var cachedGlossary: List<GlossaryTerm>? = null

    suspend fun getTopics(): List<EncyclopediaTopic> = withContext(Dispatchers.IO) {
        cachedTopics?.let { return@withContext it }
        val jsonString = runCatching {
            context.assets.open("encyclopedia/topics.json").bufferedReader().use { it.readText() }
        }.getOrNull().orEmpty()

        if (jsonString.isBlank()) return@withContext emptyList()

        val list = mutableListOf<EncyclopediaTopic>()
        runCatching {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val qRefList = mutableListOf<app.kamy.saatApp.domain.model.QuranReference>()
                val qRefArray = obj.optJSONArray("quranReferences")
                if (qRefArray != null) {
                    for (j in 0 until qRefArray.length()) {
                        val refObj = qRefArray.optJSONObject(j) ?: continue
                        qRefList.add(
                            app.kamy.saatApp.domain.model.QuranReference(
                                surahNumber = refObj.optInt("surahNumber", 1),
                                surahName = refObj.optString("surahName"),
                                surahNameEn = refObj.optString("surahNameEn"),
                                surahNameMs = refObj.optString("surahNameMs"),
                                ayahRange = refObj.optString("ayahRange"),
                                verseTextAr = refObj.optString("verseTextAr"),
                                verseTextTranslation = refObj.optString("verseTextTranslation"),
                                verseTextTranslationEn = refObj.optString("verseTextTranslationEn"),
                                verseTextTranslationMs = refObj.optString("verseTextTranslationMs")
                            )
                        )
                    }
                }

                list.add(
                    EncyclopediaTopic(
                        id = obj.optString("id"),
                        categoryId = obj.optString("categoryId"),
                        title = obj.optString("title"),
                        titleEn = obj.optString("titleEn"),
                        titleMs = obj.optString("titleMs"),
                        subtitle = obj.optString("subtitle"),
                        subtitleEn = obj.optString("subtitleEn"),
                        subtitleMs = obj.optString("subtitleMs"),
                        icon = obj.optString("icon", "ic_encyclopedia_custom"),
                        readTimeMinutes = obj.optInt("readTimeMinutes", 5),
                        summary = obj.optString("summary"),
                        summaryEn = obj.optString("summaryEn"),
                        summaryMs = obj.optString("summaryMs"),
                        content = obj.optString("content"),
                        contentEn = obj.optString("contentEn"),
                        contentMs = obj.optString("contentMs"),
                        quranReferences = qRefList
                    )
                )
            }
        }
        cachedTopics = list
        list
    }

    suspend fun getGlossary(): List<GlossaryTerm> = withContext(Dispatchers.IO) {
        cachedGlossary?.let { return@withContext it }
        val jsonString = runCatching {
            context.assets.open("encyclopedia/glossary.json").bufferedReader().use { it.readText() }
        }.getOrNull().orEmpty()

        if (jsonString.isBlank()) return@withContext emptyList()

        val list = mutableListOf<GlossaryTerm>()
        runCatching {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    GlossaryTerm(
                        id = obj.optString("id"),
                        term = obj.optString("term"),
                        termAr = obj.optString("termAr"),
                        definition = obj.optString("definition"),
                        definitionEn = obj.optString("definitionEn"),
                        definitionMs = obj.optString("definitionMs")
                    )
                )
            }
        }
        cachedGlossary = list
        list
    }
}
