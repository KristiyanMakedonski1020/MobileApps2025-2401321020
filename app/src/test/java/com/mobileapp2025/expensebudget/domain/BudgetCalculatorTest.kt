package com.mobileapp2025.expensebudget.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetCalculatorTest {

    private val budget = 10_000L

    @Test
    fun `no budget gives NO_BUDGET`() =
        assertEquals(BudgetStatus.NO_BUDGET, BudgetCalculator.status(5_000, null))

    @Test
    fun `zero budget gives NO_BUDGET`() =
        assertEquals(BudgetStatus.NO_BUDGET, BudgetCalculator.status(5_000, 0))

    @Test
    fun `79 percent is OK`() =
        assertEquals(BudgetStatus.OK, BudgetCalculator.status(7_999, budget))

    @Test
    fun `exactly 80 percent is NEAR`() =
        assertEquals(BudgetStatus.NEAR, BudgetCalculator.status(8_000, budget))

    @Test
    fun `exactly 100 percent is still NEAR`() =
        assertEquals(BudgetStatus.NEAR, BudgetCalculator.status(10_000, budget))

    @Test
    fun `over 100 percent is EXCEEDED`() =
        assertEquals(BudgetStatus.EXCEEDED, BudgetCalculator.status(10_001, budget))

    @Test
    fun `percent is computed`() =
        assertEquals(50, BudgetCalculator.percent(5_000, budget))

    @Test
    fun `percent is null without budget`() =
        assertNull(BudgetCalculator.percent(5_000, null))
}