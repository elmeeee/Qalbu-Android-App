package app.kamy.saatApp.domain.tools

import java.text.NumberFormat
import java.util.Locale

object ZakatNumberFormatter {

    fun parseDecimal(raw: String): Double {
        if (raw.isBlank()) return 0.0
        val cleaned = raw.replace(" ", "").replace(".", "").replace(",", ".")
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    fun parseMoney(raw: String): Double {
        if (raw.isBlank()) return 0.0
        val digits = raw.filter { it.isDigit() }
        return digits.toDoubleOrNull() ?: 0.0
    }

    fun formatMoneyInput(input: String, isIndonesian: Boolean = true): String {
        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        val number = digits.toLongOrNull() ?: return digits
        return if (isIndonesian) {
            val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(number)
            formatted
        } else {
            val formatted = NumberFormat.getNumberInstance(Locale.US).format(number)
            formatted
        }
    }

    fun formatDecimalInput(input: String, isIndonesian: Boolean = true): String {
        if (input.isBlank()) return ""
        val separatorIndex = input.indexOfAny(charArrayOf('.', ','))
        val intPartRaw = if (separatorIndex >= 0) input.substring(0, separatorIndex) else input
        val decPartRaw = if (separatorIndex >= 0) input.substring(separatorIndex + 1) else null

        val intDigits = intPartRaw.filter { it.isDigit() }
        val decDigits = decPartRaw?.filter { it.isDigit() }

        if (intDigits.isEmpty() && decDigits == null) return ""

        val intNumber = intDigits.toLongOrNull() ?: 0L
        val formattedInt = if (isIndonesian) {
            NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(intNumber)
        } else {
            NumberFormat.getNumberInstance(Locale.US).format(intNumber)
        }

        return if (decDigits != null) {
            val sepChar = if (isIndonesian) "," else "."
            "$formattedInt$sepChar$decDigits"
        } else {
            formattedInt
        }
    }

    fun formatCurrency(amount: Double, currencySymbol: String = "Rp"): String {
        val rounded = kotlin.math.round(amount).toLong()
        val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(rounded)
        return "$currencySymbol $formatted"
    }
}
