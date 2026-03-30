package com.disetouchi.tripexpense.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.disetouchi.tripexpense.data.room.dao.ExpenseDao
import com.disetouchi.tripexpense.data.room.dao.RateSnapshotDao
import com.disetouchi.tripexpense.data.room.dao.TripDao
import com.disetouchi.tripexpense.data.room.entity.ExpenseEntity
import com.disetouchi.tripexpense.data.room.entity.RateSnapshotEntity
import com.disetouchi.tripexpense.data.room.entity.TripEntity

@Database(
    entities = [
        TripEntity::class,
        ExpenseEntity::class,
        RateSnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun rateSnapshotDao(): RateSnapshotDao
}