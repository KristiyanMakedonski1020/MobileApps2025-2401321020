package com.mobileapp2025.expensebudget.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.repository.CategoryInUseException
import com.mobileapp2025.expensebudget.data.repository.CategoryRepository
import com.mobileapp2025.expensebudget.domain.CategoryValidation
import com.mobileapp2025.expensebudget.domain.CategoryValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One-time events for the categories screen. */
sealed interface CategoriesEvent {
    /** A category could not be deleted because expenses still use it. */
    data object CategoryInUse : CategoriesEvent
}

/**
 * Logic of the categories screen: the list, create / update and delete.
 */
class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val eventChannel = Channel<CategoriesEvent>(Channel.BUFFERED)
    val events: Flow<CategoriesEvent> = eventChannel.receiveAsFlow()

    /**
     * Validates the form and, when it is valid, saves the category in the background.
     * [categoryId] equal to [NEW_CATEGORY_ID] creates a category, any other value updates it.
     * The validation result is returned so the dialog can show the errors.
     */
    fun save(
        categoryId: Long,
        name: String,
        colorHex: String,
        budgetText: String,
    ): CategoryValidation {
        val otherNames = categories.value.filter { it.id != categoryId }.map { it.name }
        val result = CategoryValidator.validate(name, budgetText, otherNames)
        if (result is CategoryValidation.Valid) {
            viewModelScope.launch {
                if (categoryId == NEW_CATEGORY_ID) {
                    categoryRepository.insert(
                        CategoryEntity(
                            name = result.name,
                            colorHex = colorHex,
                            monthlyBudgetCents = result.budgetCents,
                        ),
                    )
                } else {
                    categoryRepository.getById(categoryId)?.let { existing ->
                        categoryRepository.update(
                            existing.copy(
                                name = result.name,
                                colorHex = colorHex,
                                monthlyBudgetCents = result.budgetCents,
                            ),
                        )
                    }
                }
            }
        }
        return result
    }

    fun delete(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                categoryRepository.delete(category)
            } catch (e: CategoryInUseException) {
                eventChannel.send(CategoriesEvent.CategoryInUse)
            }
        }
    }

    companion object {
        const val NEW_CATEGORY_ID = -1L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}