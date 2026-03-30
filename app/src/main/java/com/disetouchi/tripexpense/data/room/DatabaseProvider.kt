package com.disetouchi.tripexpense.data.room

import android.content.Context
import androidx.room.Room

/**
 * Provider to share AppDatabase without DI
 *
 * how to use:
 *   DatabaseProvider.init(applicationContext)
 *   val db = DatabaseProvider.db
 */
object DatabaseProvider {

    @Volatile
    private var _db: AppDatabase? = null

    val db: AppDatabase
        get() = requireNotNull(_db) { "DatabaseProvider is not initialized. Call init(context) first." }

    fun init(context: Context) {
        if (_db != null) return

        synchronized(this) {
            if (_db == null) {
                _db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trip_expense.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }
    }
}