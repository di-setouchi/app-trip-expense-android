package com.disetouchi.tripexpense.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Provider to manage and provide access to the DataStore instance.
 * Follows the singleton pattern used in DatabaseProvider.
 */
object DataStoreProvider {
    private var _dataStore: DataStore<Preferences>? = null

    val dataStore: DataStore<Preferences>
        get() = requireNotNull(_dataStore) {
            "DataStoreProvider is not initialized. Call init(context) first."
        }

    fun init(context: Context) {
        if (_dataStore != null) return
        _dataStore = context.applicationContext.dataStore
    }
}
