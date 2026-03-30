package com.disetouchi.tripexpense.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.disetouchi.tripexpense.data.room.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY updated_at DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE trip_id = :tripId LIMIT 1")
    fun observeTrip(tripId: Long): Flow<TripEntity?>

    @Upsert
    suspend fun upsert(trip: TripEntity): Long

    @Query("DELETE FROM trips WHERE trip_id = :tripId")
    suspend fun deleteById(tripId: Long)
}