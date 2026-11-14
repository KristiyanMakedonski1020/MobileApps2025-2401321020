package com.mobileapp2025.expensebudget.ui.summary

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
import com.mobileapp2025.expensebudget.BudgetApp
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.databinding.FragmentSummaryBinding
import com.mobileapp2025.expensebudget.domain.CurrencyFormatter
import com.mobileapp2025.expensebudget.ui.ViewModelFactory
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Monthly summary: total, and for every category the amount, its share of the total
 * and the progress against its budget.
 */
class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SummaryViewModel by viewModels {
        ViewModelFactory((requireActivity().application as BudgetApp).container)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SummaryAdapter()
        binding.recyclerSummary.adapter = adapter
        binding.buttonPreviousMonth.setOnClickListener { viewModel.previousMonth() }
        binding.buttonNextMonth.setOnClickListener { viewModel.nextMonth() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state, adapter) }
            }
        }
    }

    private fun render(state: SummaryUiState, adapter: SummaryAdapter) {
        binding.textMonth.text = formatMonth(state.month)
        binding.textTotal.text = getString(
            R.string.total_spent,
            CurrencyFormatter.format(state.totalCents),
        )
        adapter.submitList(state.items)
        val isEmpty = state.items.isEmpty()
        binding.textEmpty.isVisible = isEmpty
        binding.recyclerSummary.isVisible = !isEmpty
    }

    private fun formatMonth(month: YearMonth): String =
        "${month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${month.year}"

    override fun onDestroyView() {
        binding.recyclerSummary.adapter = null
        super.onDestroyView()
        _binding = null
    }
}