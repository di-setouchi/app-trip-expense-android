package com.disetouchi.tripexpense.data.repository

import com.disetouchi.tripexpense.data.datastore.DataStoreProvider
import com.disetouchi.tripexpense.data.network.NetworkProvider
import com.disetouchi.tripexpense.data.room.DatabaseProvider

/**
 * Provider to share Repository instances without DI.
 */
object RepositoryProvider {
    val tripRepository: TripRepository by lazy {
        TripRepository(DatabaseProvider.db.tripDao())
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(DatabaseProvider.db.expenseDao())
    }

    val rateSnapshotRepository: RateSnapshotRepository by lazy {
        RateSnapshotRepository(
            rateSnapshotDao = DatabaseProvider.db.rateSnapshotDao(),
            apiService = NetworkProvider.frankfurterApi
        )
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(DataStoreProvider.dataStore)
    }
}
