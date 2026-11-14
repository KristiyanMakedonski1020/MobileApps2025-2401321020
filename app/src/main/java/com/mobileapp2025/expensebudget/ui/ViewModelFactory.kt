package com.mobileapp2025.expensebudget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.mobileapp2025.expensebudget.AppContainer
import com.mobileapp2025.expensebudget.ui.addedit.AddEditExpenseViewModel
import com.mobileapp2025.expensebudget.ui.categories.CategoriesViewModel
import com.mobileapp2025.expensebudget.ui.expenses.ExpensesViewModel
import com.mobileapp2025.expensebudget.ui.summary.SummaryViewModel

/**
 * Creates every ViewModel of the app with the repositories from [AppContainer].
 */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val handle = extras.createSavedStateHandle()
        return when {
            modelClass.isAssignableFrom(ExpensesViewModel::class.java) ->
                ExpensesViewModel(
                    container.expenseRepository,
                    container.categoryRepository,
                    handle,
                )
            modelClass.isAssignableFrom(AddEditExpenseViewModel::class.java) ->
                AddEditExpenseViewModel(
                    container.expenseRepository,
                    container.categoryRepository,
                    handle,
                )
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) ->
                CategoriesViewModel(container.categoryRepository)
            modelClass.isAssignableFrom(SummaryViewModel::class.java) ->
                SummaryViewModel(
                    container.expenseRepository,
                    container.categoryRepository,
                    handle,
                )
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        } as T
    }
}