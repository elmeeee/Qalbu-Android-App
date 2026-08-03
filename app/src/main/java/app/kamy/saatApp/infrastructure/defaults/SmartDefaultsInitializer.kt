package app.kamy.saatApp.infrastructure.defaults

import android.content.Context
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

        // 2. Auto-default translation + prayer method from SIM/network country.
        val defaults = CountryDefaultsProvider.detect(appContext)
        TranslationPreferencesStore(appContext).setTranslation(
            defaults.translationId,
            defaults.translationName
        )
        PrayerCalculationStore(appContext).setMethod(defaults.prayerMethod)

        onboardingStore.markFirstLaunchDefaultApplied()
    }
}
