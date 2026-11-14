package com.mobileapp2025.expensebudget.ui.categories

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.databinding.ItemCategoryBinding
import com.mobileapp2025.expensebudget.domain.CurrencyFormatter

/**
 * Shows the categories. Tapping a row edits it, the trash icon deletes it.
 */
class CategoriesAdapter(
    private val onEdit: (CategoryEntity) -> Unit,
    private val onDelete: (CategoryEntity) -> Unit,
) : ListAdapter<CategoryEntity, CategoriesAdapter.CategoryViewHolder>(CategoryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryEntity) {
            val context = binding.root.context
            val color = runCatching { Color.parseColor(category.colorHex) }.getOrDefault(Color.GRAY)
            binding.viewColorDot.backgroundTintList = ColorStateList.valueOf(color)
            binding.textName.text = category.name

            val budget = category.monthlyBudgetCents
            binding.textBudget.text = if (budget == null) {
                context.getString(R.string.no_budget)
            } else {
                context.getString(R.string.budget_per_month, CurrencyFormatter.format(budget))
            }

            binding.root.setOnClickListener { onEdit(category) }
            binding.buttonDelete.setOnClickListener { onDelete(category) }
        }
    }
}

private object CategoryDiffCallback : DiffUtil.ItemCallback<CategoryEntity>() {

    override fun areItemsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity) =
        oldItem == newItem
}