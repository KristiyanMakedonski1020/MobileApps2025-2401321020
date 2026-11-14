package com.mobileapp2025.expensebudget.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileapp2025.expensebudget.data.repository.CategoryRepository
import com.mobileapp2025.expensebudget.data.repository.ExpenseRepository
import com.mobileapp2025.expensebudget.domain.MonthRange
import com.mobileapp2025.expensebudget.domain.SummaryCalculator
import com.mobileapp2025.expensebudget.domain.SummaryItem
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/** Everything the summary screen needs to draw itself. */
data class SummaryUiState(
    val month: YearMonth,
    val totalCents: Long = 0L,
    val items: List<SummaryItem> = emptyList(),
)

/**
 * Holds the selected month (survives rotation through [SavedStateHandle]) and exposes
 * the per-category summary of that month.
 */
class SummaryViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val selectedMonth: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_MONTH, YearMonth.now().toString())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SummaryUiState> = selectedMonth
        .flatMapLatest { monthText ->
            val month = YearMonth.parse(monthText)
            val range = MonthRange.of(month)
            combine(
                expenseRepository.observeTotalsByCategory(range.startMillis, range.endMillis),
                categoryRepository.observeAll(),
            ) { totals, categories ->
                SummaryUiState(
                    month = month,
                    totalCents = totals.sumOf { it.totalCents },
                    items = SummaryCalculator.build(categories, totals),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SummaryUiState(month = YearMonth.parse(selectedMonth.value)),
        )

    fun previousMonth() = changeMonth(-1)

    fun nextMonth() = changeMonth(1)

    private fun changeMonth(delta: Long) {
        val current = YearMonth.parse(selectedMonth.value)
        savedStateHandle[KEY_MONTH] = current.plusMonths(delta).toString()
    }

    private companion object {
        const val KEY_MONTH = "summary_month"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}