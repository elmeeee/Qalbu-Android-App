package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context

data class DhikrPreset(
    val id: String,
    val labelResKey: String,
    val arabic: String,
    val target: Int
)

object DhikrStore {
    private const val PREFS = "qalbu_dhikr"
    private const val KEY_COUNT_PREFIX = "count_"
    private const val KEY_TOTAL_PREFIX = "total_"

    val presets: List<DhikrPreset> = listOf(
        DhikrPreset("subhanallah", "dhikr_subhanallah", "سُبْحَانَ اللَّهِ", 33),
        DhikrPreset("alhamdulillah", "dhikr_alhamdulillah", "الْحَمْدُ لِلَّهِ", 33),
        DhikrPreset("allahuakbar", "dhikr_allahuakbar", "اللَّهُ أَكْبَرُ", 34),
        DhikrPreset("istighfar", "dhikr_istighfar", "أَسْتَغْفِرُ اللَّهَ", 100),
        DhikrPreset("salawat", "dhikr_salawat", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّد", 100)
    )

    fun sessionCount(context: Context, presetId: String): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_COUNT_PREFIX + presetId, 0)

    fun totalCount(context: Context, presetId: String): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_TOTAL_PREFIX + presetId, 0)

    fun increment(context: Context, presetId: String): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val next = prefs.getInt(KEY_COUNT_PREFIX + presetId, 0) + 1
        prefs.edit()
            .putInt(KEY_COUNT_PREFIX + presetId, next)
            .putInt(KEY_TOTAL_PREFIX + presetId, prefs.getInt(KEY_TOTAL_PREFIX + presetId, 0) + 1)
            .apply()
        return next
    }

    fun resetSession(context: Context, presetId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_COUNT_PREFIX + presetId, 0)
            .apply()
    }
}
