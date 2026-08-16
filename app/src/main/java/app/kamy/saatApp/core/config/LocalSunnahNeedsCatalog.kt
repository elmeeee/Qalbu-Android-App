package app.kamy.saatApp.core.config

import android.content.Context
import app.kamy.saatApp.domain.model.SunnahActionStep
import app.kamy.saatApp.domain.model.SunnahNeedItem
import org.json.JSONArray

object LocalSunnahNeedsCatalog {

    private var cachedItems: List<SunnahNeedItem>? = null

    fun getItems(context: Context): List<SunnahNeedItem> {
        cachedItems?.let { return it }
        val items = loadFromAssets(context)
        if (items.isNotEmpty()) {
            cachedItems = items
        }
        return items
    }

    private fun loadFromAssets(context: Context): List<SunnahNeedItem> = runCatching {
        val jsonText = context.assets.open("prayer_guide/sunnah_needs.json")
            .bufferedReader()
            .use { it.readText() }
        val jsonArray = JSONArray(jsonText)
        val list = mutableListOf<SunnahNeedItem>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val stepArr = obj.optJSONArray("actionSteps") ?: JSONArray()
            val steps = mutableListOf<SunnahActionStep>()
            for (k in 0 until stepArr.length()) {
                val s = stepArr.getJSONObject(k)
                steps.add(
                    SunnahActionStep(
                        stepNumber = s.optInt("stepNumber", k + 1),
                        titleId = s.optString("titleId"),
                        titleMs = s.optString("titleMs"),
                        titleEn = s.optString("titleEn"),
                        descId = s.optString("descId"),
                        descMs = s.optString("descMs"),
                        descEn = s.optString("descEn"),
                        arabic = s.optString("arabic").takeIf { it.isNotBlank() },
                        latin = s.optString("latin").takeIf { it.isNotBlank() },
                        targetCount = s.optString("targetCount").takeIf { it.isNotBlank() }
                    )
                )
            }

            list.add(
                SunnahNeedItem(
                    id = obj.getString("id"),
                    category = obj.getString("category"),
                    titleId = obj.getString("titleId"),
                    titleMs = obj.getString("titleMs"),
                    titleEn = obj.getString("titleEn"),
                    subtitleId = obj.optString("subtitleId"),
                    subtitleMs = obj.optString("subtitleMs"),
                    subtitleEn = obj.optString("subtitleEn"),
                    descriptionId = obj.optString("descriptionId"),
                    descriptionMs = obj.optString("descriptionMs"),
                    descriptionEn = obj.optString("descriptionEn"),
                    hadithReference = obj.optString("hadithReference"),
                    dalilArabic = obj.optString("dalilArabic").takeIf { it.isNotBlank() },
                    dalilLatin = obj.optString("dalilLatin").takeIf { it.isNotBlank() },
                    dalilTranslationId = obj.optString("dalilTranslationId").takeIf { it.isNotBlank() },
                    dalilTranslationMs = obj.optString("dalilTranslationMs").takeIf { it.isNotBlank() },
                    dalilTranslationEn = obj.optString("dalilTranslationEn").takeIf { it.isNotBlank() },
                    actionSteps = steps
                )
            )
        }
        list
    }.getOrDefault(emptyList())
}
