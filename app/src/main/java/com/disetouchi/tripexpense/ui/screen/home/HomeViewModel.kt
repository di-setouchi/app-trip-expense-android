package com.disetouchi.tripexpense.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disetouchi.tripexpense.data.repository.ExpenseRepository
import com.disetouchi.tripexpense.data.repository.RateSnapshotRepository
import com.disetouchi.tripexpense.data.repository.TripRepository
import com.disetouchi.tripexpense.data.repository.UserPreferencesRepository
import com.disetouchi.tripexpense.domain.model.Trip
import com.disetouchi.tripexpense.ui.model.ExpenseUiModel
import com.disetouchi.tripexpense.ui.model.toUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HomeUiState(
    val trips: List<Trip> = emptyList(),
    val selectedTripId: Long? = null,
    val expenses: List<ExpenseUiModel> = emptyList(),
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

sealed interface HomeEvent {
    data object RateFetchFailed : HomeEvent
}

class HomeViewModel(
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val rateSnapshotRepository: RateSnapshotRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)

    private val _events = MutableSharedFlow<HomeEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    private val tripsFlow: Flow<List<Trip>> =
        tripRepository.observeTrips().map { trips -> trips.sortedByDescending { it.tripId } }

    private val _selectedTripId = MutableStateFlow<Long?>(null)

    init {
        // Automatically select the most appropriate trip
        viewModelScope.launch {
            var isFirstLoad = true
            tripsFlow.collect { trips ->
                val currentIds = trips.map { it.tripId }
                val currentSelected = _selectedTripId.value
                
                if (isFirstLoad) {
                    // On first load, select the most recent trip (highest ID)
                    _selectedTripId.value = trips.firstOrNull()?.tripId
                    isFirstLoad = false
                } else {
                    // Check if new trips were added
                    val maxId = trips.maxOfOrNull { it.tripId }
                    if (maxId != null && (currentSelected == null || maxId > currentSelected)) {
                        // A newer trip was added, select it
                        _selectedTripId.value = maxId
                    } else if (currentSelected != null && currentSelected !in currentIds) {
                        // The currently selected trip was deleted
                        _selectedTripId.value = trips.firstOrNull()?.tripId
                    }
                }
            }
        }
    }

    private val resolvedSelectedTripIdFlow: Flow<Long?> = _selectedTripId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val expensesFlow: Flow<List<ExpenseUiModel>> =
        resolvedSelectedTripIdFlow.flatMapLatest { tripId ->
            if (tripId == null) {
                flowOf(emptyList())
            } else {
                expenseRepository.observeExpensesByTrip(tripId).map { list ->
                    list.map { it.toUiModel() }
                }
            }
        }

    internal val uiState: StateFlow<HomeUiState> =
        combine(
            tripsFlow,
            resolvedSelectedTripIdFlow,
            expensesFlow,
            _isSyncing,
            userPreferencesRepository.lastSyncTimestamp
        ) { trips, selectedTripId, expenses, isSyncing, lastSyncTimestamp ->
            HomeUiState(
                trips = trips,
                selectedTripId = selectedTripId,
                expenses = expenses,
                isSyncing = isSyncing,
                lastSyncTimestamp = lastSyncTimestamp
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    // ---------- UI actions ----------

    init {
        // Debug: Read saved values from DataStore
        viewModelScope.launch {
            val savedTimestamp = userPreferencesRepository.lastSyncTimestamp.first()
            android.util.Log.d("DataStoreDebug", "Last Sync Timestamp: $savedTimestamp")
        }
    }

    /**
     * Updates the selected trip ID in memory.
     * This method is called when a trip is selected from the UI (e.g. horizontal pager scroll).
     */
    internal fun onTripSelected(tripId: Long) {
        _selectedTripId.value = tripId
    }

    /**
     * Requests an automatic sync if the date has changed since the last sync.
     * This is intended to be called when the app returns to the foreground.
     */
    internal fun requestAutoSync() {
        viewModelScope.launch {
            val lastSync = userPreferencesRepository.lastSyncTimestamp.first()
            
            // Convert the last sync timestamp to LocalDate in the system's default time zone
            val lastSyncDate = Instant.ofEpochMilli(lastSync)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val currentDate = LocalDate.now(ZoneId.systemDefault())

            // Trigger sync if today is after the last recorded sync date (date has changed)
            val shouldSync = currentDate.isAfter(lastSyncDate)

            if (shouldSync) {
                onSyncClick()
            }
        }
    }

    /**
     * Performs a manual sync of exchange rates.
     */
    internal fun onSyncClick() {
        val currentState = uiState.value

        // Prevent double sync or sync when there are no trips
        if (_isSyncing.value || currentState.trips.isEmpty()) return

        // Extract all unique local currencies used in all existing trips
        val currenciesToSync = currentState.trips
            .flatMap { it.localCurrencyCodes }
            .distinct()

        if (currenciesToSync.isEmpty()) return

        executeSync(currenciesToSync)
    }

    // ---------- Internal logic for sync ----------

    /**
     * Executes the sync operation for the provided currencies.
     * Updates the last sync timestamp only if all fetch operations are successful.
     */
    private fun executeSync(currenciesToSync: List<String>) {
        viewModelScope.launch {
            _isSyncing.value = true
            var hasNetworkError = false

            try {
                currenciesToSync.forEach { currencyCode ->
                    val result = rateSnapshotRepository.fetchAndSaveRate(currencyCode)
                    if (result.isFailure) {
                        hasNetworkError = true
                    }
                }

                if (hasNetworkError) {
                    _events.tryEmit(HomeEvent.RateFetchFailed)
                } else {
                    // Update sync timestamp if all requests succeeded
                    userPreferencesRepository.updateLastSyncTimestamp(System.currentTimeMillis())
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }
}