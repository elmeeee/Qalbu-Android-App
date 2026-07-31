package app.kamy.saatApp.features.tools

import app.kamy.saatApp.domain.tools.ZakatCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZakatCalculatorTest {

    @Test
    fun calculate_belowNisab_returnsNoZakat() {
        val result = ZakatCalculator.calculate(
            cash = 1_000_000.0,
            goldGrams = 0.0,
            silverGrams = 0.0,
            investments = 0.0,
            debts = 0.0,
            goldPricePerGram = 1_200_000.0,
            silverPricePerGram = 15_000.0
        )
        assertFalse(result.meetsNisab)
        assertEquals(0.0, result.zakatDue, 0.01)
    }

    @Test
    fun calculate_aboveNisab_returnsCorrectZakatDue() {
        val result = ZakatCalculator.calculate(
            cash = 200_000_000.0,
            goldGrams = 10.0,
            silverGrams = 0.0,
            investments = 0.0,
            debts = 0.0,
            goldPricePerGram = 1_200_000.0,
            silverPricePerGram = 15_000.0
        )
        assertTrue(result.meetsNisab)
        // 200m + 12m = 212,000,000 * 2.5% = 5,300,000
        assertEquals(5_300_000.0, result.zakatDue, 100.0)
    }

    @Test
    fun calculateFitrah_returnsCorrectTotal() {
        val result = ZakatCalculator.calculateFitrah(
            familyMembers = 4,
            staplePricePerKg = 15_000.0
        )
        assertEquals(10.0, result.totalStapleKilograms, 0.01)
        assertEquals(150_000.0, result.zakatDue, 0.01)
    }
}
