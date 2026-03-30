package com.disetouchi.tripexpense.ui.screen.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disetouchi.tripexpense.data.repository.ExpenseRepository
import com.disetouchi.tripexpense.data.repository.RateSnapshotRepository
import com.disetouchi.tripexpense.data.repository.TripRepository
import com.disetouchi.tripexpense.domain.model.Expense
import com.disetouchi.tripexpense.domain.model.Trip
import com.disetouchi.tripexpense.util.AmountUtil
import com.disetouchi.tripexpense.util.DateTimeUtil
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate

data class EditExpenseUiState(
    val tripId: Long,
    val expenseId: Long?,
    val isEdit: Boolean,

    val baseCurrencyCode: String = "",
    val localCurrencyCodes: List<String> = emptyList(),
    val localCurrencyCode: String = "",

    val occurredAt: LocalDate = LocalDate.now(),
    val categoryId: Int = 1, // default category is Food
    val note: String = "",

    val localAmountText: String = "",
    val baseAmountText: String = "0",

    val exchangeRateText: String = "—",
    val rateFetchedText: String = "—",
    val isRateAvailable: Boolean = false,

    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = !isSaving && isRateAvailable && localAmountText.toBigDecimalOrNull() != null && localAmountText.isNotBlank()
}

sealed interface EditExpenseEvent {
    data object Saved : EditExpenseEvent
    data object SaveFailed : EditExpenseEvent
    data object RateUnavailable : EditExpenseEvent
}

class EditExpenseViewModel(
    private val tripId: Long,
    private val expenseId: Long?,
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val rateSnapshotRepository: RateSnapshotRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditExpenseUiState(
            tripId = tripId,
            expenseId = expenseId,
            isEdit = expenseId != null,
        )
    )
    internal val uiState: StateFlow<EditExpenseUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditExpenseEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<EditExpenseEvent> = _events.asSharedFlow()

    private var loadedTrip: Trip? = null
    private var loadedExpense: Expense? = null
    private var rateUsedMicros: Long? = null
    private var rateFetchedAt: Instant? = null

    init {
        observeTrip()
        observeExpenseIfNeeded()
    }

    private fun observeTrip() {
        viewModelScope.launch {
            tripRepository.observeTrip(tripId).collect { trip ->
                loadedTrip = trip
                if (trip == null) return@collect

                _uiState.update { state ->
                    val localCurrencyCodes = trip.localCurrencyCodes
                    val localCurrencyCode = state.localCurrencyCode.ifBlank { localCurrencyCodes.firstOrNull().orEmpty() }

                    state.copy(
                        baseCurrencyCode = trip.baseCurrencyCode,
                        localCurrencyCodes = localCurrencyCodes,
                        localCurrencyCode = localCurrencyCode,
                    )
                }

                // For Add mode (or when edit currency changes), we get the rate from snapshot.
                setupExchangeRate()
                updateBaseAmountPreview()
            }
        }
    }

    private fun observeExpenseIfNeeded() {
        if (expenseId == null) {
            // Initialize Add mode defaults when trip arrives.
            _uiState.update { state -> state.copy(occurredAt = LocalDate.now()) }
            return
        }

        viewModelScope.launch {
            expenseRepository.observeExpense(tripId, expenseId).collect { expense ->
                loadedExpense = expense
                if (expense == null) return@collect

                // Apply saved rate to state and internal variables.
                applySavedExchangeRate(expense)

                _uiState.update { state ->
                    state.copy(
                        localCurrencyCode = expense.localCurrencyCode,
                        occurredAt = expense.occurredAt,
                        categoryId = expense.categoryId,
                        note = expense.note,
                        localAmountText = AmountUtil.minorToPlainString(expense.localAmountMinor, expense.localCurrencyCode),
                        baseAmountText = AmountUtil.formatMinorAmount(expense.baseAmountMinor, expense.baseCurrencyCode),
                    )
                }
            }
        }
    }

    // ---------- UI actions ----------
    internal fun onLocalAmountChange(value: String) {
        _uiState.update { state -> state.copy(localAmountText = value) }
        updateBaseAmountPreview()
    }

    internal fun onLocalCurrencyChange(code: String) {
        _uiState.update { state -> state.copy(localCurrencyCode = code) }
        setupExchangeRate()
        updateBaseAmountPreview()
    }

    internal fun onDateChange(date: LocalDate) {
        _uiState.update { state -> state.copy(occurredAt = date) }
    }

    internal fun onCategoryChange(categoryId: Int) {
        _uiState.update { state -> state.copy(categoryId = categoryId) }
    }

    internal fun onNoteChange(value: String) {
        _uiState.update { state -> state.copy(note = value) }
    }

    internal fun onSaveClick() {
        if (!canSaveExpense()) return

        val localAmountMinor = calculateLocalMinor() ?: return
        val baseAmountMinor = calculateBaseMinor(localAmountMinor)
        val expense = buildExpense(localAmountMinor, baseAmountMinor)

        executeSave(expense)
    }

    // ---------- Internal logic for saving ----------
    private fun canSaveExpense(): Boolean {
        val state = _uiState.value
        if (state.isSaving || !state.canSave) return false

        if (loadedTrip == null) {
            _events.tryEmit(EditExpenseEvent.SaveFailed)
            return false
        }

        if (rateUsedMicros == null || rateFetchedAt == null) {
            _events.tryEmit(EditExpenseEvent.RateUnavailable)
            return false
        }
        return true
    }

    private fun calculateLocalMinor(): Long? {
        val state = _uiState.value
        return AmountUtil.parseToMinor(state.localAmountText, state.localCurrencyCode)
    }

    private fun calculateBaseMinor(localAmountMinor: Long): Long {
        val state = _uiState.value
        val trip = loadedTrip!!
        val micros = rateUsedMicros!!

        return AmountUtil.computeBaseMinor(
            localAmountMinor = localAmountMinor,
            localCurrencyCode = state.localCurrencyCode,
            baseCurrencyCode = trip.baseCurrencyCode,
            rateUsedMicros = micros
        )
    }

    private fun buildExpense(localAmountMinor: Long, baseAmountMinor: Long): Expense {
        val state = _uiState.value
        val trip = loadedTrip!!
        val now = Instant.now()

        return Expense(
            expenseId = loadedExpense?.expenseId ?: 0L,
            tripId = trip.tripId,
            categoryId = state.categoryId,
            occurredAt = state.occurredAt,
            localAmountMinor = localAmountMinor,
            baseAmountMinor = baseAmountMinor,
            localCurrencyCode = state.localCurrencyCode,
            baseCurrencyCode = trip.baseCurrencyCode,
            rateUsedMicros = rateUsedMicros!!,
            rateFetchedAt = rateFetchedAt!!,
            note = state.note,
            createdAt = loadedExpense?.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun executeSave(expense: Expense) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isSaving = true) }
            try {
                expenseRepository.upsert(expense)
                _events.tryEmit(EditExpenseEvent.Saved)
            } catch (_: Exception) {
                _events.tryEmit(EditExpenseEvent.SaveFailed)
            } finally {
                _uiState.update { state -> state.copy(isSaving = false) }
            }
        }
    }

    // ---------- Exchange rate logic ----------
    private fun setupExchangeRate() {
        val state = _uiState.value
        val savedExpense = loadedExpense

        // If edit mode and local currency not changed,
        // keep the rate saved at the time of making the record to prevent base amount changes due to rate update.
        if (savedExpense != null && savedExpense.localCurrencyCode == state.localCurrencyCode) {
            applySavedExchangeRate(savedExpense)
        } else {
            fetchExchangeRateFromSnapshot()
        }
    }

    private fun applySavedExchangeRate(expense: Expense) {
        rateUsedMicros = expense.rateUsedMicros
        rateFetchedAt = expense.rateFetchedAt
        _uiState.update { state ->
            state.copy(
                exchangeRateText = AmountUtil.formatRateMicros(expense.rateUsedMicros),
                rateFetchedText = DateTimeUtil.format(expense.rateFetchedAt),
                isRateAvailable = true
            )
        }
    }

    private fun fetchExchangeRateFromSnapshot() {
        val state = _uiState.value
        val trip = loadedTrip ?: return

        viewModelScope.launch {
            val snapshot = rateSnapshotRepository.getSnapshot(state.localCurrencyCode)
            val rate = snapshot?.rates?.get(trip.baseCurrencyCode)

            if (rate == null) {
                handleRateUnavailable()
            } else {
                applySnapshotRate(rate, snapshot.rateFetchedAt)
            }
        }
    }

    private fun handleRateUnavailable() {
        rateUsedMicros = null
        rateFetchedAt = null
        _uiState.update { state ->
            state.copy(
                exchangeRateText = "—",
                rateFetchedText = "—",
                isRateAvailable = false
            )
        }
        _events.tryEmit(EditExpenseEvent.RateUnavailable)
    }

    private fun applySnapshotRate(rate: Double, fetchedAt: Instant) {
        val micros = BigDecimal.valueOf(rate)
            .multiply(BigDecimal.valueOf(1_000_000L))
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()

        rateUsedMicros = micros
        rateFetchedAt = fetchedAt

        _uiState.update { state ->
            state.copy(
                exchangeRateText = AmountUtil.formatRateMicros(micros),
                rateFetchedText = DateTimeUtil.format(fetchedAt),
                isRateAvailable = true
            )
        }
        updateBaseAmountPreview()
    }

    // ---------- Base Amount preview logic ----------
    private fun updateBaseAmountPreview() {
        val trip = loadedTrip ?: return
        val localAmountMinor = calculateLocalMinor()
        
        if (localAmountMinor == null || rateUsedMicros == null) {
            _uiState.update { state -> state.copy(baseAmountText = "0") }
            return
        }

        val baseAmountMinor = calculateBaseMinor(localAmountMinor)

        _uiState.update { state ->
            state.copy(baseAmountText = AmountUtil.formatMinorAmount(baseAmountMinor, trip.baseCurrencyCode))
        }
    }
}
