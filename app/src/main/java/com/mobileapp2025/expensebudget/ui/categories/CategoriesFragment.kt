package com.mobileapp2025.expensebudget.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mobileapp2025.expensebudget.BudgetApp
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.databinding.FragmentCategoriesBinding
import com.mobileapp2025.expensebudget.ui.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Manages expense categories: list, add, edit and delete.
 * A category that is used by expenses cannot be deleted.
 */
class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels {
        ViewModelFactory((requireActivity().application as BudgetApp).container)
    }

    private var confirmDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CategoriesAdapter(
            onEdit = { category -> showEditor(category) },
            onDelete = { category -> confirmDelete(category) },
        )
        binding.recyclerCategories.adapter = adapter
        binding.fabAdd.setOnClickListener { showEditor(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collect { categories ->
                        adapter.submitList(categories)
                        binding.textEmpty.isVisible = categories.isEmpty()
                        binding.recyclerCategories.isVisible = categories.isNotEmpty()
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            CategoriesEvent.CategoryInUse -> showInUseMessage()
                        }
                    }
                }
            }
        }
    }

    private fun showEditor(category: CategoryEntity?) {
        if (childFragmentManager.findFragmentByTag(CategoryDialogFragment.TAG) != null) return
        CategoryDialogFragment.newInstance(category)
            .show(childFragmentManager, CategoryDialogFragment.TAG)
    }

    private fun confirmDelete(category: CategoryEntity) {
        confirmDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_category_title)
            .setMessage(getString(R.string.delete_category_message, category.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.delete(category) }
            .show()
    }

    private fun showInUseMessage() {
        Snackbar.make(binding.root, R.string.category_in_use, Snackbar.LENGTH_LONG)
            .setAnchorView(binding.fabAdd)
            .show()
    }

    override fun onDestroyView() {
        confirmDialog?.dismiss()
        confirmDialog = null
        binding.recyclerCategories.adapter = null
        super.onDestroyView()
        _binding = null
    }
}