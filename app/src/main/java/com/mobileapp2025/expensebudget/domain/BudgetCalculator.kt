package com.mobileapp2025.expensebudget.domain

/** How much of a budget has been used. */
enum class BudgetStatus { NO_BUDGET, OK, NEAR, EXCEEDED }

/**
 * Budget math. NEAR starts at 80% of the budget, EXCEEDED means strictly more than 100%.
 */
object BudgetCalculator {
    private const val PERCENT = 100L
    private const val NEAR_THRESHOLD_PERCENT = 80L

    fun percent(spentCents: Long, budgetCents: Long?): Int? =
        if (budgetCents == null || budgetCents <= 0) {
            null
        } else {
            (spentCents * PERCENT / budgetCents).toInt()
        }

    fun status(spentCents: Long, budgetCents: Long?): BudgetStatus = when {
        budgetCents == null || budgetCents <= 0 -> BudgetStatus.NO_BUDGET
        spentCents > budgetCents -> BudgetStatus.EXCEEDED
        spentCents * PERCENT >= budgetCents * NEAR_THRESHOLD_PERCENT -> BudgetStatus.NEAR
        else -> BudgetStatus.OK
    }
}