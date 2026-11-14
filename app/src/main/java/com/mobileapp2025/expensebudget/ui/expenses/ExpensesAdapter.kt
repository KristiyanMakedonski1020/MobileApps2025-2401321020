package com.mobileapp2025.expensebudget.ui.expenses

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.data.local.model.ExpenseWithCategory
import com.mobileapp2025.expensebudget.databinding.ItemExpenseBinding
import com.mobileapp2025.expensebudget.domain.CurrencyFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Shows the expenses in a RecyclerView. [DiffUtil] updates only the rows that changed.
 */
class ExpensesAdapter(
    private val onExpenseClick: (Long) -> Unit,
) : ListAdapter<ExpenseWithCategory, ExpensesAdapter.ExpenseViewHolder>(ExpenseDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExpenseWithCategory) {
            val expense = item.expense
            val context = binding.root.context

            val color = runCatching { Color.parseColor(item.category.colorHex) }
                .getOrDefault(Color.GRAY)
            binding.viewColorDot.backgroundTintList = ColorStateList.valueOf(color)

            binding.textCategory.text = item.category.name
            binding.textAmount.text = CurrencyFormatter.format(expense.amountCents)

            val date = Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DATE_FORMATTER)
            val note = expense.note
            binding.textSubtitle.text = if (note.isNullOrBlank()) {
                date
            } else {
                context.getString(R.string.expense_date_note, date, note)
            }

            binding.root.setOnClickListener { onExpenseClick(expense.id) }
        }
    }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH)
    }
}

private object ExpenseDiffCallback : DiffUtil.ItemCallback<ExpenseWithCategory>() {

    override fun areItemsTheSame(oldItem: ExpenseWithCategory, newItem: ExpenseWithCategory) =
        oldItem.expense.id == newItem.expense.id

    override fun areContentsTheSame(oldItem: ExpenseWithCategory, newItem: ExpenseWithCategory) =
        oldItem == newItem
}