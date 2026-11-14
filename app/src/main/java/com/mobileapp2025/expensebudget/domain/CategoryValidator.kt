package com.mobileapp2025.expensebudget.domain

/** Why a category name was rejected. */
enum class CategoryNameError { EMPTY, TOO_LONG, DUPLICATE }

/** Result of validating the category form. */
sealed interface CategoryValidation {
    data class Valid(val name: String, val budgetCents: Long?) : CategoryValidation

    data class Invalid(
        val nameError: CategoryNameError?,
        val budgetError: AmountError?,
    ) : CategoryValidation
}

/**
 * Validates the category form. The budget is optional: an empty text means "no budget".
 */
object CategoryValidator {
    const val MAX_NAME_LENGTH = 30

    /** [otherNames] are the names of all the OTHER categories (not the one being edited). */
    fun validate(name: String, budgetText: String, otherNames: List<String>): CategoryValidation {
        val trimmed = name.trim()
        val nameError = when {
            trimmed.isEmpty() -> CategoryNameError.EMPTY
            trimmed.length > MAX_NAME_LENGTH -> CategoryNameError.TOO_LONG
            otherNames.any { it.trim().equals(trimmed, ignoreCase = true) } ->
                CategoryNameError.DUPLICATE
            else -> null
        }

        var budgetCents: Long? = null
        var budgetError: AmountError? = null
        if (budgetText.isNotBlank()) {
            when (val result = AmountParser.parse(budgetText)) {
                is AmountResult.Valid -> budgetCents = result.cents
                is AmountResult.Invalid -> budgetError = result.error
            }
        }

        return if (nameError == null && budgetError == null) {
            CategoryValidation.Valid(trimmed, budgetCents)
        } else {
            CategoryValidation.Invalid(nameError, budgetError)
        }
    }
}