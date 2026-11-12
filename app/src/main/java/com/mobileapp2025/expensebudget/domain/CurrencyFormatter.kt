package com.mobileapp2025.expensebudget.domain

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Formats euro cents for display ("€12.50") and for edit fields ("12.50").
 */
object CurrencyFormatter {
    private const val CENTS_PER_EURO = 100L

    fun format(cents: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        formatter.currency = Currency.getInstance("EUR")
        return formatter.format(cents / CENTS_PER_EURO.toDouble())
    }

    fun toInputString(cents: Long): String =
        String.format(Locale.US, "%d.%02d", cents / CENTS_PER_EURO, cents % CENTS_PER_EURO)
}