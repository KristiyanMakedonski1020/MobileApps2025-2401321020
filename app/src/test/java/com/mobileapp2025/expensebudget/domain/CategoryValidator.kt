package com.mobileapp2025.expensebudget.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryValidatorTest {

    private val others = listOf("Food", "Transport")

    @Test
    fun `valid name and budget`() =
        assertEquals(
            CategoryValidation.Valid("Pets", 5_000),
            CategoryValidator.validate("Pets", "50", others),
        )

    @Test
    fun `empty budget means no budget`() =
        assertEquals(
            CategoryValidation.Valid("Pets", null),
            CategoryValidator.validate("Pets", "  ", others),
        )

    @Test
    fun `name is trimmed`() =
        assertEquals(
            CategoryValidation.Valid("Pets", null),
            CategoryValidator.validate("  Pets  ", "", others),
        )

    @Test
    fun `empty name is rejected`() =
        assertEquals(
            CategoryValidation.Invalid(CategoryNameError.EMPTY, null),
            CategoryValidator.validate("   ", "", others),
        )

    @Test
    fun `too long name is rejected`() =
        assertEquals(
            CategoryValidation.Invalid(CategoryNameError.TOO_LONG, null),
            CategoryValidator.validate("a".repeat(31), "", others),
        )

    @Test
    fun `duplicate name is rejected ignoring case`() =
        assertEquals(
            CategoryValidation.Invalid(CategoryNameError.DUPLICATE, null),
            CategoryValidator.validate("fOOd", "", others),
        )

    @Test
    fun `invalid budget is rejected`() =
        assertEquals(
            CategoryValidation.Invalid(null, AmountError.NOT_A_NUMBER),
            CategoryValidator.validate("Pets", "abc", others),
        )

    @Test
    fun `zero budget is rejected`() =
        assertEquals(
            CategoryValidation.Invalid(null, AmountError.NOT_POSITIVE),
            CategoryValidator.validate("Pets", "0", others),
        )

    @Test
    fun `both errors are reported together`() =
        assertEquals(
            CategoryValidation.Invalid(CategoryNameError.EMPTY, AmountError.TOO_MANY_DECIMALS),
            CategoryValidator.validate("", "1.234", others),
        )
}