package app.kamy.saatApp.features.quran.tajweed

object ArabicCharUtil {
    const val FATHA = 0x064E
    const val DAMMA = 0x064F
    const val KASRA = 0x0650
    const val FATHATAN = 0x064B
    const val DAMMATAN = 0x064C
    const val KASRATAN = 0x064D
    const val SUKUN = 0x0652
    const val SHADDA = 0x0651
    const val NUN = 0x0646
    const val MIM = 0x0645
    const val BAA = 0x0628
    const val QAAF = 0x0642
    const val THAA = 0x0637
    const val JIMM = 0x062C
    const val DAAL = 0x062F
    const val YAA = 0x064A
    const val WAW = 0x0648
    const val RAA = 0x0631
    const val LAAM = 0x0644
    
    // Rasm Usmani Specific
    const val SMALL_MEEM = 0x06ED

    fun getCodePointAt(text: String, index: Int): Int {
        if (index < 0 || index >= text.length) return -1
        return text.codePointAt(index)
    }

    fun isTanwin(codePoint: Int): Boolean {
        return codePoint == FATHATAN || codePoint == DAMMATAN || codePoint == KASRATAN
    }

    fun isNunSukunOrTanwin(text: String, index: Int): Boolean {
        val cp = getCodePointAt(text, index)
        if (isTanwin(cp)) return true
        if (cp == NUN) {
            val nextCp = getCodePointAt(text, index + 1)
            // Kemenag uses Sukun, or empty for Ikhfa/Idgham, or small meem for Iqlab
            if (nextCp == SUKUN || isHarf(nextCp) || nextCp == SMALL_MEEM || nextCp == 0x20 || nextCp == SHADDA) {
                return true
            }
        }
        return false
    }

    fun isMimSukun(text: String, index: Int): Boolean {
        if (getCodePointAt(text, index) == MIM) {
            val nextCp = getCodePointAt(text, index + 1)
            if (nextCp == SUKUN || isHarf(nextCp) || nextCp == 0x20) {
                return true
            }
        }
        return false
    }

    fun isHarf(codePoint: Int): Boolean {
        return codePoint in 0x0621..0x064A
    }

    fun isCombiningMark(codePoint: Int): Boolean {
        return codePoint in 0x064B..0x0652 ||
            codePoint == 0x0670 ||
            codePoint in 0x06D6..0x06ED ||
            codePoint == 0x06E3 ||
            codePoint == 0x06E5 ||
            codePoint == 0x06E6 ||
            codePoint == 0x06EA ||
            codePoint == 0x06EB ||
            codePoint == 0x06EC ||
            codePoint == 0x06DF ||
            codePoint == 0x06E0 ||
            codePoint == 0x06E1
    }
    
    fun getNextHarfIndex(text: String, startIndex: Int): Int {
        var index = startIndex
        while (index < text.length) {
            if (isHarf(getCodePointAt(text, index))) {
                return index
            }
            index++
        }
        return -1
    }
}
