package com.mobileapp2025.expensebudget.ui

import androidx.annotation.StringRes
import com.mobileapp2025.expensebudget.R
import com.mobileapp2025.expensebudget.domain.AmountError

/** The message to show for an invalid amount. */
@StringRes
fun AmountError.toMessageRes(): Int = when (this) {
    AmountError.EMPTY -> R.string.error_amount_empty
    AmountError.NOT_A_NUMBER -> R.string.error_amount_invalid
    AmountError.NOT_POSITIVE -> R.string.error_amount_not_positive
    AmountError.TOO_MANY_DECIMALS -> R.string.error_amount_decimals
    AmountError.TOO_LARGE -> R.string.error_amount_too_large
}