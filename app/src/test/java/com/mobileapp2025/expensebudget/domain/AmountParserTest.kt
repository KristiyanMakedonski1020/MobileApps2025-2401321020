package com.mobileapp2025.expensebudget.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountParserTest {

    private fun assertInvalid(input: String, expected: AmountError) =
        assertEquals(AmountResult.Invalid(expected), AmountParser.parse(input))

    @Test
    fun `comma decimal is parsed`() =
        assertEquals(AmountResult.Valid(1250), AmountParser.parse("12,50"))

    @Test
    fun `dot decimal with one digit is parsed`() =
        assertEquals(AmountResult.Valid(1250), AmountParser.parse("12.5"))

    @Test
    fun `whole number is parsed`() =
        assertEquals(AmountResult.Valid(700), AmountParser.parse(" 7 "))

    @Test
    fun `empty input is rejected`() = assertInvalid("", AmountError.EMPTY)

    @Test
    fun `blank input is rejected`() = assertInvalid("   ", AmountError.EMPTY)

    @Test
    fun `zero is rejected`() = assertInvalid("0", AmountError.NOT_POSITIVE)

    @Test
    fun `negative is rejected`() = assertInvalid("-5", AmountError.NOT_POSITIVE)

    @Test
    fun `letters are rejected`() = assertInvalid("abc", AmountError.NOT_A_NUMBER)

    @Test
    fun `three decimals are rejected`() = assertInvalid("1.234", AmountError.TOO_MANY_DECIMALS)

    @Test
    fun `huge number is rejected`() = assertInvalid("9999999999", AmountError.TOO_LARGE)
}