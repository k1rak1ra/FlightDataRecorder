package net.k1ra.flight_data_recorder.feature.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import net.k1ra.hoodies_network_kmm.cookies.database.databaseDirectory

internal actual fun createDatabaseBuilder(collection: String): RoomDatabase.Builder<LogDatabase> {
    return Room.databaseBuilder<LogDatabase>("${databaseDirectory()}/FDR-$collection.db") {
        LogDatabaseConstructor.initialize()
    }.setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
}