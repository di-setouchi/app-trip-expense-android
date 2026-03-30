package com.disetouchi.tripexpense.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.disetouchi.tripexpense.data.room.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query(
        """
        SELECT * FROM expenses
        WHERE trip_id = :tripId
        ORDER BY occurred_at DESC, expense_id DESC
    """
    )
    fun observeExpensesByTrip(tripId: Long): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT * FROM expenses
        WHERE expense_id = :expenseId AND trip_id = :tripId
        LIMIT 1
    """
    )
    fun observeExpense(tripId: Long, expenseId: Long): Flow<ExpenseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: ExpenseEntity): Long


    @Query("DELETE FROM expenses WHERE expense_id = :expenseId")
    suspend fun deleteById(expenseId: Long)
}