package com.disetouchi.tripexpense.ui.screen.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.disetouchi.tripexpense.data.repository.RateSnapshotRepository
import com.disetouchi.tripexpense.data.repository.TripRepository
import com.disetouchi.tripexpense.data.repository.UserPreferencesRepository

class EditTripViewModelFactory(
    private val tripId: Long?,
    private val tripRepository: TripRepository,
    private val rateSnapshotRepository: RateSnapshotRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == EditTripViewModel::class.java)
        return EditTripViewModel(
            tripId = tripId,
            tripRepository = tripRepository,
            rateSnapshotRepository = rateSnapshotRepository,
            userPreferencesRepository = userPreferencesRepository
        ) as T
    }
}