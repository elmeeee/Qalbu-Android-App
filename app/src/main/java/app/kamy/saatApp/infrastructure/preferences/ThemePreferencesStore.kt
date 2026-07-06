package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class AppThemeColor(val key: String, val displayNameRes: Int) {
    EMERALD("emerald", app.kamy.saatApp.R.string.theme_emerald),
    OCEAN("ocean", app.kamy.saatApp.R.string.theme_ocean),
    GOLD("gold", app.kamy.saatApp.R.string.theme_gold),
    DARK("dark", app.kamy.saatApp.R.string.theme_dark),
    ROSE("rose", app.kamy.saatApp.R.string.theme_rose),
    PURPLE("purple", app.kamy.saatApp.R.string.theme_purple),
    ORANGE("orange", app.kamy.saatApp.R.string.theme_orange),
    RED("red", app.kamy.saatApp.R.string.theme_red),
    CUSTOM("custom", app.kamy.saatApp.R.string.theme_custom);

    companion object {
        fun fromKey(key: String?): AppThemeColor {
            return values().firstOrNull { it.key == key } ?: EMERALD
        }
    }
}

@Singleton
class ThemePreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(currentTheme())
    val themeFlow: StateFlow<AppThemeColor> = _themeFlow.asStateFlow()

    fun currentTheme(): AppThemeColor {
        val key = prefs.getString(KEY_THEME, AppThemeColor.EMERALD.key)
        return AppThemeColor.fromKey(key)
    }

    fun setTheme(theme: AppThemeColor) {
        prefs.edit().putString(KEY_THEME, theme.key).apply()
        _themeFlow.value = theme
    }

    fun customColorHex(): String {
        return prefs.getString(KEY_CUSTOM_COLOR, "#0F4C3A") ?: "#0F4C3A"
    }

    fun setCustomColorHex(hex: String) {
        prefs.edit().putString(KEY_CUSTOM_COLOR, hex).apply()
        _themeFlow.value = currentTheme() // Trigger update flow
    }

    companion object {
        private const val PREFS_NAME = "saat_app_theme"
        private const val KEY_THEME = "app_theme_color"
        private const val KEY_CUSTOM_COLOR = "custom_theme_color_hex"

        fun from(context: Context): ThemePreferencesStore = ThemePreferencesStore(context.applicationContext)
    }
}
