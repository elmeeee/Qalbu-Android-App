package app.kamy.saatApp.infrastructure.defaults

import app.kamy.saatApp.core.locale.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceLanguageDetectorTest {

    @Test
    fun `id tag maps to INDONESIAN`() {
        assertEquals(AppLanguage.INDONESIAN, DeviceLanguageDetector.fromLanguageTag("id"))
    }

    @Test
    fun `ms tag maps to MALAY`() {
        assertEquals(AppLanguage.MALAY, DeviceLanguageDetector.fromLanguageTag("ms"))
    }

    @Test
    fun `en tag maps to ENGLISH`() {
        assertEquals(AppLanguage.ENGLISH, DeviceLanguageDetector.fromLanguageTag("en"))
    }

    @Test
    fun `Unknown tag falls back to ENGLISH`() {
        assertEquals(AppLanguage.ENGLISH, DeviceLanguageDetector.fromLanguageTag("fr"))
    }

    @Test
    fun `Null tag falls back to ENGLISH`() {
        assertEquals(AppLanguage.ENGLISH, DeviceLanguageDetector.fromLanguageTag(null))
    }

    @Test
    fun `Uppercase tag is handled case-insensitively`() {
        assertEquals(AppLanguage.INDONESIAN, DeviceLanguageDetector.fromLanguageTag("ID"))
        assertEquals(AppLanguage.MALAY, DeviceLanguageDetector.fromLanguageTag("MS"))
    }
}
