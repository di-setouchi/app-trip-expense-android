package com.disetouchi.tripexpense.ui.screen.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disetouchi.tripexpense.data.repository.ExpenseRepository
import com.disetouchi.tripexpense.domain.model.Expense
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExpenseDetailUiState(
    val expense: Expense? = null
)

sealed interface ExpenseDetailEvent {
    data object Deleted : ExpenseDetailEvent
    data class Error(val message: String) : ExpenseDetailEvent
}

class ExpenseDetailViewModel(
    private val tripId: Long,
    private val expenseId: Long,
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {

    val uiState: StateFlow<ExpenseDetailUiState> =
        expenseRepository.observeExpense(tripId, expenseId)
            .map { expense -> ExpenseDetailUiState(expense = expense) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ExpenseDetailUiState()
            )

    private val _events = MutableSharedFlow<ExpenseDetailEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ExpenseDetailEvent> = _events.asSharedFlow()

    fun deleteExpense() {
        viewModelScope.launch {
            try {
                expenseRepository.deleteById(expenseId)
                _events.tryEmit(ExpenseDetailEvent.Deleted)
            } catch (e: Exception) {
                _events.tryEmit(ExpenseDetailEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }
}