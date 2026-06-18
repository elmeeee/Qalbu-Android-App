package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.faraidh.EstateAssetInput
import app.kamy.saatApp.domain.faraidh.FaraidhParticipantNames
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FaraidhScenarioMeta(
    val id: String,
    val title: String,
    val updatedAt: Long
)

@Serializable
data class FaraidhScenarioData(
    val gender: String = "MALE",
    val madhhab: String = "SHAFII",
    val estate: EstateAssetInput = EstateAssetInput(),
    val names: FaraidhParticipantNames = FaraidhParticipantNames(),
    val husbandCount: Int = 0,
    val wifeCount: Int = 0,
    val fatherCount: Int = 0,
    val motherCount: Int = 0,
    val sonCount: Int = 0,
    val daughterCount: Int = 0,
    val grandsonCount: Int = 0,
    val granddaughterCount: Int = 0,
    val fullBrotherCount: Int = 0,
    val fullSisterCount: Int = 0,
    val paternalBrotherCount: Int = 0,
    val paternalSisterCount: Int = 0,
    val maternalBrotherCount: Int = 0,
    val maternalSisterCount: Int = 0
)

@Singleton
class FaraidhScenarioStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun loadAutoDraft(): FaraidhScenarioData? =
        prefs.getString(KEY_AUTO_DRAFT, null)?.let {
            runCatching { json.decodeFromString<FaraidhScenarioData>(it) }.getOrNull()
        }

    fun saveAutoDraft(data: FaraidhScenarioData) {
        prefs.edit().putString(KEY_AUTO_DRAFT, json.encodeToString(data)).apply()
    }

    fun listScenarios(): List<FaraidhScenarioMeta> =
        prefs.getString(KEY_INDEX, null)?.let {
            runCatching { json.decodeFromString<List<FaraidhScenarioMeta>>(it) }.getOrElse { emptyList() }
        } ?: emptyList()

    fun loadScenario(id: String): FaraidhScenarioData? =
        prefs.getString(KEY_SCENARIO_PREFIX + id, null)?.let {
            runCatching { json.decodeFromString<FaraidhScenarioData>(it) }.getOrNull()
        }

    fun saveScenario(id: String, title: String, data: FaraidhScenarioData) {
        val meta = FaraidhScenarioMeta(id = id, title = title, updatedAt = System.currentTimeMillis())
        val index = listScenarios().filterNot { it.id == id } + meta
        prefs.edit()
            .putString(KEY_SCENARIO_PREFIX + id, json.encodeToString(data))
            .putString(KEY_INDEX, json.encodeToString(index.sortedByDescending { it.updatedAt }))
            .apply()
    }

    fun deleteScenario(id: String) {
        val index = listScenarios().filterNot { it.id == id }
        prefs.edit()
            .remove(KEY_SCENARIO_PREFIX + id)
            .putString(KEY_INDEX, json.encodeToString(index))
            .apply()
    }

    fun clearAutoDraft() {
        prefs.edit().remove(KEY_AUTO_DRAFT).apply()
    }

    companion object {
        private const val PREFS = "saat_faraidh_scenarios"
        private const val KEY_AUTO_DRAFT = "auto_draft"
        private const val KEY_INDEX = "index"
        private const val KEY_SCENARIO_PREFIX = "scenario_"
    }
}
