package com.mobileapp2025.expensebudget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One expense category (table "categories").
 * [monthlyBudgetCents] is null when the category has no budget.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val monthlyBudgetCents: Long? = null,
)