package com.disetouchi.tripexpense.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.disetouchi.tripexpense.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Repository for managing application-wide user preferences and session state using DataStore.
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private companion object {
        val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey(Constants.PREF_KEY_LAST_SYNC_TIMESTAMP)
    }

    /**
     * Emits the last successful sync timestamp in epoch milliseconds.
     * Returns 0 if no sync has occurred yet.
     */
    val lastSyncTimestamp: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] ?: 0L
        }

    /**
     * Updates the last successful sync timestamp.
     *
     * @param timestamp The sync timestamp in epoch milliseconds.
     */
    suspend fun updateLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
}