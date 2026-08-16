package app.kamy.saatApp.core.config

import android.content.Context
import app.kamy.saatApp.domain.model.JanazahDuaItem
import app.kamy.saatApp.domain.model.JanazahGuide
import app.kamy.saatApp.domain.model.JanazahNiatItem
import app.kamy.saatApp.domain.model.JanazahPositionGuide
import app.kamy.saatApp.domain.model.JanazahTakbirStep
import org.json.JSONArray
import org.json.JSONObject

object LocalJanazahGuideCatalog {

    private var cachedGuide: JanazahGuide? = null

    fun getGuide(context: Context): JanazahGuide {
        cachedGuide?.let { return it }
        val guide = loadFromAssets(context)
        cachedGuide = guide
        return guide
    }

    private fun loadFromAssets(context: Context): JanazahGuide = runCatching {
        val jsonText = context.assets.open("prayer_guide/janazah_guide.json")
            .bufferedReader()
            .use { it.readText() }
        val root = JSONObject(jsonText)

        val pillarsId = root.optJSONArray("pillarsId").toStringList()
        val pillarsMs = root.optJSONArray("pillarsMs").toStringList()
        val pillarsEn = root.optJSONArray("pillarsEn").toStringList()

        val conditionsId = root.optJSONArray("conditionsId").toStringList()
        val conditionsMs = root.optJSONArray("conditionsMs").toStringList()
        val conditionsEn = root.optJSONArray("conditionsEn").toStringList()

        val takbirSteps = mutableListOf<JanazahTakbirStep>()
        val takbirArr = root.optJSONArray("takbirSteps") ?: JSONArray()
        for (i in 0 until takbirArr.length()) {
            val s = takbirArr.getJSONObject(i)
            takbirSteps.add(
                JanazahTakbirStep(
                    takbirNumber = s.optInt("takbirNumber", i + 1),
                    titleId = s.optString("titleId"),
                    titleMs = s.optString("titleMs"),
                    titleEn = s.optString("titleEn"),
                    descId = s.optString("descId"),
                    descMs = s.optString("descMs"),
                    descEn = s.optString("descEn"),
                    arabic = s.optString("arabic"),
                    latin = s.optString("latin"),
                    translationId = s.optString("translationId"),
                    translationMs = s.optString("translationMs"),
                    translationEn = s.optString("translationEn"),
                    importantNotesId = s.optString("importantNotesId").takeIf { it.isNotBlank() },
                    importantNotesMs = s.optString("importantNotesMs").takeIf { it.isNotBlank() },
                    importantNotesEn = s.optString("importantNotesEn").takeIf { it.isNotBlank() }
                )
            )
        }

        val niatList = mutableListOf<JanazahNiatItem>()
        val niatArr = root.optJSONArray("niatList") ?: JSONArray()
        for (i in 0 until niatArr.length()) {
            val n = niatArr.getJSONObject(i)
            niatList.add(
                JanazahNiatItem(
                    id = n.optString("id"),
                    category = n.optString("category"),
                    titleId = n.optString("titleId"),
                    titleMs = n.optString("titleMs"),
                    titleEn = n.optString("titleEn"),
                    subtitleId = n.optString("subtitleId"),
                    subtitleMs = n.optString("subtitleMs"),
                    subtitleEn = n.optString("subtitleEn"),
                    arabic = n.optString("arabic"),
                    latin = n.optString("latin"),
                    translationId = n.optString("translationId"),
                    translationMs = n.optString("translationMs"),
                    translationEn = n.optString("translationEn")
                )
            )
        }

        val positionGuides = mutableListOf<JanazahPositionGuide>()
        val posArr = root.optJSONArray("positionGuides") ?: JSONArray()
        for (i in 0 until posArr.length()) {
            val p = posArr.getJSONObject(i)
            positionGuides.add(
                JanazahPositionGuide(
                    titleId = p.optString("titleId"),
                    titleMs = p.optString("titleMs"),
                    titleEn = p.optString("titleEn"),
                    imamPositionId = p.optString("imamPositionId"),
                    imamPositionMs = p.optString("imamPositionMs"),
                    imamPositionEn = p.optString("imamPositionEn"),
                    descriptionId = p.optString("descriptionId"),
                    descriptionMs = p.optString("descriptionMs"),
                    descriptionEn = p.optString("descriptionEn"),
                    hadithRef = p.optString("hadithRef")
                )
            )
        }

        val afterDuas = mutableListOf<JanazahDuaItem>()
        val duaArr = root.optJSONArray("afterDuas") ?: JSONArray()
        for (i in 0 until duaArr.length()) {
            val d = duaArr.getJSONObject(i)
            afterDuas.add(
                JanazahDuaItem(
                    titleId = d.optString("titleId"),
                    titleMs = d.optString("titleMs"),
                    titleEn = d.optString("titleEn"),
                    arabic = d.optString("arabic"),
                    latin = d.optString("latin"),
                    translationId = d.optString("translationId"),
                    translationMs = d.optString("translationMs"),
                    translationEn = d.optString("translationEn")
                )
            )
        }

        JanazahGuide(
            principleDescId = root.optString("principleDescId"),
            principleDescMs = root.optString("principleDescMs"),
            principleDescEn = root.optString("principleDescEn"),
            rewardHadithId = root.optString("rewardHadithId"),
            rewardHadithMs = root.optString("rewardHadithMs"),
            rewardHadithEn = root.optString("rewardHadithEn"),
            pillarsId = pillarsId,
            pillarsMs = pillarsMs,
            pillarsEn = pillarsEn,
            conditionsId = conditionsId,
            conditionsMs = conditionsMs,
            conditionsEn = conditionsEn,
            takbirSteps = takbirSteps,
            niatList = niatList,
            positionGuides = positionGuides,
            afterDuas = afterDuas
        )
    }.getOrElse {
        JanazahGuide(
            principleDescId = "",
            principleDescMs = "",
            principleDescEn = "",
            rewardHadithId = "",
            rewardHadithMs = "",
            rewardHadithEn = "",
            pillarsId = emptyList(),
            pillarsMs = emptyList(),
            pillarsEn = emptyList(),
            conditionsId = emptyList(),
            conditionsMs = emptyList(),
            conditionsEn = emptyList(),
            takbirSteps = emptyList(),
            niatList = emptyList(),
            positionGuides = emptyList(),
            afterDuas = emptyList()
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            list.add(optString(i))
        }
        return list
    }
}
