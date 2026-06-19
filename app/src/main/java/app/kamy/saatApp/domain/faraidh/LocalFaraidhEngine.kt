package app.kamy.saatApp.domain.faraidh

import java.math.BigInteger

object LocalFaraidhEngine {

    fun calculate(relatives: List<RelativeInput>): LocalFaraidhResult {
        val normalized = relatives.groupBy { it.type }
            .map { (type, items) ->
                items.reduce { acc, item -> acc.copy(count = acc.count + item.count) }
            }

        val context = analyze(normalized)
        val blocked = resolveDisqualifications(normalized, context)

        val fixedShares = assignFixedShares(normalized, context, blocked)
        val residueShares = assignResidue(normalized, context, fixedShares, blocked)
        val finalShares = applyAulOrRadd(fixedShares + residueShares)

        return LocalFaraidhResult(
            shares = finalShares,
            blockedReasons = blocked,
            ruleCitations = context.ruleCitations,
            notes = buildNotes(context, fixedShares, residueShares, finalShares)
        )
    }

    private fun analyze(relatives: List<RelativeInput>): LocalEngineContext {
        val hasChildren = relatives.any { it.type in setOf(RelativeType.SON, RelativeType.DAUGHTER) && it.count > 0 }
        val hasGrandchildren = relatives.any { it.type in setOf(RelativeType.GRANDSON, RelativeType.GRANDDAUGHTER) && it.count > 0 }
        val hasFather = relatives.any { it.type == RelativeType.FATHER && it.count > 0 }
        val hasGrandfather = relatives.any { it.type == RelativeType.GRANDFATHER && it.count > 0 && !hasFather }
        val hasMother = relatives.any { it.type == RelativeType.MOTHER && it.count > 0 }
        val hasSiblings = relatives.any { it.type in setOf(RelativeType.FULL_BROTHER, RelativeType.FULL_SISTER, RelativeType.PATERNAL_BROTHER, RelativeType.PATERNAL_SISTER, RelativeType.MATERNAL_SIBLING) && it.count > 0 }
        val hasJanin = relatives.any { it.isJanin }
        val hasMafqud = relatives.any { it.isMafqud }
        val simultaneous = relatives.any { it.isSimultaneousDeath }
        val waladZina = relatives.any { it.isWaladZina }

        return LocalEngineContext(
            hasChildren = hasChildren,
            hasGrandchildren = hasGrandchildren,
            hasFather = hasFather,
            hasGrandfather = hasGrandfather,
            hasMother = hasMother,
            hasSiblings = hasSiblings,
            hasJanin = hasJanin,
            hasMafqud = hasMafqud,
            simultaneousDeath = simultaneous,
            waladZina = waladZina
        )
    }

    private fun resolveDisqualifications(
        relatives: List<RelativeInput>,
        context: LocalEngineContext
    ): List<BlockedReason> {
        return relatives.flatMap { relative ->
            val reasons = mutableListOf<BlockReason>()

            if (!relative.isMuslim) reasons += BlockReason.OUT_OF_WEDLOCK
            if (relative.isMurderer) reasons += BlockReason.MURDER
            if (relative.isMafqud) reasons += BlockReason.MAFQUD
            if (relative.isSimultaneousDeath && context.simultaneousDeath) reasons += BlockReason.SIMULTANEOUS_DEATH
            if (relative.type == RelativeType.GRANDFATHER && context.hasFather) reasons += BlockReason.BY_FATHER
            if (relative.type == RelativeType.FATHER && context.hasChildren) reasons += BlockReason.BY_SON
            if (relative.type in setOf(RelativeType.FULL_BROTHER, RelativeType.FULL_SISTER) && context.hasGrandfather && !context.hasFather && !context.hasChildren) reasons += BlockReason.BY_GRANDFATHER

            reasons.map { reason ->
                BlockedReason(relative.type, relative.count, reason)
            }
        }
    }

    private fun assignFixedShares(
        relatives: List<RelativeInput>,
        context: LocalEngineContext,
        blocked: List<BlockedReason>
    ): List<LocalHeirShare> {
        val slots = mutableListOf<LocalHeirShare>()

        if (context.hasMother && blocked.none { it.type == RelativeType.MOTHER }) {
            slots += LocalHeirShare(
                type = RelativeType.MOTHER,
                heads = 1,
                fraction = if (context.hasChildren || context.hasTwoOrMoreSiblings()) FaraidhFraction.of(1, 6) else FaraidhFraction.of(1, 3),
                isAsabah = false,
                proofKeys = listOf("proof_mother")
            )
        }

        if (relatives.any { it.type == RelativeType.HUSBAND && it.count > 0 } && blocked.none { it.type == RelativeType.HUSBAND }) {
            slots += LocalHeirShare(
                type = RelativeType.HUSBAND,
                heads = relatives.countOf(RelativeType.HUSBAND),
                fraction = if (context.hasChildren) FaraidhFraction.of(1, 4) else FaraidhFraction.of(1, 2),
                isAsabah = false,
                proofKeys = listOf("proof_husband")
            )
        }

        if (relatives.any { it.type == RelativeType.WIFE && it.count > 0 } && blocked.none { it.type == RelativeType.WIFE }) {
            slots += LocalHeirShare(
                type = RelativeType.WIFE,
                heads = relatives.countOf(RelativeType.WIFE),
                fraction = if (context.hasChildren) FaraidhFraction.of(1, 8) else FaraidhFraction.of(1, 4),
                isAsabah = false,
                proofKeys = listOf("proof_wife")
            )
        }

        return slots
    }

    private fun assignResidue(
        relatives: List<RelativeInput>,
        context: LocalEngineContext,
        fixedShares: List<LocalHeirShare>,
        blocked: List<BlockedReason>
    ): List<LocalHeirShare> {
        val totalFixed = FaraidhFraction.sumOf(fixedShares.map { it.fraction })
        val residue = if (totalFixed.numerator >= totalFixed.denominator) FaraidhFraction.ZERO else FaraidhFraction(totalFixed.denominator - totalFixed.numerator, totalFixed.denominator)

        if (residue == FaraidhFraction.ZERO) return emptyList()

        val asabahGroups = buildAsabahPriority(relatives, context)
        return distributeResidue(residue, asabahGroups)
    }

    private fun buildAsabahPriority(relatives: List<RelativeInput>, context: LocalEngineContext): List<AsabahGroup> {
        if (context.hasChildren) {
            return listOf(
                AsabahGroup(RelativeType.SON, relatives.countOf(RelativeType.SON), relatives.countOf(RelativeType.DAUGHTER))
            )
        }
        if (context.hasGrandchildren) {
            return listOf(
                AsabahGroup(RelativeType.GRANDSON, relatives.countOf(RelativeType.GRANDSON), relatives.countOf(RelativeType.GRANDDAUGHTER))
            )
        }
        if (!context.hasFather && !context.hasGrandfather && !context.simultaneousDeath) {
            if (relatives.countOf(RelativeType.FULL_BROTHER) > 0 || relatives.countOf(RelativeType.FULL_SISTER) > 0) {
                return listOf(AsabahGroup(RelativeType.FULL_BROTHER, relatives.countOf(RelativeType.FULL_BROTHER), relatives.countOf(RelativeType.FULL_SISTER)))
            }
            if (relatives.countOf(RelativeType.PATERNAL_BROTHER) > 0 || relatives.countOf(RelativeType.PATERNAL_SISTER) > 0) {
                return listOf(AsabahGroup(RelativeType.PATERNAL_BROTHER, relatives.countOf(RelativeType.PATERNAL_BROTHER), relatives.countOf(RelativeType.PATERNAL_SISTER)))
            }
        }
        return emptyList()
    }

    private fun distributeResidue(residue: FaraidhFraction, groups: List<AsabahGroup>): List<LocalHeirShare> {
        if (groups.isEmpty()) return emptyList()
        val group = groups.first()
        val maleWeight = BigInteger.valueOf(group.maleHeads.toLong() * 2)
        val femaleWeight = BigInteger.valueOf(group.femaleHeads.toLong())
        val totalWeight = maleWeight + femaleWeight

        val maleShare = if (group.maleHeads > 0) {
            FaraidhFraction(residue.numerator * maleWeight, residue.denominator * totalWeight)
                .divideAmongHeads(group.maleHeads)
        } else FaraidhFraction.ZERO

        val femaleShare = if (group.femaleHeads > 0) {
            FaraidhFraction(residue.numerator * femaleWeight, residue.denominator * totalWeight)
                .divideAmongHeads(group.femaleHeads)
        } else FaraidhFraction.ZERO

        val shares = mutableListOf<LocalHeirShare>()
        if (group.maleHeads > 0) shares += LocalHeirShare(group.type, group.maleHeads, maleShare.multiplyScalar(group.maleHeads), true, listOf("proof_asabah"))
        if (group.femaleHeads > 0) shares += LocalHeirShare(sisterType(group.type), group.femaleHeads, femaleShare.multiplyScalar(group.femaleHeads), true, listOf("proof_asabah"))
        return shares
    }

    private fun applyAulOrRadd(shares: List<LocalHeirShare>): List<LocalHeirShare> {
        val total = FaraidhFraction.sumOf(shares.map { it.fraction })
        return when {
            total.numerator > total.denominator -> applyAul(shares)
            total.numerator < total.denominator -> applyRadd(shares)
            else -> shares
        }
    }

    private fun applyAul(shares: List<LocalHeirShare>): List<LocalHeirShare> {
        val total = FaraidhFraction.sumOf(shares.map { it.fraction })
        val scaleFactor = FaraidhFraction(total.denominator, total.numerator)
        return shares.map { it.copy(fraction = it.fraction.multiply(scaleFactor).normalized()) }
    }

    private fun applyRadd(shares: List<LocalHeirShare>): List<LocalHeirShare> {
        val total = FaraidhFraction.sumOf(shares.map { it.fraction })
        val remainder = FaraidhFraction(total.denominator - total.numerator, total.denominator)
        val eligible = shares.filter { !it.isAsabah }
        if (eligible.isEmpty() || remainder == FaraidhFraction.ZERO) return shares

        val unit = remainder.divideAmongHeads(eligible.sumOf { it.heads })
        return shares.map { share ->
            if (share.isAsabah) share
            else share.copy(fraction = share.fraction.add(unit.multiplyScalar(share.heads)).normalized())
        }
    }

    private fun buildNotes(
        context: LocalEngineContext,
        fixedShares: List<LocalHeirShare>,
        residueShares: List<LocalHeirShare>,
        finalShares: List<LocalHeirShare>
    ): List<String> {
        val notes = mutableListOf<String>()
        if (context.waladZina) notes += "Applied walad zina exclusion logic"
        if (context.hasJanin) notes += "Included janin conditional shares"
        if (fixedShares.isNotEmpty()) notes += "Fixed shares assigned locally"
        if (residueShares.isNotEmpty()) notes += "Residue assigned to asabah candidates"
        if (finalShares != fixedShares + residueShares) notes += "Applied Aul or Radd adjustment"
        return notes
    }

    private fun sisterType(type: RelativeType): RelativeType = when (type) {
        RelativeType.SON -> RelativeType.DAUGHTER
        RelativeType.GRANDSON -> RelativeType.GRANDDAUGHTER
        RelativeType.FULL_BROTHER -> RelativeType.FULL_SISTER
        RelativeType.PATERNAL_BROTHER -> RelativeType.PATERNAL_SISTER
        else -> type
    }

    private data class LocalEngineContext(
        val hasChildren: Boolean,
        val hasGrandchildren: Boolean,
        val hasFather: Boolean,
        val hasGrandfather: Boolean,
        val hasMother: Boolean,
        val hasSiblings: Boolean,
        val hasJanin: Boolean,
        val hasMafqud: Boolean,
        val simultaneousDeath: Boolean,
        val waladZina: Boolean,
        val ruleCitations: List<String> = emptyList()
    ) {
        fun hasTwoOrMoreSiblings() = hasSiblings && !hasChildren && !hasFather && !hasGrandfather
    }

    private data class AsabahGroup(
        val type: RelativeType,
        val maleHeads: Int,
        val femaleHeads: Int
    )
}
