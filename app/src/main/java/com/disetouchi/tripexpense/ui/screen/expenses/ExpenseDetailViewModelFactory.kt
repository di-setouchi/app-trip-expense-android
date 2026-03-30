package com.disetouchi.tripexpense.ui.screen.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.disetouchi.tripexpense.data.repository.ExpenseRepository

class ExpenseDetailViewModelFactory(
    private val tripId: Long,
    private val expenseId: Long,
    private val expenseRepository: ExpenseRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ExpenseDetailViewModel::class.java)
        return ExpenseDetailViewModel(tripId, expenseId, expenseRepository) as T
    }
}