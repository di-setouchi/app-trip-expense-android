package com.disetouchi.tripexpense.domain.model

import java.time.Instant
import java.time.LocalDate

data class Expense(
    val expenseId: Long,
    val tripId: Long,
    val categoryId: Int,
    val occurredAt: LocalDate,
    val localAmountMinor: Long, // Entered amount in localCurrency
    val baseAmountMinor: Long, // Converted & saved amount in baseCurrency
    val localCurrencyCode: String,
    val baseCurrencyCode: String,
    val rateUsedMicros: Long,
    val rateFetchedAt: Instant,
    val note: String,
    val createdAt: Instant,
    val updatedAt: Instant
)