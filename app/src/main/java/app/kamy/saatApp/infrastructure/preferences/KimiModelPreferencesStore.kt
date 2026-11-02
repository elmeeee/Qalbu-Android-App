package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.model.KimiAiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KimiModelPreferencesStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _selectedModel = MutableStateFlow(
        KimiAiModel.fromApiId(preferences.getString(KEY_MODEL, null))
    )
    val selectedModel: StateFlow<KimiAiModel> = _selectedModel.asStateFlow()

    fun setModel(model: KimiAiModel) {
        preferences.edit().putString(KEY_MODEL, model.apiId).apply()
        _selectedModel.value = model
    }

    companion object {
        private const val PREFERENCES_NAME = "kimi_preferences"
        private const val KEY_MODEL = "selected_model"
    }
}
