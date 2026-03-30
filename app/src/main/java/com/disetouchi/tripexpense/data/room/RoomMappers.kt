package com.disetouchi.tripexpense.data.room

import com.disetouchi.tripexpense.data.room.entity.ExpenseEntity
import com.disetouchi.tripexpense.data.room.entity.RateSnapshotEntity
import com.disetouchi.tripexpense.data.room.entity.TripEntity
import com.disetouchi.tripexpense.domain.model.Expense
import com.disetouchi.tripexpense.domain.model.RateSnapshot
import com.disetouchi.tripexpense.domain.model.Trip
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.time.Instant
import java.time.LocalDate

/**
 * Minimal mappers between Room entities and domain models.
 * - Entity uses epochDay / epochMillis and JSON string
 * - Domain uses LocalDate / Instant and Map<String, Double>
 */

// ---------- Time helpers ----------
private fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)
private fun LocalDate.toEpochDayLong(): Long = this.toEpochDay()

private fun Long.toInstant(): Instant = Instant.ofEpochMilli(this)
private fun Instant.toEpochMillisLong(): Long = this.toEpochMilli()

// ---------- Rates JSON helpers ----------
private object RatesJson {
    private val moshi: Moshi = Moshi.Builder().build()

    private val mapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Double::class.javaObjectType
    )

    private val adapter: JsonAdapter<Map<String, Double>> = moshi.adapter(mapType)

    fun toJson(rates: Map<String, Double>): String = adapter.toJson(rates)

    fun fromJson(json: String): Map<String, Double> =
        adapter.fromJson(json) ?: emptyMap()
}

// ---------- Trip ----------
fun TripEntity.toDomain(): Trip = Trip(
    tripId = tripId,
    tripName = tripName,
    baseCurrencyCode = baseCurrencyCode,
    localCurrencyCodes = localCurrencyCodes,
    startDate = startDateEpochDay.toLocalDate(),
    endDate = endDateEpochDay.toLocalDate(),
    createdAt = createdAtEpochMillis.toInstant(),
    updatedAt = updatedAtEpochMillis.toInstant()
)

fun Trip.toEntity(): TripEntity = TripEntity(
    tripId = tripId,
    tripName = tripName,
    baseCurrencyCode = baseCurrencyCode,
    localCurrencyCodes = localCurrencyCodes,
    startDateEpochDay = startDate.toEpochDayLong(),
    endDateEpochDay = endDate.toEpochDayLong(),
    createdAtEpochMillis = createdAt.toEpochMillisLong(),
    updatedAtEpochMillis = updatedAt.toEpochMillisLong()
)

// ---------- Expense ----------
fun ExpenseEntity.toDomain(): Expense = Expense(
    expenseId = expenseId,
    tripId = tripId,
    categoryId = categoryId,
    occurredAt = occurredAtEpochDay.toLocalDate(),
    localAmountMinor = localAmountMinor,
    baseAmountMinor = baseAmountMinor,
    localCurrencyCode = localCurrencyCode,
    baseCurrencyCode = baseCurrencyCode,
    rateUsedMicros = rateUsedMicros,
    rateFetchedAt = rateFetchedAtEpochMillis.toInstant(),
    note = note,
    createdAt = createdAtEpochMillis.toInstant(),
    updatedAt = updatedAtEpochMillis.toInstant()
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    expenseId = expenseId,
    tripId = tripId,
    categoryId = categoryId,
    occurredAtEpochDay = occurredAt.toEpochDayLong(),
    localAmountMinor = localAmountMinor,
    baseAmountMinor = baseAmountMinor,
    localCurrencyCode = localCurrencyCode,
    baseCurrencyCode = baseCurrencyCode,
    rateUsedMicros = rateUsedMicros,
    rateFetchedAtEpochMillis = rateFetchedAt.toEpochMillisLong(),
    note = note,
    createdAtEpochMillis = createdAt.toEpochMillisLong(),
    updatedAtEpochMillis = updatedAt.toEpochMillisLong()
)

// ---------- RateSnapshot ----------
fun RateSnapshotEntity.toDomain(): RateSnapshot = RateSnapshot(
    localCurrencyCode = apiParameterCurrency,
    rates = RatesJson.fromJson(ratesJson),
    rateFetchedAt = rateFetchedAtEpochMillis.toInstant(),
    frankfurterApiReferenceDate = frankfurterApiReferenceDate
)

fun RateSnapshot.toEntity(): RateSnapshotEntity = RateSnapshotEntity(
    apiParameterCurrency = localCurrencyCode,
    ratesJson = RatesJson.toJson(rates),
    rateFetchedAtEpochMillis = rateFetchedAt.toEpochMillisLong(),
    frankfurterApiReferenceDate = frankfurterApiReferenceDate
)
