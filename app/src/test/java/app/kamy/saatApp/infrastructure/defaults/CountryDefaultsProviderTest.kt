package app.kamy.saatApp.infrastructure.defaults

import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import org.junit.Assert.assertEquals
import org.junit.Test

class CountryDefaultsProviderTest {

    @Test
    fun `Indonesia maps to Kemenag translation and KEMENAG method`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode("ID")
        assertEquals(LocalQuranConfig.TRANSLATION_INDONESIAN, defaults.translationId)
        assertEquals(PrayerCalculationMethod.KEMENAG, defaults.prayerMethod)
    }

    @Test
    fun `Malaysia maps to DBP translation and JAKIM method`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode("MY")
        assertEquals(LocalQuranConfig.TRANSLATION_MALAY, defaults.translationId)
        assertEquals(PrayerCalculationMethod.JAKIM, defaults.prayerMethod)
    }

    @Test
    fun `Singapore maps to Sahih International translation and MUIS method`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode("SG")
        assertEquals(LocalQuranConfig.TRANSLATION_ENGLISH, defaults.translationId)
        assertEquals(PrayerCalculationMethod.MUIS, defaults.prayerMethod)
    }

    @Test
    fun `Brunei maps to Sahih International translation and BRUNEI method`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode("BN")
        assertEquals(LocalQuranConfig.TRANSLATION_ENGLISH, defaults.translationId)
        assertEquals(PrayerCalculationMethod.BRUNEI, defaults.prayerMethod)
    }

    @Test
    fun `Unknown country falls back to Sahih International and MWL`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode("XX")
        assertEquals(LocalQuranConfig.TRANSLATION_ENGLISH, defaults.translationId)
        assertEquals(PrayerCalculationMethod.MWL, defaults.prayerMethod)
    }

    @Test
    fun `Null country code falls back to Sahih International and MWL`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode(null)
        assertEquals(LocalQuranConfig.TRANSLATION_ENGLISH, defaults.translationId)
        assertEquals(PrayerCalculationMethod.MWL, defaults.prayerMethod)
    }

    @Test
    fun `Lowercase country code is normalized to uppercase`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode("id")
        assertEquals(LocalQuranConfig.TRANSLATION_INDONESIAN, defaults.translationId)
        assertEquals(PrayerCalculationMethod.KEMENAG, defaults.prayerMethod)
    }

    @Test
    fun `Translation name for Indonesia is Kementerian Agama RI`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode("ID")
        assertEquals("Kementerian Agama RI", defaults.translationName)
    }

    @Test
    fun `Translation name for Malaysia is DBP`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode("MY")
        assertEquals("DBP", defaults.translationName)
    }

    @Test
    fun `Translation name for fallback is Sahih International`() {
        val defaults = CountryDefaultsProvider.defaultsForCountryCode(null)
        assertEquals("Sahih International", defaults.translationName)
    }
}
