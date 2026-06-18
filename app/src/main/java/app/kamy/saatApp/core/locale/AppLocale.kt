package app.kamy.saatApp.core.locale

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLocale {

    fun wrap(context: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
