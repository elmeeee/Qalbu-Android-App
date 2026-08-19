package app.kamy.saatApp.features.tools.hajj.data

import android.content.Context
import app.kamy.saatApp.features.tools.hajj.model.*
import org.json.JSONArray
import org.json.JSONObject

data class HajjUmrahData(
    val umrahSteps: List<ManasikStep>,
    val hajjSteps: List<ManasikStep>,
    val manasikDuas: List<HajjDoaItem>,
    val dalilList: List<HajjDalilItem>,
    val madhhabRulings: List<MadhhabRuling>,
    val damRules: List<DamRuleItem>,
    val miqatLocations: List<MiqatLocation>,
    val historicSites: List<HistoricZiarahSite>,
    val checklistCategories: List<HajjChecklistCategory>
)

object HajjUmrahCatalog {

    private var cachedData: HajjUmrahData? = null

    fun getData(context: Context): HajjUmrahData {
        cachedData?.let { return it }
        val data = loadFromAssets(context)
        cachedData = data
        return data
    }

    private fun loadFromAssets(context: Context): HajjUmrahData = runCatching {
        val jsonText = context.assets.open("hajj/hajj_umrah_guide.json")
            .bufferedReader()
            .use { it.readText() }
        val root = JSONObject(jsonText)

        val umrahSteps = parseManasikSteps(root.optJSONArray("umrahSteps"))
        val hajjSteps = parseManasikSteps(root.optJSONArray("hajjSteps"))
        val manasikDuas = parseDuas(root.optJSONArray("manasikDuas"))
        val dalilList = parseDalils(root.optJSONArray("dalilList"))
        val madhhabRulings = parseMadhhabRulings(root.optJSONArray("madhhabRulings"))
        val damRules = parseDamRules(root.optJSONArray("damRules"))
        val miqatLocations = parseMiqats(root.optJSONArray("miqatLocations"))
        val historicSites = parseHistoricSites(root.optJSONArray("historicSites"))
        val checklistCategories = parseChecklists(root.optJSONArray("checklistCategories"))

        HajjUmrahData(
            umrahSteps = umrahSteps,
            hajjSteps = hajjSteps,
            manasikDuas = manasikDuas,
            dalilList = dalilList,
            madhhabRulings = madhhabRulings,
            damRules = damRules,
            miqatLocations = miqatLocations,
            historicSites = historicSites,
            checklistCategories = checklistCategories
        )
    }.getOrElse {
        HajjUmrahData(
            umrahSteps = emptyList(),
            hajjSteps = emptyList(),
            manasikDuas = emptyList(),
            dalilList = emptyList(),
            madhhabRulings = emptyList(),
            damRules = emptyList(),
            miqatLocations = emptyList(),
            historicSites = emptyList(),
            checklistCategories = emptyList()
        )
    }

    private fun parseLocalizedText(obj: JSONObject?): LocalizedText {
        if (obj == null) return LocalizedText("", "", "")
        return LocalizedText(
            id = obj.optString("id"),
            en = obj.optString("en"),
            ms = obj.optString("ms")
        )
    }

    private fun parseLocalizedList(arr: JSONArray?): List<LocalizedText> {
        if (arr == null) return emptyList()
        val list = mutableListOf<LocalizedText>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i)
            if (obj != null) {
                list.add(parseLocalizedText(obj))
            }
        }
        return list
    }

    private fun parseManasikSteps(arr: JSONArray?): List<ManasikStep> {
        if (arr == null) return emptyList()
        val list = mutableListOf<ManasikStep>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            list.add(
                ManasikStep(
                    id = obj.optString("id"),
                    stepNumber = obj.optInt("stepNumber", i + 1),
                    title = parseLocalizedText(obj.optJSONObject("title")),
                    subtitle = parseLocalizedText(obj.optJSONObject("subtitle")),
                    location = parseLocalizedText(obj.optJSONObject("location")),
                    timeOrDay = parseLocalizedText(obj.optJSONObject("timeOrDay")),
                    isRukun = obj.optBoolean("isRukun", false),
                    description = parseLocalizedText(obj.optJSONObject("description")),
                    detailedSteps = parseLocalizedList(obj.optJSONArray("detailedSteps")),
                    doaRefId = obj.optString("doaRefId").takeIf { it.isNotBlank() },
                    dalilQuran = obj.optString("dalilQuran").takeIf { it.isNotBlank() },
                    dalilHadits = obj.optString("dalilHadits").takeIf { it.isNotBlank() },
                    prohibitions = parseLocalizedList(obj.optJSONArray("prohibitions")),
                    commonMistakes = parseLocalizedList(obj.optJSONArray("commonMistakes")),
                    practicalTips = parseLocalizedList(obj.optJSONArray("practicalTips"))
                )
            )
        }
        return list
    }

    private fun parseDuas(arr: JSONArray?): List<HajjDoaItem> {
        if (arr == null) return emptyList()
        val list = mutableListOf<HajjDoaItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            list.add(
                HajjDoaItem(
                    id = obj.optString("id"),
                    title = parseLocalizedText(obj.optJSONObject("title")),
                    category = parseLocalizedText(obj.optJSONObject("category")),
                    arabic = obj.optString("arabic"),
                    latin = obj.optString("latin"),
                    translation = parseLocalizedText(obj.optJSONObject("translation")),
                    contextAndBenefits = parseLocalizedText(obj.optJSONObject("contextAndBenefits")),
                    reference = obj.optString("reference"),
                    occasions = parseLocalizedText(obj.optJSONObject("occasions"))
                )
            )
        }
        return list
    }

    private fun parseDalils(arr: JSONArray?): List<HajjDalilItem> {
        if (arr == null) return emptyList()
        val list = mutableListOf<HajjDalilItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            list.add(
                HajjDalilItem(
                    id = obj.optString("id"),
                    title = parseLocalizedText(obj.optJSONObject("title")),
                    category = parseLocalizedText(obj.optJSONObject("category")),
                    isQuran = obj.optBoolean("isQuran", true),
                    surahOrNarrator = obj.optString("surahOrNarrator"),
                    arabic = obj.optString("arabic"),
                    latin = obj.optString("latin").takeIf { it.isNotBlank() },
                    translation = parseLocalizedText(obj.optJSONObject("translation")),
                    tafsirExplanation = parseLocalizedText(obj.optJSONObject("tafsirExplanation")),
                    keyLessons = parseLocalizedList(obj.optJSONArray("keyLessons"))
                )
            )
        }
        return list
    }

    private fun parseMadhhabRulings(arr: JSONArray?): List<MadhhabRuling> {
        if (arr == null) return emptyList()
        val list = mutableListOf<MadhhabRuling>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            list.add(
                MadhhabRuling(
                    id = obj.optString("id"),
                    topic = parseLocalizedText(obj.optJSONObject("topic")),
                    generalExplanation = parseLocalizedText(obj.optJSONObject("generalExplanation")),
                    syafii = parseLocalizedText(obj.optJSONObject("syafii")),
                    hanafi = parseLocalizedText(obj.optJSONObject("hanafi")),
                    maliki = parseLocalizedText(obj.optJSONObject("maliki")),
                    hanbali = parseLocalizedText(obj.optJSONObject("hanbali")),
                    rajihConclusion = parseLocalizedText(obj.optJSONObject("rajihConclusion"))
                )
            )
        }
        return list
    }

    private fun parseDamRules(arr: JSONArray?): List<DamRuleItem> {
        if (arr == null) return emptyList()
        val list = mutableListOf<DamRuleItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            list.add(
                DamRuleItem(
                    id = obj.optString("id"),
                    violation = parseLocalizedText(obj.optJSONObject("violation")),
                    category = parseLocalizedText(obj.optJSONObject("category")),
                    penalty = parseLocalizedText(obj.optJSONObject("penalty")),
                    alternatives = parseLocalizedText(obj.optJSONObject("alternatives")),
                    dalil = obj.optString("dalil")
                )
            )
        }
        return list
    }

    private fun parseMiqats(arr: JSONArray?): List<MiqatLocation> {
        if (arr == null) return emptyList()
        val list = mutableListOf<MiqatLocation>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            list.add(
                MiqatLocation(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    arabicName = obj.optString("arabicName"),
                    distanceFromMakkah = obj.optString("distanceFromMakkah"),
                    direction = parseLocalizedText(obj.optJSONObject("direction")),
                    dedicatedFor = parseLocalizedText(obj.optJSONObject("dedicatedFor")),
                    description = parseLocalizedText(obj.optJSONObject("description")),
                    facilities = parseLocalizedList(obj.optJSONArray("facilities"))
                )
            )
        }
        return list
    }

    private fun parseHistoricSites(arr: JSONArray?): List<HistoricZiarahSite> {
        if (arr == null) return emptyList()
        val list = mutableListOf<HistoricZiarahSite>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            list.add(
                HistoricZiarahSite(
                    id = obj.optString("id"),
                    name = parseLocalizedText(obj.optJSONObject("name")),
                    city = parseLocalizedText(obj.optJSONObject("city")),
                    historicalSignificance = parseLocalizedText(obj.optJSONObject("historicalSignificance")),
                    adabAndDoa = parseLocalizedText(obj.optJSONObject("adabAndDoa"))
                )
            )
        }
        return list
    }

    private fun parseChecklists(arr: JSONArray?): List<HajjChecklistCategory> {
        if (arr == null) return emptyList()
        val list = mutableListOf<HajjChecklistCategory>()
        for (i in 0 until arr.length()) {
            val catObj = arr.optJSONObject(i) ?: continue
            val itemsArr = catObj.optJSONArray("items")
            val items = mutableListOf<HajjChecklistItem>()
            if (itemsArr != null) {
                for (j in 0 until itemsArr.length()) {
                    val itemObj = itemsArr.optJSONObject(j) ?: continue
                    items.add(
                        HajjChecklistItem(
                            id = itemObj.optString("id"),
                            label = parseLocalizedText(itemObj.optJSONObject("label")),
                            note = if (itemObj.has("note")) parseLocalizedText(itemObj.optJSONObject("note")) else null,
                            isCrucial = itemObj.optBoolean("isCrucial", false)
                        )
                    )
                }
            }
            list.add(
                HajjChecklistCategory(
                    id = catObj.optString("id"),
                    title = parseLocalizedText(catObj.optJSONObject("title")),
                    items = items
                )
            )
        }
        return list
    }
}
