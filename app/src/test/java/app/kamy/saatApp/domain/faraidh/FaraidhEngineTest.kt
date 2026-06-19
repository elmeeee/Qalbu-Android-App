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
        assertTrue("Mother should inherit", motherShare != null)
        assertTrue("Maternal siblings should inherit", maternalShare != null)
        assertEquals(BigDecimal("8000.00"), motherShare!!.cashAmount)
        assertEquals(BigDecimal("16000.00"), maternalShare!!.cashAmount)
        assertEquals(2, maternalShare.headCount)
    }

    @Test
    fun testDisqualifiedHeirExclusion() {
        val profile = DeceasedProfile(
            gender = DeceasedGender.MALE,
            netEstate = BigDecimal("30000.00")
        )
        // 1 Wife (qualified), 1 Son (non-Muslim, disqualified), 1 Full Brother (qualified)
        val input = HeirInput(
            wifeCount = 1,
            fullBrotherCount = 1,
            disqualifiedHeirs = listOf(
                DisqualifiedHeir(HeirType.SON, 1, BlockingReasonKey.DIFFERENCE_OF_RELIGION)
            )
        )
        val result = FaraidhEngine.calculate(profile, input)

        // The non-Muslim son must be excluded and get 0 share
        val sonShare = result.activeShares.find { it.type == HeirType.SON }
        assertTrue("Son should not inherit", sonShare == null)

        val sonBlocked = result.blockedHeirs.find { it.type == HeirType.SON }
        assertTrue("Son should be blocked", sonBlocked != null)
        assertEquals(BlockingReasonKey.DIFFERENCE_OF_RELIGION, sonBlocked!!.reason)

        // Since the son is disqualified, he is treated as non-existent for blocking.
        // Therefore, Wife gets 1/4 (instead of 1/8) = 7500.00
        val wifeShare = result.activeShares.find { it.type == HeirType.WIFE }
        assertTrue("Wife should inherit", wifeShare != null)
        assertEquals(BigDecimal("7500.00"), wifeShare!!.cashAmount)

        // Full Brother inherits the remaining 3/4 as Asabah = 22500.00
        val brotherShare = result.activeShares.find { it.type == HeirType.FULL_BROTHER }
        assertTrue("Brother should inherit", brotherShare != null)
        assertEquals(BigDecimal("22500.00"), brotherShare!!.cashAmount)
    }

    @Test
    fun testPregnancyReserveSimulation() {
        val profile = DeceasedProfile(
            gender = DeceasedGender.MALE,
            netEstate = BigDecimal("24000.00")
        )
        // Deceased has Wife, Mother, Father, and an unborn child (1 fetus)
        val baseInput = HeirInput(
            wifeCount = 1,
            motherCount = 1,
            fatherCount = 1
        )
        val contingency = ContingencyEngine.calculatePregnancyReserve(profile, baseInput, numberOfFetuses = 1)

        // Let's verify active distributions:
        // Scenario A (no birth): Wife (1/4 = 6000), Mother (1/3 = 8000), Father (1/3 + Asabah = 10000)
        // Scenario B (male birth): Wife (1/8 = 3000), Mother (1/6 = 4000), Father (1/6 = 4000)
        // Scenario C (female birth): Wife (1/8 = 3000), Mother (1/6 = 4000), Father (1/6 = 4000)
        // Guaranteed minimums: Wife (3000), Mother (4000), Father (4000)
        // Total distributed: 11000
        // Frozen reserve: 24000 - 11000 = 13000
        assertEquals(BigDecimal("3000.00"), contingency.activeDistribution["wife"])
        assertEquals(BigDecimal("4000.00"), contingency.activeDistribution["mother"])
        assertEquals(BigDecimal("4000.00"), contingency.activeDistribution["father"])
        assertEquals(BigDecimal("13000.00"), contingency.frozenReserve)
    }

    @Test
    fun testMunasakhatChainedCalculation() {
        // Original Deceased has estate 24000
        // Heirs: Mother (gets 1/3 = 8000), Full Brother (gets 2/3 = 16000)
        val profile = DeceasedProfile(
            gender = DeceasedGender.MALE,
            netEstate = BigDecimal("24000.00")
        )
        val baseInput = HeirInput(
            motherCount = 1,
            fullBrotherCount = 1
        )
        val primaryResult = FaraidhEngine.calculate(profile, baseInput)

        // Full Brother dies before distribution, leaving his Mother and a Son.
        // His personal estate is 4000. So his total estate is 16000 (inherited) + 4000 = 20000.
        // Sub-heirs of Brother: Mother (gets 1/6 of 20000 = 3333.33), Son (gets 5/6 as Asabah = 16666.67)
        val brotherSubTree = MunasakhatNode(
            deceasedId = "full_brother",
            deceasedName = "Ahmad",
            netPersonalEstate = BigDecimal("4000.00"),
            input = HeirInput(
                motherCount = 1,
                sonCount = 1
            )
        )

        val collapsed = MunasakhatEngine.calculate(
            primaryResult,
            mapOf("full_brother" to brotherSubTree)
        )

        // Combined Mother share: Mother of primary deceased (8000) + Mother of Brother (3333.33) = 11333.33.
        assertEquals(BigDecimal("11333.33"), collapsed["mother"])
        assertEquals(BigDecimal("16666.67"), collapsed["son"])
    }

    @Test
    fun testBaitulMalFallback() {
        val profile = DeceasedProfile(
            gender = DeceasedGender.MALE,
            netEstate = BigDecimal("15000.00")
        )
        val input = HeirInput() // No heirs entered at all
        val result = FaraidhEngine.calculate(profile, input)

        assertEquals(1, result.activeShares.size)
        val fallbackShare = result.activeShares.first()
        assertEquals("baitul_mal", fallbackShare.heirId)
        assertEquals(BigDecimal("15000.00"), fallbackShare.cashAmount)
        assertEquals(FaraidhFraction.ONE, fallbackShare.fraction)
    }

    /**
     * Two daughters, no son.
     * Islamic ruling: daughters get 2/3 as furud (Quran 4:11).
     * No son present → daughters do NOT become asabah.
     * No other asabah → radd distributes the remaining 1/3 back proportionally → daughters receive 3/3 = all.
     */
    @Test
    fun test_twoDaughtersOnly_getRaddNotAsabah() {
        val profile = DeceasedProfile(
            gender = DeceasedGender.MALE,
            netEstate = BigDecimal("12000.00")
        )
        val input = HeirInput(daughterCount = 2)
        val result = FaraidhEngine.calculate(profile, input)

        val daughterShare = result.activeShares.find { it.type == HeirType.DAUGHTER }
        assertTrue("Daughters should inherit", daughterShare != null)
        // After radd: daughters get the full estate
        assertEquals(BigDecimal("12000.00"), daughterShare!!.cashAmount)
        // Must NOT be marked as asabah — they received furud + radd only
        assertTrue("Daughters should NOT be asabah when alone", !daughterShare.isAsabah)
        // No second daughter entry with isAsabah=true
        val asabahDaughterEntry = result.activeShares.filter { it.type == HeirType.DAUGHTER && it.isAsabah }
        assertTrue("No asabah daughter slot should exist", asabahDaughterEntry.isEmpty())
    }

    /**
     * Father + two daughters (no son).
     * Islamic ruling:
     *   - Daughters: 2/3 furud
     *   - Father: 1/6 fixed + residue (1/6) as asabah = 1/3 total
     */
    @Test
    fun test_fatherAndTwoDaughters_fatherTakesResidueAsAsabah() {
        val profile = DeceasedProfile(
            gender = DeceasedGender.MALE,
            netEstate = BigDecimal("12000.00")
        )
        val input = HeirInput(fatherCount = 1, daughterCount = 2)
        val result = FaraidhEngine.calculate(profile, input)

        val daughterShare = result.activeShares.find { it.type == HeirType.DAUGHTER }
        assertTrue("Daughters should inherit", daughterShare != null)
        assertEquals(BigDecimal("8000.00"), daughterShare!!.cashAmount)  // 2/3 of 12000

        // Father: 1/6 (fixed) + 1/6 (residue asabah) = 2/6 = 1/3 = 4000
        val fatherShares = result.activeShares.filter { it.type == HeirType.FATHER }
        val fatherTotal = fatherShares.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.cashAmount) }
        assertEquals(BigDecimal("4000.00"), fatherTotal)  // 1/3 of 12000

        // No asabah daughter slot
        val asabahDaughter = result.activeShares.filter { it.type == HeirType.DAUGHTER && it.isAsabah }
        assertTrue("Daughters must not appear as asabah", asabahDaughter.isEmpty())
    }
}
