package com.disetouchi.tripexpense.data.repository

import com.disetouchi.tripexpense.data.room.dao.ExpenseDao
import com.disetouchi.tripexpense.data.room.toDomain
import com.disetouchi.tripexpense.data.room.toEntity
import com.disetouchi.tripexpense.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {
    fun observeExpensesByTrip(tripId: Long): Flow<List<Expense>> =
        expenseDao.observeExpensesByTrip(tripId).map { list -> list.map { it.toDomain() } }

    fun observeExpense(tripId: Long, expenseId: Long): Flow<Expense?> =
        expenseDao.observeExpense(tripId, expenseId).map { it?.toDomain() }

    suspend fun upsert(expense: Expense): Long =
        expenseDao.upsert(expense.toEntity())

    suspend fun deleteById(expenseId: Long) {
        expenseDao.deleteById(expenseId)
    }
}