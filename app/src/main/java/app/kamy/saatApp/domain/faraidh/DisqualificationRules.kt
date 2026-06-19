package app.kamy.saatApp.domain.faraidh

object DisqualificationRules {

    fun evaluate(relative: RelativeInput, context: LocalEngineContext): List<BlockReason> {
        val reasons = mutableListOf<BlockReason>()
        if (!relative.isMuslim) reasons += BlockReason.OUT_OF_WEDLOCK
        if (relative.isMurderer) reasons += BlockReason.MURDER
        if (relative.isMafqud) reasons += BlockReason.MAFQUD
        if (relative.isSimultaneousDeath && context.simultaneousDeath) reasons += BlockReason.SIMULTANEOUS_DEATH
        if (relative.type == RelativeType.GRANDFATHER && context.hasFather) reasons += BlockReason.BY_FATHER
        if (relative.type == RelativeType.FATHER && context.hasChildren) reasons += BlockReason.BY_SON
        if (relative.type in setOf(RelativeType.FULL_BROTHER, RelativeType.FULL_SISTER) && context.hasGrandfather && !context.hasFather && !context.hasChildren) reasons += BlockReason.BY_GRANDFATHER
        return reasons
    }

    fun shouldFreezeShare(relative: RelativeInput, context: LocalEngineContext): Boolean {
        return relative.isMafqud || relative.isSimultaneousDeath || !relative.isMuslim || relative.isMurderer
    }
}
