package com.mobileapp2025.expensebudget.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.core.content.ContextCompat
import com.mobileapp2025.expensebudget.BudgetApp
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.databinding.FragmentExpensesBinding
import com.mobileapp2025.expensebudget.domain.BudgetCalculator
import com.mobileapp2025.expensebudget.domain.BudgetStatus
import com.mobileapp2025.expensebudget.domain.CurrencyFormatter
import com.mobileapp2025.expensebudget.ui.ViewModelFactory
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Start screen: the expenses of the selected month with the total and the budget progress.
 */
class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpensesViewModel by viewModels {
        ViewModelFactory((requireActivity().application as BudgetApp).container)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ExpensesAdapter { expenseId -> openEditor(expenseId) }
        binding.recyclerExpenses.adapter = adapter

        binding.buttonPreviousMonth.setOnClickListener { viewModel.previousMonth() }
        binding.buttonNextMonth.setOnClickListener { viewModel.nextMonth() }
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_expenses_to_addEdit)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state, adapter) }
            }
        }
    }

    private fun openEditor(expenseId: Long) {
        val args = Bundle().apply { putLong("expenseId", expenseId) }
        findNavController().navigate(R.id.action_expenses_to_addEdit, args)
    }

    private fun render(state: ExpensesUiState, adapter: ExpensesAdapter) {
        binding.textMonth.text = formatMonth(state.month)
        binding.textTotal.text = getString(
            R.string.total_spent,
            CurrencyFormatter.format(state.totalCents),
        )

        val budget = state.budgetCents
        binding.textBudget.isVisible = budget != null
        binding.progressBudget.isVisible = budget != null
        if (budget != null) {
            val percent = BudgetCalculator.percent(state.budgetSpentCents, budget) ?: 0
            binding.textBudget.text = getString(
                R.string.budget_summary,
                CurrencyFormatter.format(state.budgetSpentCents),
                CurrencyFormatter.format(budget),
                percent,
            )
            binding.progressBudget.setProgressCompat(percent.coerceIn(0, MAX_PERCENT), true)

            val exceeded =
                BudgetCalculator.status(state.budgetSpentCents, budget) == BudgetStatus.EXCEEDED
            val colorRes = if (exceeded) R.color.budget_exceeded else R.color.budget_ok
            binding.progressBudget.setIndicatorColor(
                ContextCompat.getColor(requireContext(), colorRes),
            )
        }

        adapter.submitList(state.expenses)
        val isEmpty = state.expenses.isEmpty()
        binding.textEmpty.isVisible = isEmpty
        binding.recyclerExpenses.isVisible = !isEmpty
    }

    private fun formatMonth(month: YearMonth): String =
        "${month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${month.year}"

    override fun onDestroyView() {
        binding.recyclerExpenses.adapter = null
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val MAX_PERCENT = 100
    }
}