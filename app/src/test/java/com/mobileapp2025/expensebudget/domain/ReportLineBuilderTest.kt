package com.mobileapp2025.expensebudget.domain

import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.local.entity.ExpenseEntity
import com.mobileapp2025.expensebudget.data.local.model.ExpenseWithCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportLineBuilderTest {

    private val food = CategoryEntity(id = 1, name = "Food", colorHex = "#4CAF50", monthlyBudgetCents = 10_000)
    private val transport = CategoryEntity(id = 2, name = "Transport", colorHex = "#2196F3")

    private fun expense(id: Long, category: CategoryEntity, cents: Long) =
        ExpenseWithCategory(
            expense = ExpenseEntity(
                id = id,
                amountCents = cents,
                categoryId = category.id,
                date = 0L,
                createdAt = 0L,
            ),
            category = category,
        )

    @Test
    fun `expenses of the same category are summed`() {
        val lines = ReportLineBuilder.fromExpenses(
            listOf(expense(1, food, 1_000), expense(2, food, 2_500)),
        )
        assertEquals(listOf(ReportLine("Food", 3_500, 10_000)), lines)
    }

    @Test
    fun `lines are sorted by spent amount`() {
        val lines = ReportLineBuilder.fromExpenses(
            listOf(expense(1, food, 1_000), expense(2, transport, 5_000)),
        )
        assertEquals(listOf("Transport", "Food"), lines.map { it.categoryName })
    }

    @Test
    fun `category without budget has null budget`() {
        val lines = ReportLineBuilder.fromExpenses(listOf(expense(1, transport, 1_000)))
        assertEquals(null, lines.single().budgetCents)
    }

    @Test
    fun `zero budget is treated as no budget`() {
        val zeroBudget = transport.copy(monthlyBudgetCents = 0)
        val lines = ReportLineBuilder.fromExpenses(listOf(expense(1, zeroBudget, 1_000)))
        assertEquals(null, lines.single().budgetCents)
    }

    @Test
    fun `no expenses gives no lines`() {
        assertTrue(ReportLineBuilder.fromExpenses(emptyList()).isEmpty())
    }
}