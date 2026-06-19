package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

private fun BigInteger.lcm(other: BigInteger): BigInteger {
    if (this == BigInteger.ZERO || other == BigInteger.ZERO) return BigInteger.ZERO
    return this.multiply(other).abs().divide(this.gcd(other))
}

data class FaraidhFraction(
    val numerator: BigInteger,
    val denominator: BigInteger
) {
    init {
        require(denominator > BigInteger.ZERO) { "Denominator must be positive" }
    }

    fun normalized(): FaraidhFraction {
        val gcd = numerator.gcd(denominator)
        val den = if (denominator.signum() < 0) denominator.negate() else denominator
        val num = if (denominator.signum() < 0) numerator.negate() else numerator
        return FaraidhFraction(num / gcd, den / gcd)
    }

    fun add(other: FaraidhFraction): FaraidhFraction {
        val lcm = denominator.lcm(other.denominator)
        val a = numerator * (lcm / denominator)
        val b = other.numerator * (lcm / other.denominator)
        return FaraidhFraction(a + b, lcm).normalized()
    }

    fun multiplyScalar(count: Int): FaraidhFraction {
        if (count <= 0) return ZERO
        return FaraidhFraction(numerator * BigInteger.valueOf(count.toLong()), denominator).normalized()
    }

    fun divideAmongHeads(heads: Int): FaraidhFraction {
        if (heads <= 0) return ZERO
        return FaraidhFraction(numerator, denominator * BigInteger.valueOf(heads.toLong())).normalized()
    }

    fun toDecimal(scale: Int = 8): BigDecimal =
        BigDecimal(numerator).divide(BigDecimal(denominator), scale, RoundingMode.HALF_UP)

    fun toPercentage(scale: Int = 4): BigDecimal =
        toDecimal(scale + 2).multiply(BigDecimal(100)).setScale(scale, RoundingMode.HALF_UP)

    fun toDisplayString(): String {
        val n = normalized()
        return "${n.numerator}/${n.denominator}"
    }

    fun toCashAmount(estate: BigDecimal, scale: Int = 2): BigDecimal =
        estate.multiply(BigDecimal(numerator))
            .divide(BigDecimal(denominator), scale, RoundingMode.HALF_UP)

    companion object {
        val ZERO = FaraidhFraction(BigInteger.ZERO, BigInteger.ONE)
        val ONE = FaraidhFraction(BigInteger.ONE, BigInteger.ONE)

        fun of(n: Long, d: Long): FaraidhFraction = FaraidhFraction(
            BigInteger.valueOf(n),
            BigInteger.valueOf(d)
        ).normalized()

        fun sumOf(fractions: List<FaraidhFraction>): FaraidhFraction =
            fractions.fold(ZERO) { acc, f -> acc.add(f) }

        fun applyAwl(shares: List<Pair<HeirType, FaraidhFraction>>): List<Pair<HeirType, FaraidhFraction>> {
            val total = sumOf(shares.map { it.second })
            if (total.numerator <= total.denominator) return shares
            return shares.map { (type, frac) ->
                type to FaraidhFraction(
                    frac.numerator * total.denominator,
                    frac.denominator * total.numerator
                ).normalized()
            }
        }

        fun applyRadd(
            shares: List<Triple<HeirType, FaraidhFraction, Boolean>>,
            spouseTypes: Set<HeirType> = setOf(HeirType.HUSBAND, HeirType.WIFE)
        ): List<Pair<HeirType, FaraidhFraction>> {
            val total = sumOf(shares.map { it.second })
            if (total.numerator >= total.denominator) {
                return shares.map { it.first to it.second }
            }
            val surplus = FaraidhFraction(
                total.denominator - total.numerator,
                total.denominator
            )
            val eligible = shares.filter { (type, frac, _) ->
                type !in spouseTypes && frac.numerator > BigInteger.ZERO
            }
            if (eligible.isEmpty()) return shares.map { it.first to it.second }
            val eligibleTotal = sumOf(eligible.map { it.second })
            return shares.map { (type, frac, _) ->
                if (type in spouseTypes || frac.numerator == BigInteger.ZERO) {
                    type to frac
                } else {
                    val extraProp = FaraidhFraction(
                        frac.numerator * eligibleTotal.denominator,
                        frac.denominator * eligibleTotal.numerator
                    )
                    val extra = FaraidhFraction(
                        surplus.numerator * extraProp.numerator,
                        surplus.denominator * extraProp.denominator
                    ).normalized()
                    type to frac.add(extra).normalized()
                }
            }
        }
    }
}
