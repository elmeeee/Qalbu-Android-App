package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.kamy.saatApp.infrastructure.datastore.appDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

enum class AppThemeColor(val key: String, val displayNameRes: Int) {
    EMERALD("emerald", app.kamy.saatApp.R.string.theme_emerald),
    OCEAN("ocean", app.kamy.saatApp.R.string.theme_ocean),
    GOLD("gold", app.kamy.saatApp.R.string.theme_gold),
    ROSE("rose", app.kamy.saatApp.R.string.theme_rose),
    PURPLE("purple", app.kamy.saatApp.R.string.theme_purple),
    ORANGE("orange", app.kamy.saatApp.R.string.theme_orange),
    RED("red", app.kamy.saatApp.R.string.theme_red);

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
    val themeFlow: Flow<AppThemeColor> = context.appDataStore.data.map { preferences ->
        val key = preferences[KEY_THEME_DS] ?: AppThemeColor.EMERALD.key
        AppThemeColor.fromKey(key)
    }

    suspend fun setTheme(theme: AppThemeColor) {
        context.appDataStore.edit { preferences ->
            preferences[KEY_THEME_DS] = theme.key
        }
    }

    // fallback for synchronous access if absolutely required
    fun currentTheme(): AppThemeColor {
        var theme = AppThemeColor.EMERALD
        runBlocking {
            val key = context.appDataStore.data.map { it[KEY_THEME_DS] ?: AppThemeColor.EMERALD.key }.first()
            theme = AppThemeColor.fromKey(key)
        }
        return theme
    }

    companion object {
        private val KEY_THEME_DS = stringPreferencesKey("app_theme_color")

        fun from(context: Context): ThemePreferencesStore = ThemePreferencesStore(context.applicationContext)
    }
}
