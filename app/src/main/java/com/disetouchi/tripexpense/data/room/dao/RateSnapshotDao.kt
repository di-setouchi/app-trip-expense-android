package com.disetouchi.tripexpense.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.disetouchi.tripexpense.data.room.entity.RateSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RateSnapshotDao {
    @Query(""" SELECT * FROM rate_snapshots WHERE api_parameter_currency_code = :apiParameterCurrency LIMIT 1 """)
    fun observeSnapshot(apiParameterCurrency: String): Flow<RateSnapshotEntity?>

    @Query(""" SELECT * FROM rate_snapshots WHERE api_parameter_currency_code = :apiParameterCurrency LIMIT 1 """)
    suspend fun getSnapshot(apiParameterCurrency: String): RateSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: RateSnapshotEntity)
}