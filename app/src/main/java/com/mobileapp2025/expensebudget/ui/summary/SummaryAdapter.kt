package com.mobileapp2025.expensebudget.ui.summary

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.databinding.ItemSummaryBinding
import com.mobileapp2025.expensebudget.domain.BudgetStatus
import com.mobileapp2025.expensebudget.domain.CurrencyFormatter
import com.mobileapp2025.expensebudget.domain.SummaryItem

/**
 * Shows one row per category. The budget bar is green, orange (from 80%) or red (over budget).
 */
class SummaryAdapter : ListAdapter<SummaryItem, SummaryAdapter.SummaryViewHolder>(SummaryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val binding = ItemSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SummaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SummaryViewHolder(
        private val binding: ItemSummaryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SummaryItem) {
            val context = binding.root.context

            val color = runCatching { Color.parseColor(item.colorHex) }.getOrDefault(Color.GRAY)
            binding.viewColorDot.backgroundTintList = ColorStateList.valueOf(color)
            binding.textName.text = item.name
            binding.textAmount.text = CurrencyFormatter.format(item.spentCents)
            binding.textPercentOfTotal.text = context.getString(R.string.percent_of_total, item.percentOfTotal)

            val budget = item.budgetCents
            val hasBudget = budget != null && item.budgetPercent != null
            binding.textBudget.isVisible = hasBudget
            binding.progressBudget.isVisible = hasBudget
            if (budget == null || item.budgetPercent == null) return

            val textRes = if (item.status == BudgetStatus.EXCEEDED) {
                R.string.budget_line_exceeded
            } else {
                R.string.budget_line
            }
            binding.textBudget.text = context.getString(
                textRes,
                CurrencyFormatter.format(item.spentCents),
                CurrencyFormatter.format(budget),
                item.budgetPercent,
            )

            val statusColor = ContextCompat.getColor(
                context,
                when (item.status) {
                    BudgetStatus.EXCEEDED -> R.color.budget_exceeded
                    BudgetStatus.NEAR -> R.color.budget_near
                    else -> R.color.budget_ok
                },
            )
            binding.textBudget.setTextColor(statusColor)
            binding.progressBudget.setIndicatorColor(statusColor)
            binding.progressBudget.setProgressCompat(item.budgetPercent.coerceIn(0, MAX_PERCENT), false)
        }
    }

    private companion object {
        const val MAX_PERCENT = 100
    }
}

private object SummaryDiffCallback : DiffUtil.ItemCallback<SummaryItem>() {

    override fun areItemsTheSame(oldItem: SummaryItem, newItem: SummaryItem) =
        oldItem.categoryId == newItem.categoryId

    override fun areContentsTheSame(oldItem: SummaryItem, newItem: SummaryItem) =
        oldItem == newItem
}