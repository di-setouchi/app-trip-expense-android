package com.disetouchi.tripexpense.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Frankfurter API rate snapshot.
 *
 * We keep only the latest snapshot per base currency (replace on update).
 */
@Entity(tableName = "rate_snapshots")
data class RateSnapshotEntity(
    @PrimaryKey
    @ColumnInfo(name = "api_parameter_currency_code")
    val apiParameterCurrency: String, // PrimaryKey = apiParameterCurrency (keep latest per API base currency)

    @ColumnInfo(name = "rates_json")
    val ratesJson: String,

    @ColumnInfo(name = "rate_fetched_at")
    val rateFetchedAtEpochMillis: Long,

    @ColumnInfo(name = "frankfurter_api_reference_date")
    val frankfurterApiReferenceDate: String, // Reference date from API
)