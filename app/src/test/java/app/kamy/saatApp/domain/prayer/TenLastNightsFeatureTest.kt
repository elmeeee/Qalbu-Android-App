package app.kamy.saatApp.domain.prayer

import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.repository.TenLastNightsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TenLastNightsFeatureTest {

    private val quotesJsonContent: String by lazy {
        File("src/main/assets/ten_last_nights_quotes.json").readText()
    }

    private val guideJsonContent: String by lazy {
        File("src/main/assets/lailatul_qadr_guide.json").readText()
    }

    private val duasJsonContent: String by lazy {
        File("src/main/assets/ten_last_nights_duas.json").readText()
    }

    @Test
    fun testTenLastNightsQuotesParsingAndLocalization() {
        val rawQuotes = TenLastNightsRepository.parseQuotesJson(quotesJsonContent)
        assertEquals("Should contain 10 quotes for nights 21 to 30", 10, rawQuotes.size)

        // Test night 21 Indonesian
        val night21Id = TenLastNightsRepository.resolveQuote(rawQuotes, 21, AppLanguage.INDONESIAN)
        assertEquals(21, night21Id.night)
        assertTrue(night21Id.quote.isNotEmpty())
        assertTrue(night21Id.description.isNotEmpty())

        // Test night 27 English
        val night27En = TenLastNightsRepository.resolveQuote(rawQuotes, 27, AppLanguage.ENGLISH)
        assertEquals(27, night27En.night)
        assertTrue(night27En.quote.contains("odd night", ignoreCase = true) || night27En.quote.isNotEmpty())

        // Test night 27 Malay
        val night27Ms = TenLastNightsRepository.resolveQuote(rawQuotes, 27, AppLanguage.MALAY)
        assertEquals(27, night27Ms.night)
        assertTrue(night27Ms.quote.isNotEmpty())
    }

    @Test
    fun testLailatulQadrGuideParsingAndContent() {
        val rawGuide = TenLastNightsRepository.parseGuideJson(guideJsonContent)
        assertNotNull(rawGuide)
        assertTrue(rawGuide.sections.isNotEmpty())

        val resolvedGuideId = TenLastNightsRepository.resolveGuide(rawGuide, AppLanguage.INDONESIAN)
        assertEquals("Tentang Lailatul Qadr", resolvedGuideId.title)
        assertTrue(resolvedGuideId.sections.size >= 5)

        val meaningSection = resolvedGuideId.sections.first { it.id == "meaning" }
        assertNotNull("Should contain Quran reference in meaning section", meaningSection.quranReference)
        assertEquals("QS. Al-Qadr (97:1)", meaningSection.quranReference?.reference)

        val resolvedGuideEn = TenLastNightsRepository.resolveGuide(rawGuide, AppLanguage.ENGLISH)
        assertEquals("About Lailatul Qadr", resolvedGuideEn.title)
        val meaningSectionEn = resolvedGuideEn.sections.first { it.id == "meaning" }
        assertTrue(meaningSectionEn.content.isNotEmpty())
    }

    @Test
    fun testTenLastNightsDuasParsingAndContent() {
        val rawDuas = TenLastNightsRepository.parseDuasJson(duasJsonContent)
        assertTrue("Should contain at least 4 authentic duas", rawDuas.size >= 4)

        val resolvedDuasId = TenLastNightsRepository.resolveDuas(rawDuas, AppLanguage.INDONESIAN)
        val mainDua = resolvedDuasId.first { it.id == "dua_lailatul_qadr" }
        assertTrue(mainDua.reference.contains("3513"))
        assertTrue(mainDua.arabic.contains("عَفُوٌّ"))
        assertTrue(mainDua.transliteration.contains("Allahumma innaka 'afuwwun"))
        assertTrue(mainDua.translation.contains("Maha Pemaaf"))

        val resolvedDuasEn = TenLastNightsRepository.resolveDuas(rawDuas, AppLanguage.ENGLISH)
        val mainDuaEn = resolvedDuasEn.first { it.id == "dua_lailatul_qadr" }
        assertTrue(mainDuaEn.translation.contains("Forgiving", ignoreCase = true) || mainDuaEn.translation.contains("Pardoning", ignoreCase = true))
    }
}
