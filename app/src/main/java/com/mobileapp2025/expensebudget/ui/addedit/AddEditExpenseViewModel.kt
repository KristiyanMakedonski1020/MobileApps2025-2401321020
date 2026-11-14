package com.mobileapp2025.expensebudget.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileapp2025.expensebudget.data.local.entity.CategoryEntity
import com.mobileapp2025.expensebudget.data.local.entity.ExpenseEntity
import com.mobileapp2025.expensebudget.data.repository.CategoryRepository
import com.mobileapp2025.expensebudget.data.repository.ExpenseRepository
import com.mobileapp2025.expensebudget.domain.AmountError
import com.mobileapp2025.expensebudget.domain.AmountParser
import com.mobileapp2025.expensebudget.domain.AmountResult
import com.mobileapp2025.expensebudget.domain.DateConverter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Validation problems of the form. A null / false value means the field is fine. */
data class FormErrors(
    val amount: AmountError? = null,
    val categoryMissing: Boolean = false,
    val noteTooLong: Boolean = false,
) {
    val hasErrors: Boolean get() = amount != null || categoryMissing || noteTooLong
}

/** One-time events that make the fragment navigate or show a message. */
sealed interface AddEditEvent {
    data object Saved : AddEditEvent

    data object Deleted : AddEditEvent

    data object NotFound : AddEditEvent
}

/**
 * Logic of the add / edit form. The argument "expenseId" decides the mode:
 * [NEW_EXPENSE_ID] means create, any other value means edit that expense.
 * The selected category and date live in [SavedStateHandle], so they survive rotation.
 */
class AddEditExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val expenseId: Long = savedStateHandle.get<Long>(KEY_EXPENSE_ID) ?: NEW_EXPENSE_ID

    val isEditMode: Boolean = expenseId != NEW_EXPENSE_ID

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val selectedCategoryId: StateFlow<Long?> =
        savedStateHandle.getStateFlow<Long?>(KEY_CATEGORY_ID, null)

    val dateMillis: StateFlow<Long> =
        savedStateHandle.getStateFlow(KEY_DATE, DateConverter.startOfToday())

    private val _loadedExpense = MutableStateFlow<ExpenseEntity?>(null)
    val loadedExpense: StateFlow<ExpenseEntity?> = _loadedExpense.asStateFlow()

    private val _errors = MutableStateFlow(FormErrors())
    val errors: StateFlow<FormErrors> = _errors.asStateFlow()

    private val eventChannel = Channel<AddEditEvent>(Channel.BUFFERED)
    val events: Flow<AddEditEvent> = eventChannel.receiveAsFlow()

    private var isSaving = false

    init {
        if (isEditMode) loadExpense()
    }

    fun selectCategory(categoryId: Long) {
        savedStateHandle[KEY_CATEGORY_ID] = categoryId
        _errors.update { it.copy(categoryMissing = false) }
    }

    fun selectDate(millis: Long) {
        savedStateHandle[KEY_DATE] = millis
    }

    fun clearAmountError() {
        _errors.update { it.copy(amount = null) }
    }

    /** Validates the form and creates or updates the expense. */
    fun save(amountText: String, note: String) {
        val amountResult = AmountParser.parse(amountText)
        val valid = amountResult as? AmountResult.Valid
        val categoryId = selectedCategoryId.value
        val trimmedNote = note.trim()

        val errors = FormErrors(
            amount = (amountResult as? AmountResult.Invalid)?.error,
            categoryMissing = categoryId == null,
            noteTooLong = trimmedNote.length > MAX_NOTE_LENGTH,
        )
        _errors.value = errors
        if (errors.hasErrors || valid == null || categoryId == null || isSaving) return

        isSaving = true
        viewModelScope.launch {
            try {
                persist(valid.cents, categoryId, trimmedNote)
            } finally {
                isSaving = false
            }
        }
    }

    /** Deletes the expense that is being edited. */
    fun delete() {
        if (!isEditMode) return
        viewModelScope.launch {
            expenseRepository.getById(expenseId)?.let { expenseRepository.delete(it) }
            eventChannel.send(AddEditEvent.Deleted)
        }
    }

    private suspend fun persist(cents: Long, categoryId: Long, note: String) {
        val noteOrNull = note.ifEmpty { null }
        if (isEditMode) {
            val existing = expenseRepository.getById(expenseId)
            if (existing == null) {
                eventChannel.send(AddEditEvent.NotFound)
                return
            }
            expenseRepository.update(
                existing.copy(
                    amountCents = cents,
                    categoryId = categoryId,
                    date = dateMillis.value,
                    note = noteOrNull,
                ),
            )
        } else {
            expenseRepository.insert(
                ExpenseEntity(
                    amountCents = cents,
                    categoryId = categoryId,
                    date = dateMillis.value,
                    note = noteOrNull,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
        eventChannel.send(AddEditEvent.Saved)
    }

    private fun loadExpense() {
        viewModelScope.launch {
            val expense = expenseRepository.getById(expenseId)
            if (expense == null) {
                eventChannel.send(AddEditEvent.NotFound)
                return@launch
            }
            // Copy the stored values into the form only once (not again after rotation).
            if (savedStateHandle.get<Boolean>(KEY_INITIALIZED) != true) {
                savedStateHandle[KEY_CATEGORY_ID] = expense.categoryId
                savedStateHandle[KEY_DATE] = expense.date
                savedStateHandle[KEY_INITIALIZED] = true
            }
            _loadedExpense.value = expense
        }
    }

    companion object {
        const val NEW_EXPENSE_ID = -1L
        const val MAX_NOTE_LENGTH = 200
        private const val KEY_EXPENSE_ID = "expenseId"
        private const val KEY_CATEGORY_ID = "category_id"
        private const val KEY_DATE = "date_millis"
        private const val KEY_INITIALIZED = "initialized"
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}