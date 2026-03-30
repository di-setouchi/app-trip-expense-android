package com.disetouchi.tripexpense.ui.screen.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.disetouchi.tripexpense.data.repository.ExpenseRepository
import com.disetouchi.tripexpense.data.repository.RateSnapshotRepository
import com.disetouchi.tripexpense.data.repository.TripRepository

class EditExpenseViewModelFactory(
    private val tripId: Long,
    private val expenseId: Long?,
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val rateSnapshotRepository: RateSnapshotRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == EditExpenseViewModel::class.java)
        return EditExpenseViewModel(
            tripId = tripId,
            expenseId = expenseId,
            tripRepository = tripRepository,
            expenseRepository = expenseRepository,
            rateSnapshotRepository = rateSnapshotRepository
        ) as T
    }
}