package app.kamy.saatApp.core.config

import android.content.Context
import app.kamy.saatApp.domain.model.NiatItem
import app.kamy.saatApp.domain.model.PrayerStepItem
import app.kamy.saatApp.domain.model.SunnahPrayerItem
import org.json.JSONArray
import org.json.JSONObject

object LocalSunnahPrayerCatalog {

    private var cachedItems: List<SunnahPrayerItem>? = null

    fun getItems(context: Context): List<SunnahPrayerItem> {
        cachedItems?.let { return it }
        val items = loadFromAssets(context)
        if (items.isNotEmpty()) {
            cachedItems = items
        }
        return items
    }

    private fun loadFromAssets(context: Context): List<SunnahPrayerItem> = runCatching {
        val jsonText = context.assets.open("prayer_guide/sunnah_prayers.json")
            .bufferedReader()
            .use { it.readText() }
        val jsonArray = JSONArray(jsonText)
        val list = mutableListOf<SunnahPrayerItem>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            
            val niatArr = obj.optJSONArray("niatList") ?: JSONArray()
            val niatList = mutableListOf<NiatItem>()
            for (j in 0 until niatArr.length()) {
                val n = niatArr.getJSONObject(j)
                niatList.add(
                    NiatItem(
                        titleId = n.optString("titleId"),
                        titleMs = n.optString("titleMs"),
                        titleEn = n.optString("titleEn"),
                        arabic = n.optString("arabic"),
                        latin = n.optString("latin"),
                        translationId = n.optString("translationId"),
                        translationMs = n.optString("translationMs"),
                        translationEn = n.optString("translationEn")
                    )
                )
            }

            val stepArr = obj.optJSONArray("steps") ?: JSONArray()
            val steps = mutableListOf<PrayerStepItem>()
            for (k in 0 until stepArr.length()) {
                val s = stepArr.getJSONObject(k)
                steps.add(
                    PrayerStepItem(
                        stepNumber = s.optInt("stepNumber", k + 1),
                        titleId = s.optString("titleId"),
                        titleMs = s.optString("titleMs"),
                        titleEn = s.optString("titleEn"),
                        descId = s.optString("descId"),
                        descMs = s.optString("descMs"),
                        descEn = s.optString("descEn"),
                        arabic = s.optString("arabic").takeIf { it.isNotBlank() },
                        latin = s.optString("latin").takeIf { it.isNotBlank() }
                    )
                )
            }

            val defaultRakaat = obj.optString("rakaatInfo")
            val rakaatInfoId = obj.optString("rakaatInfoId", defaultRakaat)
            val rakaatInfoMs = obj.optString("rakaatInfoMs", defaultRakaat)
            val rakaatInfoEn = obj.optString("rakaatInfoEn", defaultRakaat)

            list.add(
                SunnahPrayerItem(
                    id = obj.getString("id"),
                    category = obj.getString("category"),
                    titleId = obj.getString("titleId"),
                    titleMs = obj.getString("titleMs"),
                    titleEn = obj.getString("titleEn"),
                    summaryId = obj.optString("summaryId"),
                    summaryMs = obj.optString("summaryMs"),
                    summaryEn = obj.optString("summaryEn"),
                    waktuId = obj.optString("waktuId"),
                    waktuMs = obj.optString("waktuMs"),
                    waktuEn = obj.optString("waktuEn"),
                    rakaatInfoId = rakaatInfoId,
                    rakaatInfoMs = rakaatInfoMs,
                    rakaatInfoEn = rakaatInfoEn,
                    fadhilahId = obj.optString("fadhilahId"),
                    fadhilahMs = obj.optString("fadhilahMs"),
                    fadhilahEn = obj.optString("fadhilahEn"),
                    dalilHadithId = obj.optString("dalilHadithId").takeIf { it.isNotBlank() },
                    dalilHadithMs = obj.optString("dalilHadithMs").takeIf { it.isNotBlank() },
                    dalilHadithEn = obj.optString("dalilHadithEn").takeIf { it.isNotBlank() },
                    hadithReference = obj.optString("hadithReference").takeIf { it.isNotBlank() },
                    recommendedSurahsId = obj.optString("recommendedSurahsId").takeIf { it.isNotBlank() },
                    recommendedSurahsMs = obj.optString("recommendedSurahsMs").takeIf { it.isNotBlank() },
                    recommendedSurahsEn = obj.optString("recommendedSurahsEn").takeIf { it.isNotBlank() },
                    niatList = niatList,
                    steps = steps,
                    doaArabic = obj.optString("doaArabic").takeIf { it.isNotBlank() },
                    doaLatin = obj.optString("doaLatin").takeIf { it.isNotBlank() },
                    doaTranslationId = obj.optString("doaTranslationId").takeIf { it.isNotBlank() },
                    doaTranslationMs = obj.optString("doaTranslationMs").takeIf { it.isNotBlank() },
                    doaTranslationEn = obj.optString("doaTranslationEn").takeIf { it.isNotBlank() }
                )
            )
        }
        list
    }.getOrDefault(emptyList())
}
