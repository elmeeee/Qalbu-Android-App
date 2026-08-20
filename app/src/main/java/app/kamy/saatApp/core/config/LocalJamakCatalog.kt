package app.kamy.saatApp.core.config

import android.content.Context
import app.kamy.saatApp.domain.model.JamakDalilItem
import app.kamy.saatApp.domain.model.JamakGuideData
import app.kamy.saatApp.domain.model.JamakNiatItem
import app.kamy.saatApp.domain.model.JamakRuleItem
import app.kamy.saatApp.domain.model.JamakStepItem
import app.kamy.saatApp.domain.model.JamakType
import app.kamy.saatApp.domain.model.JamakTypeInfo
import org.json.JSONArray
import org.json.JSONObject

object LocalJamakCatalog {

    private var cachedData: JamakGuideData? = null

    fun getGuideData(context: Context): JamakGuideData {
        cachedData?.let { return it }
        val loaded = loadFromAssets(context)
        cachedData = loaded
        return loaded
    }

    private fun loadFromAssets(context: Context): JamakGuideData = runCatching {
        val jsonText = context.assets.open("prayer_guide/jamak_qashar_guide.json")
            .bufferedReader()
            .use { it.readText() }
        val root = JSONObject(jsonText)

        // Parse Types Info List
        val typeInfos = mutableListOf<JamakTypeInfo>()
        val typesArr = root.optJSONArray("types") ?: JSONArray()
        for (i in 0 until typesArr.length()) {
            val t = typesArr.getJSONObject(i)
            val key = t.optString("key")
            val type = JamakType.fromKey(key)
            typeInfos.add(
                JamakTypeInfo(
                    type = type,
                    titleId = t.optString("titleId"),
                    titleMs = t.optString("titleMs"),
                    titleEn = t.optString("titleEn"),
                    subtitleId = t.optString("subtitleId"),
                    subtitleMs = t.optString("subtitleMs"),
                    subtitleEn = t.optString("subtitleEn")
                )
            )
        }

        // Parse Dalil List
        val dalilList = mutableListOf<JamakDalilItem>()
        val dalilArr = root.optJSONArray("dalilList") ?: JSONArray()
        for (i in 0 until dalilArr.length()) {
            val d = dalilArr.getJSONObject(i)
            dalilList.add(
                JamakDalilItem(
                    id = d.optString("id"),
                    titleId = d.optString("titleId"),
                    titleMs = d.optString("titleMs"),
                    titleEn = d.optString("titleEn"),
                    arabic = d.optString("arabic"),
                    transliteration = d.optString("transliteration"),
                    translationId = d.optString("translationId"),
                    translationMs = d.optString("translationMs"),
                    translationEn = d.optString("translationEn"),
                    referenceId = d.optString("referenceId"),
                    referenceMs = d.optString("referenceMs"),
                    referenceEn = d.optString("referenceEn"),
                    explanationId = d.optString("explanationId"),
                    explanationMs = d.optString("explanationMs"),
                    explanationEn = d.optString("explanationEn")
                )
            )
        }

        // Parse Rules List
        val rules = mutableListOf<JamakRuleItem>()
        val rulesArr = root.optJSONArray("rules") ?: JSONArray()
        for (i in 0 until rulesArr.length()) {
            val r = rulesArr.getJSONObject(i)
            rules.add(
                JamakRuleItem(
                    id = r.optString("id"),
                    titleId = r.optString("titleId"),
                    titleMs = r.optString("titleMs"),
                    titleEn = r.optString("titleEn"),
                    descId = r.optString("descId"),
                    descMs = r.optString("descMs"),
                    descEn = r.optString("descEn"),
                    detailId = r.optString("detailId"),
                    detailMs = r.optString("detailMs"),
                    detailEn = r.optString("detailEn"),
                    dalilRefId = r.optString("dalilRefId").takeIf { it.isNotBlank() },
                    dalilRefMs = r.optString("dalilRefMs").takeIf { it.isNotBlank() },
                    dalilRefEn = r.optString("dalilRefEn").takeIf { it.isNotBlank() }
                )
            )
        }

        // Parse Niat List
        val niatList = mutableListOf<JamakNiatItem>()
        val niatArr = root.optJSONArray("niatList") ?: JSONArray()
        for (i in 0 until niatArr.length()) {
            val n = niatArr.getJSONObject(i)
            niatList.add(
                JamakNiatItem(
                    id = n.optString("id"),
                    titleId = n.optString("titleId"),
                    titleMs = n.optString("titleMs"),
                    titleEn = n.optString("titleEn"),
                    arabic = n.optString("arabic"),
                    transliteration = n.optString("transliteration"),
                    translationId = n.optString("translationId"),
                    translationMs = n.optString("translationMs"),
                    translationEn = n.optString("translationEn"),
                    noteId = n.optString("noteId").takeIf { it.isNotBlank() },
                    noteMs = n.optString("noteMs").takeIf { it.isNotBlank() },
                    noteEn = n.optString("noteEn").takeIf { it.isNotBlank() },
                    hadithRef = n.optString("hadithRef").takeIf { it.isNotBlank() }
                )
            )
        }

        // Parse Steps Map
        val stepsMap = mutableMapOf<JamakType, List<JamakStepItem>>()
        val stepsObj = root.optJSONObject("steps") ?: JSONObject()
        JamakType.entries.forEach { type ->
            val typeArr = stepsObj.optJSONArray(type.name) ?: JSONArray()
            val typeSteps = mutableListOf<JamakStepItem>()
            for (i in 0 until typeArr.length()) {
                val s = typeArr.getJSONObject(i)
                typeSteps.add(
                    JamakStepItem(
                        stepNumber = s.optInt("stepNumber", i + 1),
                        titleId = s.optString("titleId"),
                        titleMs = s.optString("titleMs"),
                        titleEn = s.optString("titleEn"),
                        descId = s.optString("descId"),
                        descMs = s.optString("descMs"),
                        descEn = s.optString("descEn"),
                        arabic = s.optString("arabic").takeIf { it.isNotBlank() },
                        transliteration = s.optString("transliteration").takeIf { it.isNotBlank() },
                        translationId = s.optString("translationId").takeIf { it.isNotBlank() },
                        translationMs = s.optString("translationMs").takeIf { it.isNotBlank() },
                        translationEn = s.optString("translationEn").takeIf { it.isNotBlank() },
                        tipId = s.optString("tipId").takeIf { it.isNotBlank() },
                        tipMs = s.optString("tipMs").takeIf { it.isNotBlank() },
                        tipEn = s.optString("tipEn").takeIf { it.isNotBlank() }
                    )
                )
            }
            stepsMap[type] = typeSteps
        }

        JamakGuideData(
            typeInfos = typeInfos,
            dalilList = dalilList,
            rules = rules,
            niatList = niatList,
            stepsMap = stepsMap
        )
    }.getOrElse {
        JamakGuideData(
            typeInfos = emptyList(),
            dalilList = emptyList(),
            rules = emptyList(),
            niatList = emptyList(),
            stepsMap = emptyMap()
        )
    }
}
