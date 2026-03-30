package com.disetouchi.tripexpense.ui.screen.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.disetouchi.tripexpense.data.repository.ExpenseRepository
import com.disetouchi.tripexpense.data.repository.TripRepository

class TripDetailViewModelFactory(
    private val tripId: Long,
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == TripDetailViewModel::class.java)
        return TripDetailViewModel(tripId, tripRepository, expenseRepository) as T
    }
}