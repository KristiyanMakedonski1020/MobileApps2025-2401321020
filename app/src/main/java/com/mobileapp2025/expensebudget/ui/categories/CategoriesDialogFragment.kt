package com.mobileapp2025.expensebudget.ui.categories

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mobileapp2025.expensebudget.BudgetApp
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.databinding.DialogCategoryBinding
import com.mobileapp2025.expensebudget.databinding.ItemColorChipBinding
import com.mobileapp2025.expensebudget.domain.CategoryNameError
import com.mobileapp2025.expensebudget.domain.CategoryValidation
import com.mobileapp2025.expensebudget.domain.CurrencyFormatter
import com.mobileapp2025.expensebudget.ui.ViewModelFactory
import com.mobileapp2025.expensebudget.ui.toMessageRes

/**
 * Dialog for adding or editing a category: name, color and optional monthly budget.
 * It stays open while the input is invalid. It shares the ViewModel of [CategoriesFragment].
 */
class CategoryDialogFragment : DialogFragment() {

    private var _binding: DialogCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
    ) {
        ViewModelFactory((requireActivity().application as BudgetApp).container)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCategoryBinding.inflate(layoutInflater)
        val args = requireArguments()
        val categoryId = args.getLong(ARG_ID, CategoriesViewModel.NEW_CATEGORY_ID)

        binding.editName.setText(args.getString(ARG_NAME).orEmpty())
        val budget = args.getLong(ARG_BUDGET, NO_BUDGET)
        if (budget != NO_BUDGET) binding.editBudget.setText(CurrencyFormatter.toInputString(budget))

        val initialColorIndex = savedInstanceState?.getInt(KEY_COLOR_INDEX)
            ?: CategoryColors.indexOf(args.getString(ARG_COLOR).orEmpty())
        addColorChips(initialColorIndex)

        val title = if (categoryId == CategoriesViewModel.NEW_CATEGORY_ID) {
            R.string.new_category
        } else {
            R.string.edit_category
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()

        // Set the click listener here, not in setPositiveButton, so the dialog does not close on errors.
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                onSaveClicked(categoryId)
            }
        }
        return dialog
    }

    private fun addColorChips(selectedIndex: Int) {
        CategoryColors.palette.forEachIndexed { index, option ->
            val chip = ItemColorChipBinding.inflate(layoutInflater, binding.chipGroupColors, false).root
            chip.id = View.generateViewId()
            chip.tag = index
            chip.setText(option.nameRes)
            chip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.bg_color_dot)
            chip.chipIconTint = ColorStateList.valueOf(Color.parseColor(option.hex))
            chip.isChipIconVisible = true
            chip.isChecked = index == selectedIndex
            binding.chipGroupColors.addView(chip)
        }
    }

    private fun selectedColorIndex(): Int {
        val checkedId = binding.chipGroupColors.checkedChipId
        val chip = binding.chipGroupColors.findViewById<Chip>(checkedId)
        return (chip?.tag as? Int) ?: 0
    }

    private fun onSaveClicked(categoryId: Long) {
        val result = viewModel.save(
            categoryId = categoryId,
            name = binding.editName.text?.toString().orEmpty(),
            colorHex = CategoryColors.palette[selectedColorIndex()].hex,
            budgetText = binding.editBudget.text?.toString().orEmpty(),
        )
        when (result) {
            is CategoryValidation.Valid -> dismiss()
            is CategoryValidation.Invalid -> {
                binding.layoutName.error = result.nameError?.let { getString(nameMessage(it)) }
                binding.layoutBudget.error = result.budgetError?.let { getString(it.toMessageRes()) }
            }
        }
    }

    private fun nameMessage(error: CategoryNameError): Int = when (error) {
        CategoryNameError.EMPTY -> R.string.error_name_empty
        CategoryNameError.TOO_LONG -> R.string.error_name_too_long
        CategoryNameError.DUPLICATE -> R.string.error_name_duplicate
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let { outState.putInt(KEY_COLOR_INDEX, selectedColorIndex()) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "category_dialog"
        private const val ARG_ID = "category_id"
        private const val ARG_NAME = "category_name"
        private const val ARG_COLOR = "category_color"
        private const val ARG_BUDGET = "category_budget"
        private const val KEY_COLOR_INDEX = "color_index"
        private const val NO_BUDGET = -1L

        /** Pass null for a new category, or the category to edit. */
        fun newInstance(category: CategoryEntity?): CategoryDialogFragment =
            CategoryDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ID, category?.id ?: CategoriesViewModel.NEW_CATEGORY_ID)
                    putString(ARG_NAME, category?.name.orEmpty())
                    putString(ARG_COLOR, category?.colorHex ?: CategoryColors.palette.first().hex)
                    putLong(ARG_BUDGET, category?.monthlyBudgetCents ?: NO_BUDGET)
                }
            }
    }
}