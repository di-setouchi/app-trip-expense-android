package com.disetouchi.tripexpense.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale

/**
 * Utility object for parsing, calculating, and formatting amounts.
 */
object AmountUtil {

    /**
     * Gets the default fraction digits for a specific currency (e.g., USD -> 2, JPY -> 0).
     */
    fun fractionDigits(currencyCode: String): Int {
        return runCatching { Currency.getInstance(currencyCode).defaultFractionDigits }
            .getOrDefault(2)
            .coerceAtLeast(0)
    }

    /**
     * Parses user input text into a minor unit (e.g., "12.50" USD -> 1250L).
     */
    fun parseToMinor(amountText: String, currencyCode: String): Long? {
        val bd = amountText.toBigDecimalOrNull() ?: return null
        val digits = fractionDigits(currencyCode)
        val factor = BigDecimal.TEN.pow(digits)
        return bd.multiply(factor).setScale(0, RoundingMode.HALF_UP).toLong()
    }

    /**
     * Computes the base currency minor amount from a local minor amount and exchange rate.
     */
    fun computeBaseMinor(
        localAmountMinor: Long,
        localCurrencyCode: String,
        baseCurrencyCode: String,
        rateUsedMicros: Long
    ): Long {
        val localDigits = fractionDigits(localCurrencyCode)
        val baseDigits = fractionDigits(baseCurrencyCode)

        val localMinorBd = BigDecimal.valueOf(localAmountMinor)
        val rateBd = BigDecimal.valueOf(rateUsedMicros)
            .divide(BigDecimal.valueOf(1_000_000L), 12, RoundingMode.HALF_UP)

        val diff = baseDigits - localDigits

        val scaled = if (diff >= 0) {
            val factor = BigDecimal.TEN.pow(diff)
            localMinorBd.multiply(rateBd).multiply(factor)
        } else {
            val factor = BigDecimal.TEN.pow(-diff)
            localMinorBd.multiply(rateBd).divide(factor, 12, RoundingMode.HALF_UP)
        }

        return scaled.setScale(0, RoundingMode.HALF_UP).toLong()
    }

    /**
     * Converts a minor amount to a plain string representation without commas.
     * Used for populating input text fields.
     */
    fun minorToPlainString(amountMinor: Long, currencyCode: String): String {
        val digits = fractionDigits(currencyCode)
        val divisor = BigDecimal.TEN.pow(digits)
        val major = BigDecimal.valueOf(amountMinor).divide(divisor, digits, RoundingMode.HALF_UP)
        return major.toPlainString()
    }

    /**
     * Formats a minor amount into a string with commas but no currency symbol (e.g., "1,250.00").
     */
    fun formatMinorAmount(amountMinor: Long, currencyCode: String): String {
        val digits = fractionDigits(currencyCode)
        val divisor = BigDecimal.TEN.pow(digits)
        val major = BigDecimal.valueOf(amountMinor).divide(divisor, digits, RoundingMode.HALF_UP)
        return if (digits <= 0) {
            String.format(Locale.getDefault(), "%,.0f", major)
        } else {
            String.format(Locale.getDefault(), "%,.${digits}f", major)
        }
    }

    /**
     * Formats a minor amount with its currency symbol (e.g., "$1,250.00").
     */
    fun formatMinorAmountWithSymbol(amountMinor: Long, currencyCode: String): String {
        val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
        val symbol = currency?.symbol ?: currencyCode
        return "$symbol${formatMinorAmount(amountMinor, currencyCode)}"
    }

    /**
     * Formats an exchange rate stored in micros (e.g., 1,324,500L -> "1.3245").
     * Guaranteed to show the exact stored value without redundant trailing zeros.
     */
    fun formatRateMicros(rateUsedMicros: Long): String {
        return BigDecimal.valueOf(rateUsedMicros)
            .movePointLeft(6)
            .stripTrailingZeros()
            .toPlainString()
    }
}
