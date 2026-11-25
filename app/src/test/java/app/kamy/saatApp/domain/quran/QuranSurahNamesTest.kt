package app.kamy.saatApp.domain.quran

import app.kamy.saatApp.core.locale.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class QuranSurahNamesTest {

    @Test
    fun allLanguagesHaveAll114Surahs() {
        assertEquals(114, QuranSurahNames.allNames(AppLanguage.INDONESIAN).size)
        assertEquals(114, QuranSurahNames.allNames(AppLanguage.MALAY).size)
        assertEquals(114, QuranSurahNames.allNames(AppLanguage.ENGLISH).size)
    }

    @Test
    fun surah94UsesKemenagInIndonesianMalayAndInternationalInEnglish() {
        assertEquals("Al-Insyirah", QuranSurahNames.localizedName(94, AppLanguage.INDONESIAN, "fallback"))
        assertEquals("Al-Insyirah", QuranSurahNames.localizedName(94, AppLanguage.MALAY, "fallback"))
        assertEquals("Ash-Sharh", QuranSurahNames.localizedName(94, AppLanguage.ENGLISH, "fallback"))
    }

    @Test
    fun otherKeySurahsFollowLanguageConventions() {
        assertEquals("Al-Kautsar", QuranSurahNames.localizedName(108, AppLanguage.INDONESIAN, "fallback"))
        assertEquals("Al-Kawthar", QuranSurahNames.localizedName(108, AppLanguage.ENGLISH, "fallback"))
        assertEquals("Al-Ikhlash", QuranSurahNames.localizedName(112, AppLanguage.INDONESIAN, "fallback"))
        assertEquals("Al-Ikhlas", QuranSurahNames.localizedName(112, AppLanguage.ENGLISH, "fallback"))
    }
}
