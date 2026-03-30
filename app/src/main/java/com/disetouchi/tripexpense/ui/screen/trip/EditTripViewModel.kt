package com.disetouchi.tripexpense.ui.screen.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disetouchi.tripexpense.data.repository.RateSnapshotRepository
import com.disetouchi.tripexpense.data.repository.TripRepository
import com.disetouchi.tripexpense.data.repository.UserPreferencesRepository
import com.disetouchi.tripexpense.domain.model.Trip
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

data class EditTripUiState(
    val isEdit: Boolean, // True -> Edit Trip, False -> Add Trip
    val tripId: Long?,

    val tripName: String = "",
    val baseCurrencyCode: String = "",
    val localCurrencyCodes: List<String> = emptyList(),

    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    val isSaving: Boolean = false
) {
    val isInputValid: Boolean
        get() {
            val nameValid = tripName.isNotBlank()
            val currenciesValid = localCurrencyCodes.isNotEmpty()
            val datesSelected = startDate != null && endDate != null
            val dateOrderValid = if (datesSelected) !startDate.isAfter(endDate) else false
            return nameValid && currenciesValid && datesSelected && dateOrderValid
        }
}

sealed interface EditTripEvent {
    data object Saved : EditTripEvent
    data object RateFetchFailed : EditTripEvent
    data object SaveFailed : EditTripEvent
}

class EditTripViewModel(
    private val tripId: Long?,
    private val tripRepository: TripRepository,
    private val rateSnapshotRepository: RateSnapshotRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditTripUiState(
            isEdit = tripId != null,
            tripId = tripId
        )
    )
    internal val uiState: StateFlow<EditTripUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditTripEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<EditTripEvent> = _events.asSharedFlow()

    private var loadedCreatedAt: Instant? = null

    init {
        if (tripId != null) {
            viewModelScope.launch {
                tripRepository.observeTrip(tripId).collect { trip ->
                    if (trip != null) {
                        loadedCreatedAt = trip.createdAt
                        _uiState.update { state ->
                            state.copy(
                                tripName = trip.tripName,
                                baseCurrencyCode = trip.baseCurrencyCode,
                                localCurrencyCodes = trip.localCurrencyCodes.filter { code -> code != trip.baseCurrencyCode },
                                startDate = trip.startDate,
                                endDate = trip.endDate
                            )
                        }
                    }
                }
            }
        }
    }

    // ---------- UI actions ----------
    internal fun onTripNameChange(value: String) {
        _uiState.update { state -> state.copy(tripName = value) }
    }

    internal fun onBaseCurrencyChange(value: String) {
        _uiState.update { state ->
            val filtered = state.localCurrencyCodes.filter { it != value }
            state.copy(baseCurrencyCode = value, localCurrencyCodes = filtered)
        }
    }

    internal fun addLocalCurrency(code: String) {
        _uiState.update { state ->
            if (code == state.baseCurrencyCode) return@update state
            if (state.localCurrencyCodes.contains(code)) return@update state
            state.copy(localCurrencyCodes = state.localCurrencyCodes + code)
        }
    }

    internal fun removeLocalCurrency(code: String) {
        _uiState.update { state ->
            state.copy(localCurrencyCodes = state.localCurrencyCodes.filterNot { it == code })
        }
    }

    internal fun onStartDateChange(date: LocalDate) {
        _uiState.update { state -> state.copy(startDate = date) }
    }

    internal fun onEndDateChange(date: LocalDate) {
        _uiState.update { state -> state.copy(endDate = date) }
    }

    /**
     * Triggered by the UI to save the trip.
     * Orchestrates the process of fetching required exchange rates,
     * building the domain model, and persisting it to the database.
     */
    internal fun onSaveClick() {
        if (!canSaveTrip()) return

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isSaving = true) }

            // Call Frankfurter API and save rates to Room
            val isRateFetchSuccess = fetchAndSaveRates()
            if (!isRateFetchSuccess) {
                _events.tryEmit(EditTripEvent.RateFetchFailed)
                _uiState.update { state -> state.copy(isSaving = false) }
                return@launch
            }

            // Update sync timestamp if all requests succeeded
            userPreferencesRepository.updateLastSyncTimestamp(System.currentTimeMillis())

            val trip = buildTrip()
            executeSave(trip)
        }
    }

    // ---------- Internal logic for saving ----------
    private fun canSaveTrip(): Boolean {
        val state = _uiState.value
        return !state.isSaving && state.isInputValid
    }

    /**
     * Calls the Frankfurter API for each selected local currency
     * and saves the rate snapshot to the database.
     * Returns true if all network requests succeed, false otherwise.
     */
    private suspend fun fetchAndSaveRates(): Boolean {
        val state = _uiState.value
        var hasNetworkError = false

        state.localCurrencyCodes.forEach { localCurrencyCode ->
            val result = rateSnapshotRepository.fetchAndSaveRate(localCurrencyCode)
            if (result.isFailure) {
                hasNetworkError = true
            }
        }

        return !hasNetworkError
    }

    private fun buildTrip(): Trip {
        val state = _uiState.value
        val now = Instant.now()
        val createdAt = loadedCreatedAt ?: now

        return Trip(
            tripId = state.tripId ?: 0L,
            tripName = state.tripName.trim(),
            baseCurrencyCode = state.baseCurrencyCode,
            localCurrencyCodes = state.localCurrencyCodes,
            startDate = requireNotNull(state.startDate),
            endDate = requireNotNull(state.endDate),
            createdAt = createdAt,
            updatedAt = now
        )
    }

    private fun executeSave(trip: Trip) {
        viewModelScope.launch {
            try {
                tripRepository.upsert(trip)
                _events.tryEmit(EditTripEvent.Saved)
            } catch (_: Exception) {
                _events.tryEmit(EditTripEvent.SaveFailed)
            } finally {
                _uiState.update { state -> state.copy(isSaving = false) }
            }
        }
    }
}