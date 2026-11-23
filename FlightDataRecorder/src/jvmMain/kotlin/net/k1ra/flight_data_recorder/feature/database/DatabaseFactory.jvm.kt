package net.k1ra.flight_data_recorder.feature.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

internal actual fun createDatabaseBuilder(collection: String): RoomDatabase.Builder<LogDatabase> {
    return Room.databaseBuilder<LogDatabase>(File(System.getProperty("java.io.tmpdir"), "$collection.db").absolutePath) {
        LogDatabaseConstructor.initialize()
    }.setDriver(BundledSQLiteDriver())
}