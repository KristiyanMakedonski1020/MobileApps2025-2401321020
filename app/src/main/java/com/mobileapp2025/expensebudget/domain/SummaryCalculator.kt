package com.mobileapp2025.expensebudget.domain

import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.local.model.CategoryTotal

/** One row of the summary screen. */
data class SummaryItem(
    val categoryId: Long,
    val name: String,
    val colorHex: String,
    val spentCents: Long,
    val percentOfTotal: Int,
    val budgetCents: Long?,
    val budgetPercent: Int?,
    val status: BudgetStatus,
)

/**
 * Builds the summary rows of one month. A category is listed when it has expenses
 * or a budget. Rows are sorted by the spent amount, largest first.
 */
object SummaryCalculator {
    private const val PERCENT = 100L

    fun build(categories: List<CategoryEntity>, totals: List<CategoryTotal>): List<SummaryItem> {
        val spentByCategory = totals.associate { it.categoryId to it.totalCents }
        val grandTotal = spentByCategory.values.sum()

        return categories
            .map { category ->
                val spent = spentByCategory[category.id] ?: 0L
                val budget = category.monthlyBudgetCents?.takeIf { it > 0 }
                SummaryItem(
                    categoryId = category.id,
                    name = category.name,
                    colorHex = category.colorHex,
                    spentCents = spent,
                    percentOfTotal = if (grandTotal > 0) (spent * PERCENT / grandTotal).toInt() else 0,
                    budgetCents = budget,
                    budgetPercent = BudgetCalculator.percent(spent, budget),
                    status = BudgetCalculator.status(spent, budget),
                )
            }
            .filter { it.spentCents > 0 || it.budgetCents != null }
            .sortedWith(
                compareByDescending<SummaryItem> { it.spentCents }.thenBy { it.name.lowercase() },
            )
    }
}