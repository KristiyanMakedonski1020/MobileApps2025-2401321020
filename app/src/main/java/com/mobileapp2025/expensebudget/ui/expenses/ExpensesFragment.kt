package com.mobileapp2025.expensebudget.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.databinding.FragmentExpensesBinding

/**
 * Start screen: the list of expenses for the selected month.
 * Placeholder in Phase 2, fully implemented in Phase 5.
 */
class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

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
        binding.buttonTempOpen.setOnClickListener {
            findNavController().navigate(
                R.id.action_expenses_to_addEdit,
                bundleOf("expenseId" to -1L),
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}