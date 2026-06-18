package app.kamy.saatApp.core.locale

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Resolves strings using the current app language (not the stale Application locale). */
@Singleton
class AppStrings @Inject constructor(
    @ApplicationContext private val baseContext: Context,
    private val languageStore: AppLanguageStore
) {
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
        localizedContext().getString(resId, *formatArgs)

    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String =
        localizedContext().resources.getQuantityString(resId, quantity, *formatArgs)

    fun localizedContext(): Context =
        AppLocale.wrap(baseContext, languageStore.current())
}
