package com.mobileapp2025.expensebudget.ui.categories

import androidx.annotation.StringRes
import com.mobileapp2025.expensebudget.R

/** One selectable category color: the hex value saved in the database and its display name. */
data class CategoryColor(val hex: String, @StringRes val nameRes: Int)

/** The fixed palette offered in the category dialog. */
object CategoryColors {

    val palette: List<CategoryColor> = listOf(
        CategoryColor("#4CAF50", R.string.color_name_green),
        CategoryColor("#2196F3", R.string.color_name_blue),
        CategoryColor("#FF9800", R.string.color_name_orange),
        CategoryColor("#9C27B0", R.string.color_name_purple),
        CategoryColor("#F44336", R.string.color_name_red),
        CategoryColor("#607D8B", R.string.color_name_gray),
        CategoryColor("#009688", R.string.color_name_teal),
        CategoryColor("#E91E63", R.string.color_name_pink),
    )

    /** Index of [hex] in the palette, or 0 if it is not there. */
    fun indexOf(hex: String): Int =
        palette.indexOfFirst { it.hex.equals(hex, ignoreCase = true) }.coerceAtLeast(0)
}