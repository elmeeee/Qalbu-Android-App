package app.kamy.saatApp.infrastructure.defaults

import android.content.Context
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore

/**
 * Applies smart defaults (language, Quran translation, prayer calculation method)
 * on first launch only. Subsequent launches and manual user overrides are never
 * touched because [OnboardingStore.isFirstLaunchDefaultApplied] guards the one-shot.
 *
 * Call from [app.kamy.saatApp.SaatApplication.onCreate].
 */
object SmartDefaultsInitializer {

    fun applyIfNeeded(context: Context) {
        val appContext = context.applicationContext
        val onboardingStore = OnboardingStore.from(appContext)
        if (onboardingStore.isFirstLaunchDefaultApplied()) return

        // 1. Auto-detect app language from device locale.
        val language = DeviceLanguageDetector.detect()
        AppLanguageStore.from(appContext).set(language)

        // 2. Translation default follows the app language, not country, so a
        //    Indonesian-languaged device always picks Kemenag even without a SIM.
        val translation = LocalQuranConfig.translationForAppLanguage(language)
        val translationName = LocalQuranConfig.translationDisplayLabel(translation)
        TranslationPreferencesStore(appContext).setTranslation(
            translation.id,
            translationName
        )

        // 3. Prayer method still comes from country, fallback to device locale.
        val defaults = CountryDefaultsProvider.detect(appContext)
        PrayerCalculationStore(appContext).setMethod(defaults.prayerMethod)

        onboardingStore.markFirstLaunchDefaultApplied()
    }
}
