package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerCalculationStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _method = MutableStateFlow(load())
    val method: StateFlow<PrayerCalculationMethod> = _method.asStateFlow()

    val hasSavedPreference: Boolean
        get() = prefs.contains(KEY)

    fun current(): PrayerCalculationMethod = _method.value

    fun setMethod(method: PrayerCalculationMethod) {
        prefs.edit().putString(KEY, method.rawValue).apply()
        _method.value = method
    }

    private fun load(): PrayerCalculationMethod =
        PrayerCalculationMethod.fromRawValue(prefs.getString(KEY, null))

    companion object {
        private const val PREFS_NAME = "saat_prefs"
        private const val KEY = "prayer_calculation_method"
    }
}
