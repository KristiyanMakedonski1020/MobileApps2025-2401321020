package com.mobileapp2025.expensebudget.data.local.model

/**
 * The sum of all expenses of one category in a period.
 */
data class CategoryTotal(
    val categoryId: Long,
    val totalCents: Long,
)