package com.mobileapp2025.expensebudget.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun `formats cents as euro`() = assertEquals("€12.50", CurrencyFormatter.format(1250))

    @Test
    fun `formats small amount`() = assertEquals("€0.05", CurrencyFormatter.format(5))

    @Test
    fun `input string has two decimals`() =
        assertEquals("12.50", CurrencyFormatter.toInputString(1250))

    @Test
    fun `input string pads cents`() =
        assertEquals("0.05", CurrencyFormatter.toInputString(5))
}