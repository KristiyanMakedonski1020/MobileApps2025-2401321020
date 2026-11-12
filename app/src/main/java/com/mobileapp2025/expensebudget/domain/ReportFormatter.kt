package com.mobileapp2025.expensebudget.domain

import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** One category row of the shared report. */
data class ReportLine(
    val categoryName: String,
    val spentCents: Long,
    val budgetCents: Long?,
)

/**
 * Builds the plain-text monthly report that is shared through the Share Intent.
 */
object ReportFormatter {

    fun format(month: YearMonth, totalCents: Long, lines: List<ReportLine>): String {
        val monthName = month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        return buildString {
            appendLine("Expense report - $monthName ${month.year}")
            appendLine("Total: ${CurrencyFormatter.format(totalCents)}")
            appendLine()
            lines.forEach { line ->
                append("- ${line.categoryName}: ${CurrencyFormatter.format(line.spentCents)}")
                if (line.budgetCents != null) {
                    append(" / ${CurrencyFormatter.format(line.budgetCents)}")
                }
                if (BudgetCalculator.status(line.spentCents, line.budgetCents) == BudgetStatus.EXCEEDED) {
                    append(" [OVER BUDGET]")
                }
                appendLine()
            }
        }.trimEnd()
    }
}