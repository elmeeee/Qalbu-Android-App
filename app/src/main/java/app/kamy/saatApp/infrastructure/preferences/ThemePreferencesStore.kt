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
    INDIGO("indigo", app.kamy.saatApp.R.string.theme_indigo),
    GOLD("gold", app.kamy.saatApp.R.string.theme_gold);

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

    companion object {
        private const val PREFS_NAME = "saat_app_theme"
        private const val KEY_THEME = "app_theme_color"

        fun from(context: Context): ThemePreferencesStore = ThemePreferencesStore(context.applicationContext)
    }
}
