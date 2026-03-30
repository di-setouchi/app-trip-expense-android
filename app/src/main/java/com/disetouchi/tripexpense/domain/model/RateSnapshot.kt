package com.disetouchi.tripexpense.domain.model

import java.time.Instant

/**
 * Snapshot of rates returned by Frankfurter API.
 *
 * NOTE:
 * localCurrencyCode is Frankfurter's "base currency.
 */
data class RateSnapshot(
    val localCurrencyCode: String,
    val rates: Map<String, Double>,
    val rateFetchedAt: Instant,
    val frankfurterApiReferenceDate: String
)