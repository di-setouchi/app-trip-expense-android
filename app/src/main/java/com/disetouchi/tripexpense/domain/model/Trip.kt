package com.disetouchi.tripexpense.domain.model

import java.time.Instant
import java.time.LocalDate

data class Trip(
    val tripId: Long,
    val tripName: String,
    val baseCurrencyCode: String, // Trip's base currency in app context (NOT Frankfurter API base)
    val localCurrencyCodes: List<String>, // Local currencies used in this trip
    val startDate: LocalDate,
    val endDate: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant
)