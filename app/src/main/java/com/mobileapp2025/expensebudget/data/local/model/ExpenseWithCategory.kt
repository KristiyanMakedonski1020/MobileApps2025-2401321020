package com.mobileapp2025.expensebudget.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.local.entity.ExpenseEntity

/**
 * An expense together with its category, loaded in one query.
 */
data class ExpenseWithCategory(
    @Embedded val expense: ExpenseEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
)