package app.kamy.saatApp.domain.prayer

import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.repository.RamadanQuoteRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RamadanDetailFeatureTest {

    private val sampleJson = """
    [
      {
        "day": 1,
        "source_type": "QURAN",
        "reference": {
          "en": "Al-Baqarah 2:184",
          "id": "Al-Baqarah 2:184",
          "ms": "Al-Baqarah 2:184"
        },
        "quote": {
          "en": "And fast, it is better for you if you only knew.",
          "id": "Dan berpuasa itu lebih baik bagimu jika kamu mengetahui.",
          "ms": "Dan berpuasa itu lebih baik bagi kamu sekiranya kamu mengetahui."
        }
      },
      {
        "day": 2,
        "source_type": "HADITH",
        "reference": {
          "en": "HR. Bukhari & Muslim",
          "id": "HR. Bukhari & Muslim",
          "ms": "HR. Bukhari & Muslim"
        },
        "quote": {
          "en": "Whoever fasts Ramadan out of faith and hope for reward, all their past sins will be forgiven.",
          "id": "Barangsiapa berpuasa Ramadan karena iman dan mengharap pahala, diampuni dosa-dosanya yang telah lalu.",
          "ms": "Sesiapa yang berpuasa Ramadan kerana iman dan mengharapkan pahala, diampuni dosa-dosanya yang telah lalu."
        }
      }
    ]
    """.trimIndent()

    @Test
    fun testRamadanQuotesParsingInThreeLanguages() {
        val rawQuotes = RamadanQuoteRepository.parseQuotesJson(sampleJson)
        assertEquals(2, rawQuotes.size)

        val quoteDay1Id = RamadanQuoteRepository.resolveQuote(rawQuotes, 1, AppLanguage.INDONESIAN)
        assertEquals(1, quoteDay1Id.day)
        assertEquals("Al-Baqarah 2:184", quoteDay1Id.reference)
        assertEquals("Dan berpuasa itu lebih baik bagimu jika kamu mengetahui.", quoteDay1Id.quote)

        val quoteDay1En = RamadanQuoteRepository.resolveQuote(rawQuotes, 1, AppLanguage.ENGLISH)
        assertEquals("And fast, it is better for you if you only knew.", quoteDay1En.quote)

        val quoteDay1Ms = RamadanQuoteRepository.resolveQuote(rawQuotes, 1, AppLanguage.MALAY)
        assertEquals("Dan berpuasa itu lebih baik bagi kamu sekiranya kamu mengetahui.", quoteDay1Ms.quote)

        val quoteDay2Id = RamadanQuoteRepository.resolveQuote(rawQuotes, 2, AppLanguage.INDONESIAN)
        assertEquals("HADITH", quoteDay2Id.sourceType)
        assertTrue(quoteDay2Id.quote.contains("Barangsiapa berpuasa"))
    }
}
