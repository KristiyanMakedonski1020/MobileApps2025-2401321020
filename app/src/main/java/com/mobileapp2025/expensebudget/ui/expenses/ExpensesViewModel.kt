package com.mobileapp2025.expensebudget.ui.expenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.local.model.ExpenseWithCategory
import com.mobileapp2025.expensebudget.data.repository.CategoryRepository
import com.mobileapp2025.expensebudget.data.repository.ExpenseRepository
import com.mobileapp2025.expensebudget.domain.MonthRange
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import com.mobileapp2025.expensebudget.data.local.entity.ExpenseEntity
import kotlinx.coroutines.launch

/**
 * Everything the expenses screen needs to draw itself.
 * [budgetCents] is the sum of all category budgets (null if no category has one);
 * [budgetSpentCents] is the amount spent in categories that have a budget.
 */
data class ExpensesUiState(
    val month: YearMonth,
    val expenses: List<ExpenseWithCategory> = emptyList(),
    val totalCents: Long = 0L,
    val budgetCents: Long? = null,
    val budgetSpentCents: Long = 0L,
)

/**
 * Holds the selected month (survives screen rotation through [SavedStateHandle])
 * and exposes the expenses of that month as a [StateFlow].
 */
class ExpensesViewModel(
    private val expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val selectedMonth: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_MONTH, YearMonth.now().toString())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExpensesUiState> = selectedMonth
        .flatMapLatest { monthText ->
            val month = YearMonth.parse(monthText)
            val range = MonthRange.of(month)
            combine(
                expenseRepository.observeExpenses(range.startMillis, range.endMillis),
                categoryRepository.observeAll(),
            ) { expenses, categories -> buildState(month, expenses, categories) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = ExpensesUiState(month = YearMonth.parse(selectedMonth.value)),
        )

    fun previousMonth() = changeMonth(-1)

    fun nextMonth() = changeMonth(1)

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { expenseRepository.delete(expense) }
    }

    /** Puts a just-deleted expense back (the Undo action). It keeps its old id. */
    fun restoreExpense(expense: ExpenseEntity) {
        viewModelScope.launch { expenseRepository.insert(expense) }
    }

    private fun changeMonth(delta: Long) {
        val current = YearMonth.parse(selectedMonth.value)
        savedStateHandle[KEY_MONTH] = current.plusMonths(delta).toString()
    }

    private fun buildState(
        month: YearMonth,
        expenses: List<ExpenseWithCategory>,
        categories: List<CategoryEntity>,
    ): ExpensesUiState {
        val budgets = categories.mapNotNull { it.monthlyBudgetCents }.filter { it > 0 }
        return ExpensesUiState(
            month = month,
            expenses = expenses,
            totalCents = expenses.sumOf { it.expense.amountCents },
            budgetCents = budgets.takeIf { it.isNotEmpty() }?.sum(),
            budgetSpentCents = expenses
                .filter { (it.category.monthlyBudgetCents ?: 0L) > 0 }
                .sumOf { it.expense.amountCents },
        )
    }

    private companion object {
        const val KEY_MONTH = "selected_month"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}