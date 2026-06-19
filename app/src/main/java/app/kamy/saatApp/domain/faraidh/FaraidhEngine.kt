package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Offline Islamic inheritance (faraidh) engine.
 * Implements blocking (hajb), fixed shares (ashab al-furud), residue (asabah),
 * and deficit/surplus adjustments (awl / radd) using [BigDecimal] / [FaraidhFraction].
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
        val ctx = analyze(input, profile.gender, profile.bornOutOfWedlock)

        if (!input.hasAnyHeir()) {
            return emptyResult(profile, input, estate, names)
        }

        resolveBlocking(input, ctx, blocked, profile.bornOutOfWedlock)
        val slots = mutableListOf<Slot>()
        assignFixedShares(input, profile, ctx, slots, blocked)
        assignAsabahResidue(input, ctx, slots, blocked)

        var adjustment = FaraidhAdjustment.NONE
        var adjustmentNoteKey: String? = null

        val fixedTotal = FaraidhFraction.sumOf(slots.map { it.fraction })
        if (fixedTotal.numerator > fixedTotal.denominator) {
            adjustment = FaraidhAdjustment.AWL
            adjustmentNoteKey = "faraidh_awl_note"
            val scaled = FaraidhFraction.applyAwl(slots.map { it.type to it.fraction })
            scaled.forEachIndexed { index, (type, frac) ->
                slots[index] = slots[index].copy(fraction = frac)
            }
        } else if (fixedTotal.numerator < fixedTotal.denominator && slots.none { it.isAsabah && it.fraction.numerator > BigInteger.ZERO }) {
            adjustment = FaraidhAdjustment.RADD
            adjustmentNoteKey = if (madhhab.raddIncludesSpouses()) "faraidh_radd_note_hanafi" else "faraidh_radd_note"
            val withFlag = slots.map { Triple(it.type, it.fraction, it.isAsabah) }
            val spouseTypes = if (madhhab.raddIncludesSpouses()) {
                emptySet()
            } else {
                setOf(HeirType.HUSBAND, HeirType.WIFE)
            }
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

        return FaraidhResult(
            deceased = profile,
            input = input,
            activeShares = activeShares,
            blockedHeirs = blocked,
            silsilah = silsilah,
            adjustment = adjustment,
            adjustmentNoteKey = adjustmentNoteKey,
            proofKeys = proofKeys.distinct(),
            totalDistributed = totalDistributed,
            remainderFraction = remainder,
            madhhab = madhhab,
            madhhabNoteKey = madhhabNoteKey(madhhab)
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
        
        val siblingHeads = if (bornOutOfWedlock) {
            input.maternalBrotherCount + input.maternalSisterCount + input.fullBrotherCount + input.fullSisterCount
        } else {
            input.fullBrotherCount + input.fullSisterCount +
                input.paternalBrotherCount + input.paternalSisterCount +
                input.maternalBrotherCount + input.maternalSisterCount
        }
        
        val siblingsBlocked = hasChild || hasFather
        return Context(
            hasSon = hasSon,
            hasDaughter = hasDaughter,
            hasChild = hasChild,
            hasGrandson = hasGrandson,
            hasGranddaughter = hasGranddaughter,
            hasGrandchild = hasGrandson || hasGranddaughter,
            hasFather = hasFather,
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
            if (input.fatherCount > 0) {
                blocked += BlockedHeir(HeirType.FATHER, input.fatherCount, BlockingReasonKey.OUT_OF_WEDLOCK)
            }
            if (input.paternalBrotherCount > 0) {
                blocked += BlockedHeir(HeirType.PATERNAL_BROTHER, input.paternalBrotherCount, BlockingReasonKey.OUT_OF_WEDLOCK)
            }
            if (input.paternalSisterCount > 0) {
                blocked += BlockedHeir(HeirType.PATERNAL_SISTER, input.paternalSisterCount, BlockingReasonKey.OUT_OF_WEDLOCK)
            }
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
            if (maternalHeads > 0) {
                blocked += BlockedHeir(HeirType.MATERNAL_SIBLING, maternalHeads, reason)
            }
        }
        if (ctx.hasSon) {
            // Sons fully consume grandson rank; grandchildren already blocked above when child exists
        }
    }

    private fun addBlocked(
        count: Int,
        type: HeirType,
        reason: BlockingReasonKey,
        blocked: MutableList<BlockedHeir>
    ) {
        if (count > 0) blocked += BlockedHeir(type, count, reason)
    }

    private fun assignFixedShares(
        input: HeirInput,
        profile: DeceasedProfile,
        ctx: Context,
        slots: MutableList<Slot>,
        blocked: MutableList<BlockedHeir>
    ) {
        when (profile.gender) {
            DeceasedGender.FEMALE -> {
                if (input.husbandCount > 0) {
                    val base = if (ctx.hasChild) FaraidhFraction.of(1, 4) else FaraidhFraction.of(1, 2)
                    val perHead = base.divideAmongHeads(input.husbandCount)
                    slots += Slot(
                        HeirType.HUSBAND,
                        input.husbandCount,
                        perHead.multiplyScalar(input.husbandCount),
                        isAsabah = false,
                        proofKeys = mutableListOf(if (ctx.hasChild) "proof_husband_quarter" else "proof_husband_half")
                    )
                }
                if (input.wifeCount > 0) {
                    addBlocked(input.wifeCount, HeirType.WIFE, BlockingReasonKey.GENDER_MISMATCH, blocked)
                }
            }
            DeceasedGender.MALE -> {
                if (input.wifeCount > 0) {
                    val base = if (ctx.hasChild) FaraidhFraction.of(1, 8) else FaraidhFraction.of(1, 4)
                    slots += Slot(
                        HeirType.WIFE,
                        input.wifeCount,
                        base,
                        isAsabah = false,
                        proofKeys = mutableListOf(if (ctx.hasChild) "proof_wife_eighth" else "proof_wife_quarter")
                    )
                }
                if (input.husbandCount > 0) {
                    addBlocked(input.husbandCount, HeirType.HUSBAND, BlockingReasonKey.GENDER_MISMATCH, blocked)
                }
            }
        }

        if (ctx.hasFather) {
            if (ctx.hasChild || ctx.hasGrandchild) {
                slots += Slot(
                    HeirType.FATHER,
                    1,
                    FaraidhFraction.of(1, 6),
                    isAsabah = false,
                    proofKeys = mutableListOf("proof_father_sixth")
                )
            }
        }

        if (ctx.hasMother) {
            val share = if (ctx.hasChild || ctx.hasTwoOrMoreSiblings) {
                FaraidhFraction.of(1, 6)
            } else {
                FaraidhFraction.of(1, 3)
            }
            slots += Slot(
                HeirType.MOTHER,
                1,
                share,
                isAsabah = false,
                proofKeys = mutableListOf(
                    if (ctx.hasChild || ctx.hasTwoOrMoreSiblings) "proof_mother_sixth" else "proof_mother_third"
                )
            )
        }

        if (ctx.hasDaughter && !ctx.hasSon) {
            val share = if (input.daughterCount == 1) {
                FaraidhFraction.of(1, 2)
            } else {
                FaraidhFraction.of(2, 3)
            }
            slots += Slot(
                HeirType.DAUGHTER,
                input.daughterCount,
                share,
                isAsabah = false,
                proofKeys = mutableListOf(
                    if (input.daughterCount == 1) "proof_daughter_half" else "proof_daughters_two_thirds"
                )
            )
        }

        if (!ctx.siblingsBlocked) {
            val maternalHeads = if (profile.bornOutOfWedlock) {
                input.maternalBrotherCount + input.maternalSisterCount + input.fullBrotherCount + input.fullSisterCount
            } else {
                input.maternalBrotherCount + input.maternalSisterCount
            }
            if (maternalHeads > 0) {
                val share = if (maternalHeads == 1) FaraidhFraction.of(1, 6) else FaraidhFraction.of(1, 3)
                slots += Slot(
                    HeirType.MATERNAL_SIBLING,
                    maternalHeads,
                    share,
                    isAsabah = false,
                    proofKeys = mutableListOf("proof_maternal_siblings")
                )
            }

            if (!profile.bornOutOfWedlock && input.fullBrotherCount == 0 && input.fullSisterCount > 0 && !ctx.hasChild) {
                val share = if (input.fullSisterCount == 1) {
                    FaraidhFraction.of(1, 2)
                } else {
                    FaraidhFraction.of(2, 3)
                }
                slots += Slot(
                    HeirType.FULL_SISTER,
                    input.fullSisterCount,
                    share,
                    isAsabah = false,
                    proofKeys = mutableListOf("proof_sisters_fixed")
                )
            }

            if (!profile.bornOutOfWedlock && input.paternalBrotherCount == 0 && input.paternalSisterCount > 0 &&
                input.fullBrotherCount == 0 && input.fullSisterCount == 0 && !ctx.hasChild
            ) {
                val share = if (input.paternalSisterCount == 1) {
                    FaraidhFraction.of(1, 2)
                } else {
                    FaraidhFraction.of(2, 3)
                }
                slots += Slot(
                    HeirType.PATERNAL_SISTER,
                    input.paternalSisterCount,
                    share,
                    isAsabah = false,
                    proofKeys = mutableListOf("proof_sisters_fixed")
                )
            }
        }
    }

    private fun assignAsabahResidue(
        input: HeirInput,
        ctx: Context,
        slots: MutableList<Slot>,
        blocked: MutableList<BlockedHeir>
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

        if (ctx.hasFather && !ctx.hasChild && !ctx.hasGrandchild) {
            mergeOrAdd(
                slots,
                Slot(HeirType.FATHER, 1, residue, true, mutableListOf("proof_father_residue"))
            )
        }
    }

    private data class AsabahGroup(val type: HeirType, val maleHeads: Int, val femaleHeads: Int)

    private fun buildAsabahPriority(input: HeirInput, ctx: Context): List<AsabahGroup> = buildList {
        if (ctx.hasSon || ctx.hasDaughter) {
            add(AsabahGroup(HeirType.SON, input.sonCount, input.daughterCount))
        } else if (ctx.hasGrandson || ctx.hasGranddaughter) {
            add(AsabahGroup(HeirType.GRANDSON, input.grandsonCount, input.granddaughterCount))
        } else if (!ctx.siblingsBlocked && !ctx.bornOutOfWedlock) {
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

    private fun emptyResult(
        profile: DeceasedProfile,
        input: HeirInput,
        estate: BigDecimal,
        names: FaraidhParticipantNames
    ): FaraidhResult =
        FaraidhResult(
            deceased = profile,
            input = input,
            activeShares = emptyList(),
            blockedHeirs = emptyList(),
            silsilah = FaraidhSilsilahBuilder.build(profile, input, emptyList(), emptyList(), names),
            adjustment = FaraidhAdjustment.NONE,
            adjustmentNoteKey = null,
            proofKeys = emptyList(),
            totalDistributed = BigDecimal.ZERO.setScale(2),
            remainderFraction = FaraidhFraction.ONE,
            madhhab = profile.madhhab,
            madhhabNoteKey = madhhabNoteKey(profile.madhhab)
        )
}
