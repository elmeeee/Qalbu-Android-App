package app.kamy.saatApp.features.quran.tajweed

import app.kamy.saatApp.features.quran.tajweed.ArabicCharUtil.getCodePointAt
import app.kamy.saatApp.features.quran.tajweed.ArabicCharUtil.getNextHarfIndex

interface TajweedRule {
    fun findTajweed(text: String): List<TajweedResult>
}

class IkhfaRule : TajweedRule {
    private val letters = listOf(0x062A, 0x062B, 0x062C, 0x062F, 0x0630, 0x0632, 0x0633, 0x0634, 0x0635, 0x0636, 0x0637, 0x0638, 0x0641, 0x0642, 0x0643)
    override fun findTajweed(text: String): List<TajweedResult> {
        val results = mutableListOf<TajweedResult>()
        for (i in text.indices) {
            if (ArabicCharUtil.isNunSukunOrTanwin(text, i)) {
                val nextHarfIndex = getNextHarfIndex(text, i + 1)
                if (nextHarfIndex != -1 && getCodePointAt(text, nextHarfIndex) in letters) {
                    results.add(TajweedResult(TajweedType.IKHFA, i, nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

class IdghamBighunnahRule : TajweedRule {
    private val letters = listOf(ArabicCharUtil.YAA, ArabicCharUtil.NUN, ArabicCharUtil.MIM, ArabicCharUtil.WAW)
    override fun findTajweed(text: String): List<TajweedResult> {
        val results = mutableListOf<TajweedResult>()
        for (i in text.indices) {
            if (ArabicCharUtil.isNunSukunOrTanwin(text, i)) {
                val nextHarfIndex = getNextHarfIndex(text, i + 1)
                if (nextHarfIndex != -1 && getCodePointAt(text, nextHarfIndex) in letters) {
                    results.add(TajweedResult(TajweedType.IDGHAM_WITH_GHUNNA, i, nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

class IdghamBilaghunnahRule : TajweedRule {
    private val letters = listOf(ArabicCharUtil.LAAM, ArabicCharUtil.RAA)
    override fun findTajweed(text: String): List<TajweedResult> {
        val results = mutableListOf<TajweedResult>()
        for (i in text.indices) {
            if (ArabicCharUtil.isNunSukunOrTanwin(text, i)) {
                val nextHarfIndex = getNextHarfIndex(text, i + 1)
                if (nextHarfIndex != -1 && getCodePointAt(text, nextHarfIndex) in letters) {
                    results.add(TajweedResult(TajweedType.IDGHAM_WITHOUT_GHUNNA, i, nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

class IqlabRule : TajweedRule {
    private val letters = listOf(ArabicCharUtil.BAA)
    override fun findTajweed(text: String): List<TajweedResult> {
        val results = mutableListOf<TajweedResult>()
        for (i in text.indices) {
            if (ArabicCharUtil.isNunSukunOrTanwin(text, i) || getCodePointAt(text, i) == ArabicCharUtil.SMALL_MEEM) {
                val nextHarfIndex = getNextHarfIndex(text, i + 1)
                if (nextHarfIndex != -1 && getCodePointAt(text, nextHarfIndex) in letters) {
                    results.add(TajweedResult(TajweedType.IQLAB, i, nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

class IkhfaSyafawiRule : TajweedRule {
    private val letters = listOf(ArabicCharUtil.BAA)
    override fun findTajweed(text: String): List<TajweedResult> {
        val results = mutableListOf<TajweedResult>()
        for (i in text.indices) {
            if (ArabicCharUtil.isMimSukun(text, i)) {
                val nextHarfIndex = getNextHarfIndex(text, i + 1)
                if (nextHarfIndex != -1 && getCodePointAt(text, nextHarfIndex) in letters) {
                    results.add(TajweedResult(TajweedType.IKHFA_SYAFAWI, i, nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

class IdghamMimiRule : TajweedRule {
    private val letters = listOf(ArabicCharUtil.MIM)
    override fun findTajweed(text: String): List<TajweedResult> {
        val results = mutableListOf<TajweedResult>()
        for (i in text.indices) {
            if (ArabicCharUtil.isMimSukun(text, i)) {
                val nextHarfIndex = getNextHarfIndex(text, i + 1)
                if (nextHarfIndex != -1 && getCodePointAt(text, nextHarfIndex) in letters) {
                    results.add(TajweedResult(TajweedType.IDGHAM_MIMI, i, nextHarfIndex + 1))
                }
            }
        }
        return results
    }
}

class GhunnahRule : TajweedRule {
    override fun findTajweed(text: String): List<TajweedResult> {
        val results = mutableListOf<TajweedResult>()
        for (i in text.indices) {
            val cp = getCodePointAt(text, i)
            if (cp == ArabicCharUtil.NUN || cp == ArabicCharUtil.MIM) {
                // Check if shadda is next or next next (skipping diacritics)
                var nextIndex = i + 1
                var hasShadda = false
                while (nextIndex < text.length && !ArabicCharUtil.isHarf(getCodePointAt(text, nextIndex))) {
                    if (getCodePointAt(text, nextIndex) == ArabicCharUtil.SHADDA) {
                        hasShadda = true
                        break
                    }
                    nextIndex++
                }
                if (hasShadda) {
                    results.add(TajweedResult(TajweedType.GHUNNA, i, nextIndex + 1))
                }
            }
        }
        return results
    }
}

class QalqalahRule : TajweedRule {
    private val letters = listOf(ArabicCharUtil.QAAF, ArabicCharUtil.THAA, ArabicCharUtil.BAA, ArabicCharUtil.JIMM, ArabicCharUtil.DAAL)
    override fun findTajweed(text: String): List<TajweedResult> {
        val results = mutableListOf<TajweedResult>()
        for (i in text.indices) {
            if (getCodePointAt(text, i) in letters) {
                val nextCp = getCodePointAt(text, i + 1)
                if (nextCp == ArabicCharUtil.SUKUN || nextCp == -1 || nextCp == 0x20) {
                    results.add(TajweedResult(TajweedType.QALQALAH, i, i + 2))
                }
            }
        }
        return results
    }
}
