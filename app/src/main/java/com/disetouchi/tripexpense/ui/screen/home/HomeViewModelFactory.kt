package com.disetouchi.tripexpense.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.disetouchi.tripexpense.data.repository.ExpenseRepository
import com.disetouchi.tripexpense.data.repository.RateSnapshotRepository
import com.disetouchi.tripexpense.data.repository.TripRepository
import com.disetouchi.tripexpense.data.repository.UserPreferencesRepository

class HomeViewModelFactory(
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val rateSnapshotRepository: RateSnapshotRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == HomeViewModel::class.java)
        return HomeViewModel(
            tripRepository = tripRepository,
            expenseRepository = expenseRepository,
            rateSnapshotRepository = rateSnapshotRepository,
            userPreferencesRepository = userPreferencesRepository
        ) as T
    }
}
