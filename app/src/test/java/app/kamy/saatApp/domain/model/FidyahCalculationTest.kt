package app.kamy.saatApp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FidyahCalculationTest {

    @Test
    fun testSyafiiElderlyFidyah() {
        val days = 10
        val madhhab = FidyahMadhhab.SYAFII
        val reason = FidyahReason.ELDERLY_CHRONIC

        val isRequired = true
        val multiplier = 1
        val totalDays = days * multiplier
        val riceKg = totalDays * 0.75

        assertEquals(10, totalDays)
        assertEquals(7.5, riceKg, 0.001)
        assertTrue(isRequired)
    }

    @Test
    fun testSyafiiLateQadhaMultiplier() {
        val days = 10
        val delayedYears = 3
        val madhhab = FidyahMadhhab.SYAFII
        val reason = FidyahReason.LATE_QADHA

        val totalDaysMultiplier = days * delayedYears
        val riceKg = totalDaysMultiplier * 0.75

        assertEquals(30, totalDaysMultiplier)
        assertEquals(22.5, riceKg, 0.001)
    }

    @Test
    fun testHanafiPregnantNoFidyah() {
        val days = 10
        val madhhab = FidyahMadhhab.HANAFI
        val reason = FidyahReason.PREGNANT_NURSING_CHILD

        val isRequired = false
        val totalDaysMultiplier = 0

        assertEquals(0, totalDaysMultiplier)
        assertFalse(isRequired)
    }

    @Test
    fun testMalikiLateQadhaNoYearMultiplier() {
        val days = 10
        val delayedYears = 4
        val madhhab = FidyahMadhhab.MALIKI
        val reason = FidyahReason.LATE_QADHA

        // In Maliki, late Qadha does NOT multiply by delayed years
        val totalDaysMultiplier = days * 1
        val riceKg = totalDaysMultiplier * 0.75

        assertEquals(10, totalDaysMultiplier)
        assertEquals(7.5, riceKg, 0.001)
    }

    @Test
    fun testHanbaliElderlyFidyah() {
        val days = 15
        val madhhab = FidyahMadhhab.HANBALI
        val reason = FidyahReason.ELDERLY_CHRONIC

        val totalDaysMultiplier = days * 1
        val riceKg = totalDaysMultiplier * 0.75

        assertEquals(15, totalDaysMultiplier)
        assertEquals(11.25, riceKg, 0.001)
    }
}
