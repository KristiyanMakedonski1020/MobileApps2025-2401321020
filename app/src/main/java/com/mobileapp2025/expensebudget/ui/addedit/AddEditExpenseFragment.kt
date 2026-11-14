package com.mobileapp2025.expensebudget.ui.addedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mobileapp2025.expensebudget.BudgetApp
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.local.entity.ExpenseEntity
import com.mobileapp2025.expensebudget.databinding.FragmentAddEditExpenseBinding
import com.mobileapp2025.expensebudget.domain.AmountError
import com.mobileapp2025.expensebudget.domain.CurrencyFormatter
import com.mobileapp2025.expensebudget.domain.DateConverter
import com.mobileapp2025.expensebudget.ui.ViewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Form for creating a new expense or editing / deleting an existing one.
 * The mode is chosen by the "expenseId" navigation argument (-1 means new).
 */
class AddEditExpenseFragment : Fragment() {

    private var _binding: FragmentAddEditExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditExpenseViewModel by viewModels {
        ViewModelFactory((requireActivity().application as BudgetApp).container)
    }

    private var deleteDialog: AlertDialog? = null

    /** True only on the first creation of the screen in edit mode, so rotation keeps typed text. */
    private var needsPrefill = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddEditExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        needsPrefill = viewModel.isEditMode && savedInstanceState == null

        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(
            if (viewModel.isEditMode) R.string.title_edit_expense else R.string.title_new_expense,
        )
        binding.buttonDelete.isVisible = viewModel.isEditMode

        binding.autoCategory.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position) as CategoryItem
            viewModel.selectCategory(item.id)
        }
        binding.editDate.setOnClickListener { showDatePicker() }
        binding.layoutDate.setOnClickListener { showDatePicker() }
        binding.editAmount.doOnTextChanged { _, _, _, _ -> viewModel.clearAmountError() }
        binding.buttonSave.setOnClickListener {
            viewModel.save(
                amountText = binding.editAmount.text?.toString().orEmpty(),
                note = binding.editNote.text?.toString().orEmpty(),
            )
        }
        binding.buttonDelete.setOnClickListener { confirmDelete() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(viewModel.categories, viewModel.selectedCategoryId) { list, id ->
                        list to id
                    }.collect { (list, id) -> bindCategories(list, id) }
                }
                launch {
                    viewModel.dateMillis.collect { binding.editDate.setText(formatDate(it)) }
                }
                launch { viewModel.errors.collect { renderErrors(it) } }
                launch { viewModel.loadedExpense.filterNotNull().collect { prefill(it) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }

    private fun bindCategories(categories: List<CategoryEntity>, selectedId: Long?) {
        val items = categories.map { CategoryItem(it.id, it.name) }
        binding.autoCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items),
        )
        binding.autoCategory.setText(items.firstOrNull { it.id == selectedId }?.name.orEmpty(), false)
    }

    private fun prefill(expense: ExpenseEntity) {
        if (!needsPrefill) return
        needsPrefill = false
        binding.editAmount.setText(CurrencyFormatter.toInputString(expense.amountCents))
        binding.editNote.setText(expense.note.orEmpty())
    }

    private fun renderErrors(errors: FormErrors) {
        binding.layoutAmount.error = errors.amount?.let { getString(amountMessage(it)) }
        binding.layoutCategory.error =
            if (errors.categoryMissing) getString(R.string.error_category_required) else null
        binding.layoutNote.error =
            if (errors.noteTooLong) getString(R.string.error_note_too_long) else null
    }

    private fun amountMessage(error: AmountError): Int = when (error) {
        AmountError.EMPTY -> R.string.error_amount_empty
        AmountError.NOT_A_NUMBER -> R.string.error_amount_invalid
        AmountError.NOT_POSITIVE -> R.string.error_amount_not_positive
        AmountError.TOO_MANY_DECIMALS -> R.string.error_amount_decimals
        AmountError.TOO_LARGE -> R.string.error_amount_too_large
    }

    private fun handleEvent(event: AddEditEvent) {
        when (event) {
            AddEditEvent.Saved, AddEditEvent.Deleted -> findNavController().popBackStack()
            AddEditEvent.NotFound -> {
                Toast.makeText(requireContext(), R.string.expense_not_found, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun showDatePicker() {
        if (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.select_date)
            .setSelection(DateConverter.localStartOfDayToUtcPicker(viewModel.dateMillis.value))
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            viewModel.selectDate(DateConverter.utcPickerToLocalStartOfDay(selection))
        }
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun confirmDelete() {
        deleteDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_expense_title)
            .setMessage(R.string.delete_expense_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.delete() }
            .show()
    }

    private fun formatDate(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DATE_FORMATTER)

    override fun onDestroyView() {
        deleteDialog?.dismiss()
        deleteDialog = null
        binding.autoCategory.setAdapter(null)
        super.onDestroyView()
        _binding = null
    }

    /** Dropdown entry: shows the category name but remembers its id. */
    private class CategoryItem(val id: Long, val name: String) {
        override fun toString(): String = name
    }

    private companion object {
        const val DATE_PICKER_TAG = "date_picker"
        val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH)
    }
}