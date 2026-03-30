package com.disetouchi.tripexpense.data.repository

import com.disetouchi.tripexpense.data.network.FrankfurterApiService
import com.disetouchi.tripexpense.data.network.NetworkProvider
import com.disetouchi.tripexpense.data.room.dao.RateSnapshotDao
import com.disetouchi.tripexpense.data.room.entity.RateSnapshotEntity
import com.disetouchi.tripexpense.data.room.toDomain
import com.disetouchi.tripexpense.domain.model.RateSnapshot
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class RateSnapshotRepository(
    private val rateSnapshotDao: RateSnapshotDao,
    private val apiService: FrankfurterApiService
) {
    fun observeSnapshot(localCurrencyCode: String): Flow<RateSnapshot?> =
        rateSnapshotDao.observeSnapshot(localCurrencyCode).map { it?.toDomain() }

    suspend fun getSnapshot(localCurrencyCode: String): RateSnapshot? =
        rateSnapshotDao.getSnapshot(localCurrencyCode)?.toDomain()

    /**
     * Fetches the latest exchange rates from the Frankfurter API and saves them to the Room database.
     *
     * @param localCurrencyCode The base currency code to fetch rates for.
     * @return Result.success if the operation was successful, Result.failure otherwise.
     */
    suspend fun fetchAndSaveRate(localCurrencyCode: String): Result<Unit> {
        return try {
            // Fetch data from API
            val response = apiService.getLatestRates(localCurrencyCode)

            // Convert Map<String, Double> to JSON String using Moshi
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType)
            val jsonAdapter = NetworkProvider.moshi.adapter<Map<String, Double>>(mapType)
            val ratesJsonString = jsonAdapter.toJson(response.rates)

            // Create Entity for Room
            val entity = RateSnapshotEntity(
                apiParameterCurrency = localCurrencyCode,
                ratesJson = ratesJsonString,
                rateFetchedAtEpochMillis = Instant.now().toEpochMilli(),
                frankfurterApiReferenceDate = response.date
            )

            // Save to Room(RateSnapshotEntity)
            rateSnapshotDao.upsert(entity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}