package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.model.FidyahMadhhab
import app.kamy.saatApp.domain.model.FidyahReason
import app.kamy.saatApp.domain.model.FidyahRecord
import org.json.JSONArray
import org.json.JSONObject

class FidyahStore private constructor(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPreferredMadhhab(): FidyahMadhhab {
        val id = prefs.getString(KEY_MADHHAB, FidyahMadhhab.SYAFII.id) ?: FidyahMadhhab.SYAFII.id
        return FidyahMadhhab.fromId(id)
    }

    fun setPreferredMadhhab(madhhab: FidyahMadhhab) {
        prefs.edit().putString(KEY_MADHHAB, madhhab.id).apply()
    }

    fun getCustomStaplePrice(): Double? {
        val price = prefs.getFloat(KEY_STAPLE_PRICE, -1f)
        return if (price > 0) price.toDouble() else null
    }

    fun setCustomStaplePrice(price: Double?) {
        if (price == null || price <= 0) {
            prefs.edit().remove(KEY_STAPLE_PRICE).apply()
        } else {
            prefs.edit().putFloat(KEY_STAPLE_PRICE, price.toFloat()).apply()
        }
    }

    fun getRecords(): List<FidyahRecord> {
        val jsonStr = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<FidyahRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FidyahRecord(
                        id = obj.getString("id"),
                        hijriYear = obj.optString("hijriYear", "1447 H"),
                        reason = FidyahReason.fromId(obj.optString("reason", "elderly_chronic")),
                        madhhab = FidyahMadhhab.fromId(obj.optString("madhhab", "syafii")),
                        missedDays = obj.optInt("missedDays", 0),
                        delayedYears = obj.optInt("delayedYears", 1),
                        paidDays = obj.optInt("paidDays", 0),
                        amountPaid = obj.optDouble("amountPaid", 0.0),
                        currencySymbol = obj.optString("currencySymbol", "Rp"),
                        isFullyPaid = obj.optBoolean("isFullyPaid", false),
                        completedQadhaDays = obj.optInt("completedQadhaDays", 0),
                        isQadhaCompleted = obj.optBoolean("isQadhaCompleted", false),
                        updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.updatedAtMillis }
        }.getOrDefault(emptyList())
    }

    fun saveRecord(record: FidyahRecord) {
        val current = getRecords().toMutableList()
        val index = current.indexOfFirst { it.id == record.id }
        if (index >= 0) {
            current[index] = record
        } else {
            current.add(0, record)
        }
        saveAll(current)
    }

    fun deleteRecord(recordId: String) {
        val current = getRecords().filterNot { it.id == recordId }
        saveAll(current)
    }

    private fun saveAll(records: List<FidyahRecord>) {
        val array = JSONArray()
        for (r in records) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("hijriYear", r.hijriYear)
                put("reason", r.reason.id)
                put("madhhab", r.madhhab.id)
                put("missedDays", r.missedDays)
                put("delayedYears", r.delayedYears)
                put("paidDays", r.paidDays)
                put("amountPaid", r.amountPaid)
                put("currencySymbol", r.currencySymbol)
                put("isFullyPaid", r.isFullyPaid)
                put("completedQadhaDays", r.completedQadhaDays)
                put("isQadhaCompleted", r.isQadhaCompleted)
                put("updatedAtMillis", r.updatedAtMillis)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_RECORDS, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "saat_fidyah_store"
        private const val KEY_MADHHAB = "preferred_madhhab"
        private const val KEY_STAPLE_PRICE = "custom_staple_price"
        private const val KEY_RECORDS = "fidyah_records_json"

        @Volatile
        private var instance: FidyahStore? = null

        fun from(context: Context): FidyahStore =
            instance ?: synchronized(this) {
                instance ?: FidyahStore(context).also { instance = it }
            }
    }
}
