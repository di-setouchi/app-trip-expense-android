package com.disetouchi.tripexpense.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["trip_id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["trip_id", "occurred_at"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "expense_id")
    val expenseId: Long = 0L,

    @ColumnInfo(name = "trip_id")
    val tripId: Long,

    @ColumnInfo(name = "category_id")
    val categoryId: Int,

    @ColumnInfo(name = "occurred_at")
    val occurredAtEpochDay: Long,

    @ColumnInfo(name = "local_amount_minor")
    val localAmountMinor: Long,

    @ColumnInfo(name = "base_amount_minor")
    val baseAmountMinor: Long,

    @ColumnInfo(name = "local_currency_code")
    val localCurrencyCode: String,

    @ColumnInfo(name = "base_currency_code")
    val baseCurrencyCode: String,

    @ColumnInfo(name = "rate_used_micros")
    val rateUsedMicros: Long,

    @ColumnInfo(name = "rate_fetched_at")
    val rateFetchedAtEpochMillis: Long,

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long,
)