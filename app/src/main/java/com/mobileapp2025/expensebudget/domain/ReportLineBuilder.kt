package com.mobileapp2025.expensebudget.domain

import com.mobileapp2025.expensebudget.data.local.model.ExpenseWithCategory

/**
 * Turns the expenses of a month into the category rows of the shared report.
 * Rows are sorted by the spent amount, largest first.
 */
object ReportLineBuilder {

    fun fromExpenses(expenses: List<ExpenseWithCategory>): List<ReportLine> =
        expenses
            .groupBy { it.category.id }
            .values
            .map { items ->
                val category = items.first().category
                ReportLine(
                    categoryName = category.name,
                    spentCents = items.sumOf { it.expense.amountCents },
                    budgetCents = category.monthlyBudgetCents?.takeIf { it > 0 },
                )
            }
            .sortedWith(
                compareByDescending<ReportLine> { it.spentCents }
                    .thenBy { it.categoryName.lowercase() },
            )
}