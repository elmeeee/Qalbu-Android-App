package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Offline Islamic inheritance (faraidh) engine.
 * Implements blocking (hajb), fixed shares (ashab al-furud), residue (asabah),
 * deficit/surplus adjustments (awl / radd), grandfather (jedd) rules,
 * Al-Akdariyah special redistribution, Al-Marwaniyah madhhab split,
 * and classical case detection — all using [BigDecimal] / [FaraidhFraction].
 */
object FaraidhEngine {

    private data class Slot(
        val type: HeirType,
        val heads: Int,
        var fraction: FaraidhFraction,
        val isAsabah: Boolean,
        val proofKeys: MutableList<String> = mutableListOf()
    )

    fun calculate(
        profile: DeceasedProfile,
        input: HeirInput,
        names: FaraidhParticipantNames = FaraidhParticipantNames(),
        madhhab: FaraidhMadhhab = profile.madhhab
    ): FaraidhResult {
        val estate = profile.netEstate.max(BigDecimal.ZERO)
        val blocked = mutableListOf<BlockedHeir>()

        for (dq in input.disqualifiedHeirs) {
            blocked.add(BlockedHeir(dq.type, dq.count, dq.reason))
        }

        if (!input.hasAnyHeir()) {
            val fallback = DzawilArhamResolver.resolve(estate, input)
            val graph = FaraidhGraphBuilder.build(profile, input, fallback.activeShares, blocked)
            return FaraidhResult(
                deceased = profile,
                input = input,
                activeShares = fallback.activeShares,
                blockedHeirs = blocked,
                silsilah = emptyList(),
                adjustment = FaraidhAdjustment.NONE,
                adjustmentNoteKey = fallback.noteKey,
                proofKeys = fallback.proofKeys,
                totalDistributed = estate,
                remainderFraction = FaraidhFraction.ZERO,
                madhhab = madhhab,
                madhhabNoteKey = madhhabNoteKey(madhhab),
                familyGraph = graph
            )
        }

        val ctx = analyze(input, profile.gender, profile.bornOutOfWedlock)

        resolveBlocking(input, ctx, blocked, profile.bornOutOfWedlock)
        val slots = mutableListOf<Slot>()

        // ── Al-Akdariyah special path ─────────────────────────────────────────
        // Husband + Mother + Grandfather + Full Sister, no children, no father.
        // Special ruling: grandfather gets 1/6 fixed; full sister gets 1/6 fixed;
        // then grandfather + sister pool their combined share and redivide 2:1.
        val isAkdariyah = ctx.hasGrandfather
            && !ctx.hasFather && !ctx.hasChild && !ctx.hasGrandchild
            && input.motherCount > 0
            && (input.husbandCount > 0 || input.wifeCount > 0)
            && (input.fullSisterCount > 0 || input.fullBrotherCount == 0)
            && input.fullSisterCount > 0
            && input.fullBrotherCount == 0
            && input.paternalBrotherCount == 0 && input.paternalSisterCount == 0

        if (isAkdariyah) {
            return calculateAkdariyah(profile, input, ctx, blocked, slots, estate, madhhab, names)
        }

        assignFixedShares(input, profile, ctx, slots, blocked, madhhab)
        assignAsabahResidue(input, ctx, slots, blocked, madhhab)

        var adjustment = FaraidhAdjustment.NONE
        var adjustmentNoteKey: String? = null

        val fixedTotal = FaraidhFraction.sumOf(slots.map { it.fraction })
        if (fixedTotal.numerator > fixedTotal.denominator) {
            adjustment = FaraidhAdjustment.AWL
            adjustmentNoteKey = "faraidh_awl_note"
            val scaled = FaraidhFraction.applyAwl(slots.map { it.type to it.fraction })
            scaled.forEachIndexed { index, (_, frac) ->
                slots[index] = slots[index].copy(fraction = frac)
            }
        } else if (fixedTotal.numerator < fixedTotal.denominator && slots.none { it.isAsabah && it.fraction.numerator > BigInteger.ZERO }) {
            adjustment = FaraidhAdjustment.RADD
            adjustmentNoteKey = if (madhhab.raddIncludesSpouses()) "faraidh_radd_note_hanafi" else "faraidh_radd_note"
            val withFlag = slots.map { Triple(it.type, it.fraction, it.isAsabah) }
            val spouseTypes = if (madhhab.raddIncludesSpouses()) emptySet() else setOf(HeirType.HUSBAND, HeirType.WIFE)
            val radd = FaraidhFraction.applyRadd(withFlag, spouseTypes)
            radd.forEachIndexed { index, (_, frac) ->
                slots[index] = slots[index].copy(fraction = frac)
            }
        }

        val activeShares = slots
            .filter { it.heads > 0 && it.fraction.numerator > BigInteger.ZERO }
            .map { slot ->
                HeirShare(
                    type = slot.type,
                    headCount = slot.heads,
                    fraction = slot.fraction,
                    percentage = slot.fraction.toPercentage(scale = 1),
                    cashAmount = slot.fraction.toCashAmount(estate),
                    isAsabah = slot.isAsabah,
                    proofKeys = slot.proofKeys.distinct()
                )
            }

        val silsilah = FaraidhSilsilahBuilder.build(profile, input, activeShares, blocked, names)
        val extraProofKeys = if (profile.bornOutOfWedlock) {
            listOf("proof_out_of_wedlock", "proof_out_of_wedlock_note")
        } else {
            emptyList()
        }
        val proofKeys = activeShares.flatMap { it.proofKeys }.distinct() +
            listOfNotNull(adjustmentNoteKey?.let { "proof_awl_radd" }) + extraProofKeys

        val totalDistributed = activeShares.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.cashAmount) }
        val remainder = FaraidhFraction.ONE.add(
            FaraidhFraction.sumOf(activeShares.map { it.fraction }).let { used ->
                FaraidhFraction(used.numerator.negate(), used.denominator)
            }
        ).normalized()

        var finalShares = activeShares
        var finalAdjustmentNoteKey = adjustmentNoteKey
        var finalProofKeys = proofKeys
        var finalTotalDistributed = totalDistributed
        var finalRemainder = remainder

        if (finalShares.isEmpty()) {
            val fallback = DzawilArhamResolver.resolve(estate, input)
            finalShares = fallback.activeShares
            finalAdjustmentNoteKey = fallback.noteKey
            finalProofKeys = (finalProofKeys + fallback.proofKeys).distinct()
            finalTotalDistributed = estate
            finalRemainder = FaraidhFraction.ZERO
        }

        val graph = FaraidhGraphBuilder.build(profile, input, finalShares, blocked)
        val classicalCase = detectClassicalCase(input, ctx, madhhab)

        return FaraidhResult(
            deceased = profile,
            input = input,
            activeShares = finalShares,
            blockedHeirs = blocked,
            silsilah = silsilah,
            adjustment = adjustment,
            adjustmentNoteKey = finalAdjustmentNoteKey,
            proofKeys = finalProofKeys.distinct(),
            totalDistributed = finalTotalDistributed,
            remainderFraction = finalRemainder,
            madhhab = madhhab,
            madhhabNoteKey = madhhabNoteKey(madhhab),
            familyGraph = graph,
            classicalCase = classicalCase
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Al-Akdariyah special path
    //  Heirs: Husband ½, Mother ⅓, Grandfather 1/6 (fixed), Full Sister 1/6 (fixed)
    //  → total = ½ + ⅓ + 1/6 + 1/6 = 7/6 → awl to 7
    //  Then grandfather + sister pool their (1+1)/6 = 2/6 and redivide 2:1
    //  → Grandfather 2/9 of estate, Sister 1/9 of estate (before awl)
    //  Final awl shares (denominator 27):
    //    Husband 9/27, Mother 6/27, Grandfather 8/27, Sister 4/27
    // ──────────────────────────────────────────────────────────────────────────
    private fun calculateAkdariyah(
        profile: DeceasedProfile,
        input: HeirInput,
        ctx: Context,
        blocked: MutableList<BlockedHeir>,
        slots: MutableList<Slot>,
        estate: BigDecimal,
        madhhab: FaraidhMadhhab,
        names: FaraidhParticipantNames
    ): FaraidhResult {
        // Spouse
        if (input.husbandCount > 0) {
            slots += Slot(HeirType.HUSBAND, 1, FaraidhFraction.of(1, 2), false, mutableListOf("proof_husband_half"))
        }
        if (input.wifeCount > 0) {
            slots += Slot(HeirType.WIFE, input.wifeCount, FaraidhFraction.of(1, 4), false, mutableListOf("proof_wife_quarter"))
        }
        // Mother
        slots += Slot(HeirType.MOTHER, 1, FaraidhFraction.of(1, 3), false, mutableListOf("proof_mother_third"))
        // Grandfather 1/6 fixed
        slots += Slot(HeirType.GRANDFATHER, 1, FaraidhFraction.of(1, 6), false, mutableListOf("proof_grandfather_sixth", "proof_akdariyah"))
        // Full Sister 1/6 fixed
        slots += Slot(HeirType.FULL_SISTER, input.fullSisterCount, FaraidhFraction.of(1, 6), false, mutableListOf("proof_sisters_fixed", "proof_akdariyah"))

        // Apply awl (total = 7/6 for husband case, or 5/6 + 1/4 for wife case)
        val rawTotal = FaraidhFraction.sumOf(slots.map { it.fraction })
        val adjustment: FaraidhAdjustment
        if (rawTotal.numerator > rawTotal.denominator) {
            // Awl: scale all proportionally
            adjustment = FaraidhAdjustment.AWL
            val scaled = FaraidhFraction.applyAwl(slots.map { it.type to it.fraction })
            scaled.forEachIndexed { index, (_, frac) ->
                slots[index] = slots[index].copy(fraction = frac)
            }
        } else {
            adjustment = FaraidhAdjustment.NONE
        }

        // Now apply Akdariyah pool: grandfather + sister combine their post-awl shares and split 2:1
        val gfIdx = slots.indexOfFirst { it.type == HeirType.GRANDFATHER }
        val sisIdx = slots.indexOfFirst { it.type == HeirType.FULL_SISTER }
        if (gfIdx >= 0 && sisIdx >= 0) {
            val combined = slots[gfIdx].fraction.add(slots[sisIdx].fraction).normalized()
            val grandFatherShare = FaraidhFraction(
                combined.numerator * BigInteger.valueOf(2),
                combined.denominator * BigInteger.valueOf(3)
            ).normalized()
            val sisterShare = FaraidhFraction(
                combined.numerator,
                combined.denominator * BigInteger.valueOf(3)
            ).normalized()
            slots[gfIdx] = slots[gfIdx].copy(fraction = grandFatherShare)
            slots[sisIdx] = slots[sisIdx].copy(fraction = sisterShare)
        }

        val activeShares = slots
            .filter { it.heads > 0 && it.fraction.numerator > BigInteger.ZERO }
            .map { slot ->
                HeirShare(
                    type = slot.type,
                    headCount = slot.heads,
                    fraction = slot.fraction,
                    percentage = slot.fraction.toPercentage(scale = 1),
                    cashAmount = slot.fraction.toCashAmount(estate),
                    isAsabah = slot.isAsabah,
                    proofKeys = slot.proofKeys.distinct()
                )
            }

        val silsilah = FaraidhSilsilahBuilder.build(profile, input, activeShares, blocked, names)
        val graph = FaraidhGraphBuilder.build(profile, input, activeShares, blocked)
        val totalDistributed = activeShares.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.cashAmount) }

        return FaraidhResult(
            deceased = profile,
            input = input,
            activeShares = activeShares,
            blockedHeirs = blocked,
            silsilah = silsilah,
            adjustment = adjustment,
            adjustmentNoteKey = if (adjustment == FaraidhAdjustment.AWL) "faraidh_awl_note" else null,
            proofKeys = listOf("proof_akdariyah"),
            totalDistributed = totalDistributed,
            remainderFraction = FaraidhFraction.ZERO,
            madhhab = madhhab,
            madhhabNoteKey = madhhabNoteKey(madhhab),
            familyGraph = graph,
            classicalCase = ClassicalCase.AL_AKDARIYAH
        )
    }

    private fun madhhabNoteKey(madhhab: FaraidhMadhhab): String = when (madhhab) {
        FaraidhMadhhab.HANAFI -> "madhhab_hanafi"
        FaraidhMadhhab.MALIKI -> "madhhab_maliki"
        FaraidhMadhhab.SHAFII -> "madhhab_shafii"
        FaraidhMadhhab.HANBALI -> "madhhab_hanbali"
    }

    private data class Context(
        val hasSon: Boolean,
        val hasDaughter: Boolean,
        val hasChild: Boolean,
        val hasGrandson: Boolean,
        val hasGranddaughter: Boolean,
        val hasGrandchild: Boolean,
        val hasFather: Boolean,
        val hasGrandfather: Boolean,
        /** Grandfather blocks siblings when no father AND no child/grandchild exist */
        val grandfatherBlocksSiblings: Boolean,
        val hasMother: Boolean,
        val siblingHeads: Int,
        val hasTwoOrMoreSiblings: Boolean,
        val siblingsBlocked: Boolean,
        val grandchildrenBlocked: Boolean,
        val bornOutOfWedlock: Boolean
    )

    private fun analyze(input: HeirInput, gender: DeceasedGender, bornOutOfWedlock: Boolean): Context {
        val hasSon = input.sonCount > 0
        val hasDaughter = input.daughterCount > 0
        val hasChild = hasSon || hasDaughter
        val grandchildrenBlocked = hasChild
        val hasGrandson = input.grandsonCount > 0 && !grandchildrenBlocked
        val hasGranddaughter = input.granddaughterCount > 0 && !grandchildrenBlocked

        val effectiveFatherCount = if (bornOutOfWedlock) 0 else input.fatherCount
        val hasFather = effectiveFatherCount > 0
        val hasGrandfather = !hasFather && input.grandfatherCount > 0

        val siblingHeads = if (bornOutOfWedlock) {
            input.maternalBrotherCount + input.maternalSisterCount + input.fullBrotherCount + input.fullSisterCount
        } else {
            input.fullBrotherCount + input.fullSisterCount +
                input.paternalBrotherCount + input.paternalSisterCount +
                input.maternalBrotherCount + input.maternalSisterCount
        }

        // Grandfather blocks siblings (except maternal) when no father and no children.
        val grandfatherBlocksSiblings = hasGrandfather && !hasChild && !hasGrandson && !hasGranddaughter

        val siblingsBlocked = hasChild || hasFather
        return Context(
            hasSon = hasSon,
            hasDaughter = hasDaughter,
            hasChild = hasChild,
            hasGrandson = hasGrandson,
            hasGranddaughter = hasGranddaughter,
            hasGrandchild = hasGrandson || hasGranddaughter,
            hasFather = hasFather,
            hasGrandfather = hasGrandfather,
            grandfatherBlocksSiblings = grandfatherBlocksSiblings,
            hasMother = input.motherCount > 0,
            siblingHeads = siblingHeads,
            hasTwoOrMoreSiblings = siblingHeads >= 2,
            siblingsBlocked = siblingsBlocked,
            grandchildrenBlocked = grandchildrenBlocked,
            bornOutOfWedlock = bornOutOfWedlock
        )
    }

    private fun resolveBlocking(
        input: HeirInput,
        ctx: Context,
        blocked: MutableList<BlockedHeir>,
        bornOutOfWedlock: Boolean
    ) {
        if (bornOutOfWedlock) {
            if (input.fatherCount > 0) blocked += BlockedHeir(HeirType.FATHER, input.fatherCount, BlockingReasonKey.OUT_OF_WEDLOCK)
            if (input.paternalBrotherCount > 0) blocked += BlockedHeir(HeirType.PATERNAL_BROTHER, input.paternalBrotherCount, BlockingReasonKey.OUT_OF_WEDLOCK)
            if (input.paternalSisterCount > 0) blocked += BlockedHeir(HeirType.PATERNAL_SISTER, input.paternalSisterCount, BlockingReasonKey.OUT_OF_WEDLOCK)
        }

        if (ctx.grandchildrenBlocked) {
            addBlocked(input.grandsonCount, HeirType.GRANDSON, BlockingReasonKey.BY_CHILDREN, blocked)
            addBlocked(input.granddaughterCount, HeirType.GRANDDAUGHTER, BlockingReasonKey.BY_CHILDREN, blocked)
        }
        if (ctx.siblingsBlocked) {
            val reason = if (ctx.hasChild) BlockingReasonKey.BY_CHILDREN else BlockingReasonKey.BY_FATHER
            if (!bornOutOfWedlock) {
                addBlocked(input.fullBrotherCount, HeirType.FULL_BROTHER, reason, blocked)
                addBlocked(input.fullSisterCount, HeirType.FULL_SISTER, reason, blocked)
                addBlocked(input.paternalBrotherCount, HeirType.PATERNAL_BROTHER, reason, blocked)
                addBlocked(input.paternalSisterCount, HeirType.PATERNAL_SISTER, reason, blocked)
            } else {
                if (ctx.hasChild) {
                    addBlocked(input.fullBrotherCount, HeirType.FULL_BROTHER, reason, blocked)
                    addBlocked(input.fullSisterCount, HeirType.FULL_SISTER, reason, blocked)
                }
            }
            val maternalHeads = input.maternalBrotherCount + input.maternalSisterCount
            if (maternalHeads > 0) blocked += BlockedHeir(HeirType.MATERNAL_SIBLING, maternalHeads, reason)
        }

        // Grandfather blocks full/paternal siblings (but NOT maternal siblings) when no father or children.
        if (ctx.grandfatherBlocksSiblings && !bornOutOfWedlock) {
            addBlocked(input.fullBrotherCount, HeirType.FULL_BROTHER, BlockingReasonKey.BY_GRANDFATHER, blocked)
            addBlocked(input.fullSisterCount, HeirType.FULL_SISTER, BlockingReasonKey.BY_GRANDFATHER, blocked)
            addBlocked(input.paternalBrotherCount, HeirType.PATERNAL_BROTHER, BlockingReasonKey.BY_GRANDFATHER, blocked)
            addBlocked(input.paternalSisterCount, HeirType.PATERNAL_SISTER, BlockingReasonKey.BY_GRANDFATHER, blocked)
        }
    }

    private fun addBlocked(count: Int, type: HeirType, reason: BlockingReasonKey, blocked: MutableList<BlockedHeir>) {
        if (count > 0) blocked += BlockedHeir(type, count, reason)
    }

    private fun assignFixedShares(
        input: HeirInput,
        profile: DeceasedProfile,
        ctx: Context,
        slots: MutableList<Slot>,
        blocked: MutableList<BlockedHeir>,
        madhhab: FaraidhMadhhab
    ) {
        when (profile.gender) {
            DeceasedGender.FEMALE -> {
                if (input.husbandCount > 0) {
                    val base = if (ctx.hasChild) FaraidhFraction.of(1, 4) else FaraidhFraction.of(1, 2)
                    val perHead = base.divideAmongHeads(input.husbandCount)
                    slots += Slot(HeirType.HUSBAND, input.husbandCount, perHead.multiplyScalar(input.husbandCount), isAsabah = false,
                        proofKeys = mutableListOf(if (ctx.hasChild) "proof_husband_quarter" else "proof_husband_half"))
                }
                if (input.wifeCount > 0) addBlocked(input.wifeCount, HeirType.WIFE, BlockingReasonKey.GENDER_MISMATCH, blocked)
            }
            DeceasedGender.MALE -> {
                if (input.wifeCount > 0) {
                    val base = if (ctx.hasChild) FaraidhFraction.of(1, 8) else FaraidhFraction.of(1, 4)
                    slots += Slot(HeirType.WIFE, input.wifeCount, base, isAsabah = false,
                        proofKeys = mutableListOf(if (ctx.hasChild) "proof_wife_eighth" else "proof_wife_quarter"))
                }
                if (input.husbandCount > 0) addBlocked(input.husbandCount, HeirType.HUSBAND, BlockingReasonKey.GENDER_MISMATCH, blocked)
            }
        }

        // ── Father ─────────────────────────────────────────────────────────────
        if (ctx.hasFather) {
            if (ctx.hasChild || ctx.hasGrandchild) {
                // Father gets 1/6 fixed; also takes residue as asabah if only daughters — see assignAsabahResidue
                slots += Slot(HeirType.FATHER, 1, FaraidhFraction.of(1, 6), isAsabah = false, proofKeys = mutableListOf("proof_father_sixth"))
            }
            // When no child/grandchild, father is pure asabah — handled in assignAsabahResidue
        }

        // ── Grandfather ────────────────────────────────────────────────────────
        // Grandfather is blocked by father (handled in analyze → hasGrandfather = !hasFather).
        // He is also blocked by son (sons block through hasChild path).
        // When present:
        //   - Child/grandchild exist  → grandfather gets 1/6 fixed (same as father); no asabah residue
        //   - No child/grandchild     → grandfather is primary asabah (handled in assignAsabahResidue)
        //                               BUT if maternal siblings exist, grandfather gets max(1/6, asabah share) or
        //                               outright 1/3 of residue when no other asabah. This is complex; we apply the
        //                               standard Shafi'i/Hanbali majority: grandfather takes residue as asabah
        //                               (same rank as father when father is absent).
        if (ctx.hasGrandfather) {
            if (ctx.hasChild || ctx.hasGrandchild) {
                slots += Slot(HeirType.GRANDFATHER, 1, FaraidhFraction.of(1, 6), isAsabah = false, proofKeys = mutableListOf("proof_grandfather_sixth"))
            }
            // No child/grandchild → grandfather takes residue as asabah (see assignAsabahResidue)
        }

        // ── Mother ─────────────────────────────────────────────────────────────
        if (ctx.hasMother) {
            val share = if (ctx.hasChild || ctx.hasTwoOrMoreSiblings) FaraidhFraction.of(1, 6) else FaraidhFraction.of(1, 3)
            slots += Slot(HeirType.MOTHER, 1, share, isAsabah = false,
                proofKeys = mutableListOf(if (ctx.hasChild || ctx.hasTwoOrMoreSiblings) "proof_mother_sixth" else "proof_mother_third"))
        }

        // ── Daughters (furud only when no son) ────────────────────────────────
        if (ctx.hasDaughter && !ctx.hasSon) {
            val share = if (input.daughterCount == 1) FaraidhFraction.of(1, 2) else FaraidhFraction.of(2, 3)
            slots += Slot(HeirType.DAUGHTER, input.daughterCount, share, isAsabah = false,
                proofKeys = mutableListOf(if (input.daughterCount == 1) "proof_daughter_half" else "proof_daughters_two_thirds"))
        }

        // ── Siblings ───────────────────────────────────────────────────────────
        if (!ctx.siblingsBlocked) {
            val maternalHeads = if (profile.bornOutOfWedlock) {
                input.maternalBrotherCount + input.maternalSisterCount + input.fullBrotherCount + input.fullSisterCount
            } else {
                input.maternalBrotherCount + input.maternalSisterCount
            }
            if (maternalHeads > 0) {
                val share = if (maternalHeads == 1) FaraidhFraction.of(1, 6) else FaraidhFraction.of(1, 3)
                slots += Slot(HeirType.MATERNAL_SIBLING, maternalHeads, share, isAsabah = false, proofKeys = mutableListOf("proof_maternal_siblings"))
            }

            // Full sisters get furud only when no full brother and no children
            if (!profile.bornOutOfWedlock && input.fullBrotherCount == 0 && input.fullSisterCount > 0 && !ctx.hasChild && !ctx.grandfatherBlocksSiblings) {
                val share = if (input.fullSisterCount == 1) FaraidhFraction.of(1, 2) else FaraidhFraction.of(2, 3)
                slots += Slot(HeirType.FULL_SISTER, input.fullSisterCount, share, isAsabah = false, proofKeys = mutableListOf("proof_sisters_fixed"))
            }

            // ── Al-Marwaniyah (Al-Mushtaraka): Maliki madhhab only ──────────
            // Husband + Mother + Full Brother(s) + Maternal siblings.
            // Non-Maliki: full brothers take entire residue as asabah (handled below in asabah step).
            // Maliki: maternal siblings share equally with full brothers in the 1/3 slot.
            val isMarwaniyah = !profile.bornOutOfWedlock
                && (input.husbandCount > 0 || input.wifeCount > 0)
                && input.motherCount > 0
                && (input.fullBrotherCount > 0)
                && (input.maternalBrotherCount + input.maternalSisterCount) > 0
                && !ctx.hasChild && !ctx.hasFather && !ctx.hasGrandfather
            if (isMarwaniyah && madhhab == FaraidhMadhhab.MALIKI) {
                // Maternal siblings already assigned their 1/3 above. Full brothers share equally with them in that 1/3.
                // Remove existing maternal sibling slot and combine heads.
                val mIdx = slots.indexOfFirst { it.type == HeirType.MATERNAL_SIBLING }
                if (mIdx >= 0) {
                    val totalSiblings = (input.maternalBrotherCount + input.maternalSisterCount) + input.fullBrotherCount +
                        (if (input.fullSisterCount > 0 && input.fullBrotherCount == 0) 0 else input.fullSisterCount)
                    val share = if (totalSiblings == 1) FaraidhFraction.of(1, 6) else FaraidhFraction.of(1, 3)
                    // Assign 1/3 shared equally across all siblings (full + maternal)
                    slots[mIdx] = slots[mIdx].copy(
                        type = HeirType.MATERNAL_SIBLING,
                        heads = input.maternalBrotherCount + input.maternalSisterCount,
                        fraction = share.multiply(FaraidhFraction.of((input.maternalBrotherCount + input.maternalSisterCount).toLong(), totalSiblings.toLong())).normalized(),
                        proofKeys = mutableListOf("proof_marwaniyah")
                    )
                    slots += Slot(HeirType.FULL_BROTHER, input.fullBrotherCount,
                        share.multiply(FaraidhFraction.of(input.fullBrotherCount.toLong(), totalSiblings.toLong())).normalized(),
                        isAsabah = false, proofKeys = mutableListOf("proof_marwaniyah"))
                }
            }

            // Paternal sisters get furud only when no paternal/full brother and no children and grandfather doesn't block
            if (!profile.bornOutOfWedlock && input.paternalBrotherCount == 0 && input.paternalSisterCount > 0
                && input.fullBrotherCount == 0 && input.fullSisterCount == 0 && !ctx.hasChild && !ctx.grandfatherBlocksSiblings
            ) {
                val share = if (input.paternalSisterCount == 1) FaraidhFraction.of(1, 2) else FaraidhFraction.of(2, 3)
                slots += Slot(HeirType.PATERNAL_SISTER, input.paternalSisterCount, share, isAsabah = false, proofKeys = mutableListOf("proof_sisters_fixed"))
            }
        }
    }

    private fun assignAsabahResidue(
        input: HeirInput,
        ctx: Context,
        slots: MutableList<Slot>,
        blocked: MutableList<BlockedHeir>,
        madhhab: FaraidhMadhhab
    ) {
        val used = FaraidhFraction.sumOf(slots.map { it.fraction })
        var residue = if (used.numerator >= used.denominator) {
            FaraidhFraction.ZERO
        } else {
            FaraidhFraction(used.denominator - used.numerator, used.denominator).normalized()
        }

        if (residue.numerator == BigInteger.ZERO) return

        val asabahGroups = buildAsabahPriority(input, ctx)
        for (group in asabahGroups) {
            if (residue.numerator == BigInteger.ZERO) break
            val (type, maleHeads, femaleHeads) = group
            if (maleHeads + femaleHeads <= 0) continue

            val maleWeight = maleHeads * 2
            val femaleWeight = femaleHeads
            val totalWeight = maleWeight + femaleWeight
            val groupShare = residue

            if (maleHeads > 0) {
                val maleFrac = FaraidhFraction(
                    groupShare.numerator * BigInteger.valueOf(maleWeight.toLong()),
                    groupShare.denominator * BigInteger.valueOf(totalWeight.toLong())
                ).divideAmongHeads(maleHeads)
                mergeOrAdd(slots, Slot(type, maleHeads, maleFrac.multiplyScalar(maleHeads), true, mutableListOf("proof_asabah")))
            }
            if (femaleHeads > 0) {
                val femaleType = sisterTypeFor(type)
                val femaleFrac = FaraidhFraction(
                    groupShare.numerator * BigInteger.valueOf(femaleWeight.toLong()),
                    groupShare.denominator * BigInteger.valueOf(totalWeight.toLong())
                ).divideAmongHeads(femaleHeads)
                mergeOrAdd(slots, Slot(femaleType, femaleHeads, femaleFrac.multiplyScalar(femaleHeads), true, mutableListOf("proof_asabah")))
            }
            residue = FaraidhFraction.ZERO
            return
        }

        // Father takes residue as asabah when:
        //   (a) no descendants at all — he is primary asabah, OR
        //   (b) only daughters/granddaughters: took furud; father sweeps the rest
        val onlyFemaleDescendants = (ctx.hasDaughter && !ctx.hasSon) || (ctx.hasGranddaughter && !ctx.hasGrandson)
        if (ctx.hasFather && (!ctx.hasChild && !ctx.hasGrandchild || onlyFemaleDescendants)) {
            mergeOrAdd(slots, Slot(HeirType.FATHER, 1, residue, true, mutableListOf("proof_father_residue")))
            return
        }

        // Grandfather takes residue as asabah (when no father):
        //   (a) no descendants → grandfather is primary asabah
        //   (b) only daughters/granddaughters → same as father above
        if (ctx.hasGrandfather && (!ctx.hasChild && !ctx.hasGrandchild || onlyFemaleDescendants)) {
            mergeOrAdd(slots, Slot(HeirType.GRANDFATHER, 1, residue, true, mutableListOf("proof_grandfather_residue")))
        }
    }

    private data class AsabahGroup(val type: HeirType, val maleHeads: Int, val femaleHeads: Int)

    private fun buildAsabahPriority(input: HeirInput, ctx: Context): List<AsabahGroup> = buildList {
        if (ctx.hasSon) {
            // Sons (and daughters riding with them as asabah bil-ghayr) take the residue.
            // Daughters ALONE already received furud — they do NOT enter here.
            add(AsabahGroup(HeirType.SON, input.sonCount, input.daughterCount))
        } else if (ctx.hasGrandson) {
            // Same rule: grandsons pull granddaughters as asabah bil-ghayr.
            add(AsabahGroup(HeirType.GRANDSON, input.grandsonCount, input.granddaughterCount))
        } else if (!ctx.siblingsBlocked && !ctx.grandfatherBlocksSiblings && !ctx.bornOutOfWedlock) {
            if (input.fullBrotherCount > 0 || input.fullSisterCount > 0) {
                add(AsabahGroup(HeirType.FULL_BROTHER, input.fullBrotherCount, input.fullSisterCount))
            } else if (input.paternalBrotherCount > 0 || input.paternalSisterCount > 0) {
                add(AsabahGroup(HeirType.PATERNAL_BROTHER, input.paternalBrotherCount, input.paternalSisterCount))
            }
        }
    }

    private fun sisterTypeFor(brotherType: HeirType): HeirType = when (brotherType) {
        HeirType.SON -> HeirType.DAUGHTER
        HeirType.GRANDSON -> HeirType.GRANDDAUGHTER
        HeirType.FULL_BROTHER -> HeirType.FULL_SISTER
        HeirType.PATERNAL_BROTHER -> HeirType.PATERNAL_SISTER
        else -> brotherType
    }

    private fun mergeOrAdd(slots: MutableList<Slot>, incoming: Slot) {
        val index = slots.indexOfFirst { it.type == incoming.type && it.isAsabah == incoming.isAsabah }
        if (index >= 0) {
            val existing = slots[index]
            slots[index] = existing.copy(
                fraction = existing.fraction.add(incoming.fraction).normalized(),
                proofKeys = (existing.proofKeys + incoming.proofKeys).distinct().toMutableList()
            )
        } else {
            slots += incoming
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Classical case detection (used to show a label in the Results tab)
    // ──────────────────────────────────────────────────────────────────────────
    private fun detectClassicalCase(input: HeirInput, ctx: Context, madhhab: FaraidhMadhhab): ClassicalCase? {
        // Al-Minbariyah: Husband + Mother + Two Daughters + Father (and no other heirs)
        if (input.husbandCount == 0 && input.wifeCount > 0) return null
        val isMinbariyah = input.husbandCount > 0 && (
            input.motherCount == 1 && input.daughterCount >= 2
                && input.fatherCount == 1 && input.sonCount == 0
                && input.fullBrotherCount == 0 && input.fullSisterCount == 0
                && input.grandfatherCount == 0 && input.maternalBrotherCount == 0
                && input.maternalSisterCount == 0
        )
        if (isMinbariyah) return ClassicalCase.AL_MINBARIYAH

        // Al-Akdariyah: detected in calculate() before reaching this method — returned early.
        // But we re-check here in case it reaches this path through a different route.
        val isAkdariyah = ctx.hasGrandfather && !ctx.hasFather && !ctx.hasChild && !ctx.hasGrandchild
            && input.motherCount > 0 && (input.husbandCount > 0 || input.wifeCount > 0)
            && input.fullSisterCount > 0 && input.fullBrotherCount == 0
        if (isAkdariyah) return ClassicalCase.AL_AKDARIYAH

        // Al-Marwaniyah: Husband + Mother + Full Brother(s) + Maternal siblings, no children, no father/grandfather
        val maternalHeads = input.maternalBrotherCount + input.maternalSisterCount
        val isMarwaniyah = (input.husbandCount > 0 || input.wifeCount > 0)
            && input.motherCount > 0 && input.fullBrotherCount > 0 && maternalHeads > 0
            && !ctx.hasChild && !ctx.hasFather && !ctx.hasGrandfather
        if (isMarwaniyah) return ClassicalCase.AL_MARWANIYAH

        // Al-ʿUmariyatain: Spouse + both parents, no children (mother gets 1/3 of residue)
        val isUmariyatain = (input.husbandCount > 0 || input.wifeCount > 0)
            && input.fatherCount > 0 && input.motherCount > 0
            && !ctx.hasChild && !ctx.hasGrandchild
        if (isUmariyatain) return ClassicalCase.UMARIYATAIN

        return null
    }
}
