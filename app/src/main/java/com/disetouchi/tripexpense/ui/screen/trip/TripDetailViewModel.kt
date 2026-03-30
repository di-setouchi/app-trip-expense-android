package com.disetouchi.tripexpense.ui.screen.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disetouchi.tripexpense.data.repository.ExpenseRepository
import com.disetouchi.tripexpense.data.repository.TripRepository
import com.disetouchi.tripexpense.domain.model.Trip
import com.disetouchi.tripexpense.ui.model.ExpenseUiModel
import com.disetouchi.tripexpense.ui.model.toUiModel
import com.disetouchi.tripexpense.util.AmountUtil
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val trip: Trip? = null,
    val expenses: List<ExpenseUiModel> = emptyList(),
    val totalsByCurrency: List<Pair<String, String>> = emptyList(),
)

sealed interface TripDetailEvent {
    data object Deleted : TripDetailEvent
    data class Error(val message: String) : TripDetailEvent
}

class TripDetailViewModel(
    private val tripId: Long,
    private val tripRepository: TripRepository,
    expenseRepository: ExpenseRepository,
) : ViewModel() {

    private val tripFlow = tripRepository.observeTrip(tripId)
    private val expensesDomainFlow = expenseRepository.observeExpensesByTrip(tripId)

    private val _events = MutableSharedFlow<TripDetailEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<TripDetailEvent> = _events.asSharedFlow()

    val uiState: StateFlow<TripDetailUiState> =
        combine(tripFlow, expensesDomainFlow) { trip, domainExpenses ->
            val uiExpenses = domainExpenses.map { it.toUiModel() }
            
            val totals = if (trip != null) {
                calculateTotals(domainExpenses, trip.baseCurrencyCode)
            } else {
                emptyList()
            }

            TripDetailUiState(
                trip = trip, 
                expenses = uiExpenses,
                totalsByCurrency = totals
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripDetailUiState()
        )

    internal fun deleteTrip() {
        viewModelScope.launch {
            try {
                // Expenses will be deleted by ForeignKey.CASCADE.
                tripRepository.deleteById(tripId)
                _events.tryEmit(TripDetailEvent.Deleted)
            } catch (e: Exception) {
                _events.tryEmit(TripDetailEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }

    private fun calculateTotals(
        expenses: List<com.disetouchi.tripexpense.domain.model.Expense>, 
        baseCurrency: String
    ): List<Pair<String, String>> {
        val localTotals = expenses
            .groupBy { it.localCurrencyCode }
            .map { (currencyCode, items) ->
                val sumMinor = items.sumOf { it.localAmountMinor }
                val formattedAmount = AmountUtil.formatMinorAmountWithSymbol(sumMinor, currencyCode)
                currencyCode to formattedAmount
            }
            .sortedBy { it.first }

        val baseSumMinor = expenses.sumOf { it.baseAmountMinor }
        val baseTotalFormatted = AmountUtil.formatMinorAmountWithSymbol(baseSumMinor, baseCurrency)
        val baseTotalRow = baseCurrency to baseTotalFormatted

        return localTotals + baseTotalRow
    }
}
