package app.kamy.saatApp.domain.tools

import java.text.NumberFormat
import java.util.Locale

object ZakatNumberFormatter {

    fun parseDecimal(raw: String): Double {
        if (raw.isBlank()) return 0.0
        val cleaned = raw.replace(" ", "").replace(",", ".")
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

    fun formatDecimalInput(input: String): String {
        // Allows digits and at most one decimal point (. or ,)
        val cleaned = StringBuilder()
        var hasDecimal = false
        for (ch in input) {
            if (ch.isDigit()) {
                cleaned.append(ch)
            } else if ((ch == '.' || ch == ',') && !hasDecimal) {
                cleaned.append(ch)
                hasDecimal = true
            }
        }
        return cleaned.toString()
    }

    fun formatCurrency(amount: Double, isMalay: Boolean = false, isIndo: Boolean = true): String {
        val rounded = kotlin.math.round(amount).toLong()
        return if (isMalay) {
            val formatted = NumberFormat.getNumberInstance(Locale.US).format(rounded)
            "RM $formatted"
        } else if (isIndo) {
            val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(rounded)
            "Rp $formatted"
        } else {
            val formatted = NumberFormat.getNumberInstance(Locale.US).format(rounded)
            "$ $formatted"
        }
    }
}
