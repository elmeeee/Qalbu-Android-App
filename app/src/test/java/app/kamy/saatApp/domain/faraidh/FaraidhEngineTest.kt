package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FaraidhEngineTest {

    @Test
    fun testStandardCalculation() {
        val profile = DeceasedProfile(
            gender = DeceasedGender.MALE,
            netEstate = BigDecimal("24000.00"),
            bornOutOfWedlock = false
        )
        val input = HeirInput(
            wifeCount = 1,
            motherCount = 1,
            fatherCount = 1,
            sonCount = 1,
            daughterCount = 1
        )
        val result = FaraidhEngine.calculate(profile, input)

        // Wife should get 1/8 of 24000 = 3000
        val wifeShare = result.activeShares.find { it.type == HeirType.WIFE }
        assertTrue("Wife should inherit", wifeShare != null)
        assertEquals(BigDecimal("3000.00"), wifeShare!!.cashAmount)

        // Mother should get 1/6 of 24000 = 4000
        val motherShare = result.activeShares.find { it.type == HeirType.MOTHER }
        assertTrue("Mother should inherit", motherShare != null)
        assertEquals(BigDecimal("4000.00"), motherShare!!.cashAmount)

        // Father should get 1/6 of 24000 = 4000
        val fatherShare = result.activeShares.find { it.type == HeirType.FATHER }
        assertTrue("Father should inherit", fatherShare != null)
        assertEquals(BigDecimal("4000.00"), fatherShare!!.cashAmount)

        // Remaining Asabah = 24000 - 3000 - 4000 - 4000 = 13000
        // Son and Daughter share Asabah in 2:1 ratio.
        // Son gets 2/3 of 13000 = 8666.67
        // Daughter gets 1/3 of 13000 = 4333.33
        val sonShare = result.activeShares.find { it.type == HeirType.SON }
        val daughterShare = result.activeShares.find { it.type == HeirType.DAUGHTER }
        assertTrue("Son should inherit", sonShare != null)
        assertTrue("Daughter should inherit", daughterShare != null)

        assertEquals(BigDecimal("8666.67"), sonShare!!.cashAmount)
        assertEquals(BigDecimal("4333.33"), daughterShare!!.cashAmount)
    }

    @Test
    fun testBornOutOfWedlockCalculation() {
        val profile = DeceasedProfile(
            gender = DeceasedGender.MALE,
            netEstate = BigDecimal("24000.00"),
            bornOutOfWedlock = true
        )
        val input = HeirInput(
            fatherCount = 1,
            motherCount = 1,
            fullBrotherCount = 1,
            fullSisterCount = 1
        )
        val result = FaraidhEngine.calculate(profile, input)

        // Father must be blocked and not inherit
        val fatherShare = result.activeShares.find { it.type == HeirType.FATHER }
        assertTrue("Father should not inherit", fatherShare == null)

        val fatherBlocked = result.blockedHeirs.find { it.type == HeirType.FATHER }
        assertTrue("Father should be blocked", fatherBlocked != null)
        assertEquals(BlockingReasonKey.OUT_OF_WEDLOCK, fatherBlocked!!.reason)

        // Full Brother and Full Sister are treated as Maternal Siblings (since born out of wedlock)
        // Maternal siblings: Full Brother (1) + Full Sister (1) = 2 heads
        // Mother: 1
        // Mother gets 1/6 (because there are 2 siblings)
        // Maternal Siblings get 1/3 collectively
        // Sum = 1/6 + 1/3 = 1/2
        // Since no Asabah exists, Radd is applied.
        // Scaled Mother: 1/6 / (1/2) = 1/3 of 24000 = 8000
        // Scaled Maternal Siblings: 1/3 / (1/2) = 2/3 of 24000 = 16000
        // 2 Maternal Siblings share 16000 equally -> 8000 each.
        val motherShare = result.activeShares.find { it.type == HeirType.MOTHER }
        val maternalShare = result.activeShares.find { it.type == HeirType.MATERNAL_SIBLING }

        println("Active Shares: " + result.activeShares.map { "${it.type} count=${it.headCount} fraction=${it.fraction} cash=${it.cashAmount}" })
        println("Blocked Heirs: " + result.blockedHeirs.map { "${it.type} reason=${it.reason}" })

        assertTrue("Mother should inherit", motherShare != null)
        assertTrue("Maternal siblings should inherit", maternalShare != null)

        assertEquals(BigDecimal("8000.00"), motherShare!!.cashAmount)
        assertEquals(BigDecimal("16000.00"), maternalShare!!.cashAmount)
        assertEquals(2, maternalShare.headCount)
    }
}
