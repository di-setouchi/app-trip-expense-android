package com.disetouchi.tripexpense.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_id")
    val tripId: Long = 0L,

    @ColumnInfo(name = "trip_name")
    val tripName: String,

    @ColumnInfo(name = "base_currency_code")
    val baseCurrencyCode: String,

    @ColumnInfo(name = "local_currency_codes")
    val localCurrencyCodes: List<String>,

    @ColumnInfo(name = "start_date")
    val startDateEpochDay: Long,

    @ColumnInfo(name = "end_date")
    val endDateEpochDay: Long,

    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long,
)