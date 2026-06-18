package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object MoneyInputFormatter {

    private val displayFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
        isGroupingUsed = true
    }

    fun digitsOnly(input: String): String = input.filter { it.isDigit() }

    fun format(input: String): String {
        val digits = digitsOnly(input)
        if (digits.isEmpty()) return ""
        return runCatching {
            displayFormat.format(digits.toLong())
        }.getOrElse { digits }
    }

    fun parseAmount(raw: String): BigDecimal =
        digitsOnly(raw).toBigDecimalOrNull() ?: BigDecimal.ZERO
}
