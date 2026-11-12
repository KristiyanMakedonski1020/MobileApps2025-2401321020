package com.mobileapp2025.expensebudget

import android.content.Context
import com.mobileapp2025.expensebudget.data.local.AppDatabase
import com.mobileapp2025.expensebudget.data.repository.CategoryRepository
import com.mobileapp2025.expensebudget.data.repository.ExpenseRepository

/**
 * Manual dependency container: builds the database and repositories once.
 */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val expenseRepository = ExpenseRepository(database.expenseDao())

    val categoryRepository = CategoryRepository(
        database.categoryDao(),
        database.expenseDao(),
    )
}