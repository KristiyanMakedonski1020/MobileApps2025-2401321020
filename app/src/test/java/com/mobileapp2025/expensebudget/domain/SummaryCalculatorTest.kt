package com.mobileapp2025.expensebudget.domain

import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.local.model.CategoryTotal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryCalculatorTest {

    private fun category(id: Long, name: String, budget: Long? = null) =
        CategoryEntity(id = id, name = name, colorHex = "#4CAF50", monthlyBudgetCents = budget)

    @Test
    fun `percent of total is computed`() {
        val items = SummaryCalculator.build(
            listOf(category(1, "Food"), category(2, "Transport")),
            listOf(CategoryTotal(1, 7_500), CategoryTotal(2, 2_500)),
        )
        assertEquals(75, items[0].percentOfTotal)
        assertEquals(25, items[1].percentOfTotal)
    }

    @Test
    fun `rows are sorted by spent amount`() {
        val items = SummaryCalculator.build(
            listOf(category(1, "Food"), category(2, "Transport"), category(3, "Bills")),
            listOf(CategoryTotal(1, 1_000), CategoryTotal(2, 5_000), CategoryTotal(3, 3_000)),
        )
        assertEquals(listOf("Transport", "Bills", "Food"), items.map { it.name })
    }

    @Test
    fun `category without expenses and budget is hidden`() {
        val items = SummaryCalculator.build(
            listOf(category(1, "Food"), category(2, "Health")),
            listOf(CategoryTotal(1, 1_000)),
        )
        assertEquals(listOf("Food"), items.map { it.name })
    }

    @Test
    fun `category with budget but no expenses is shown`() {
        val items = SummaryCalculator.build(
            listOf(category(1, "Food", budget = 10_000)),
            emptyList(),
        )
        assertEquals(1, items.size)
        assertEquals(0L, items[0].spentCents)
        assertEquals(0, items[0].budgetPercent)
        assertEquals(BudgetStatus.OK, items[0].status)
    }

    @Test
    fun `budget status and percent are computed`() {
        val items = SummaryCalculator.build(
            listOf(category(1, "Food", budget = 10_000), category(2, "Transport", budget = 2_000)),
            listOf(CategoryTotal(1, 7_500), CategoryTotal(2, 2_500)),
        )
        val food = items.first { it.name == "Food" }
        val transport = items.first { it.name == "Transport" }
        assertEquals(75, food.budgetPercent)
        assertEquals(BudgetStatus.OK, food.status)
        assertEquals(125, transport.budgetPercent)
        assertEquals(BudgetStatus.EXCEEDED, transport.status)
    }

    @Test
    fun `category without budget has no budget percent`() {
        val items = SummaryCalculator.build(
            listOf(category(1, "Food")),
            listOf(CategoryTotal(1, 1_000)),
        )
        assertNull(items[0].budgetPercent)
        assertEquals(BudgetStatus.NO_BUDGET, items[0].status)
    }

    @Test
    fun `no data gives an empty list`() {
        val items = SummaryCalculator.build(listOf(category(1, "Food")), emptyList())
        assertTrue(items.isEmpty())
    }
}