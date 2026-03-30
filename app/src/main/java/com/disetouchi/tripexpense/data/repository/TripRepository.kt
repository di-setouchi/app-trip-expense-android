package com.disetouchi.tripexpense.data.repository

import com.disetouchi.tripexpense.data.room.dao.TripDao
import com.disetouchi.tripexpense.data.room.toDomain
import com.disetouchi.tripexpense.data.room.toEntity
import com.disetouchi.tripexpense.domain.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepository(
    private val tripDao: TripDao
) {
    fun observeTrips(): Flow<List<Trip>> =
        tripDao.observeTrips().map { list -> list.map { it.toDomain() } }

    fun observeTrip(tripId: Long): Flow<Trip?> =
        tripDao.observeTrip(tripId).map { it?.toDomain() }

    /**
     * Insert/Replace. Returns rowId (Room behavior).
     */
    suspend fun upsert(trip: Trip): Long =
        tripDao.upsert(trip.toEntity())

    suspend fun deleteById(tripId: Long) {
        tripDao.deleteById(tripId)
        // expenses will be deleted by ForeignKey.CASCADE
    }
}