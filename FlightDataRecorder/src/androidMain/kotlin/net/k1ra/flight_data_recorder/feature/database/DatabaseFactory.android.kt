package net.k1ra.flight_data_recorder.feature.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import net.k1ra.sharedprefkmm.SharedPrefKmmInitContentProvider
import net.k1ra.sharedprefkmm.util.TestConfig

internal actual fun createDatabaseBuilder(collection: String): RoomDatabase.Builder<LogDatabase> {
    //If we're in test mode and can't use Context, use in-memory DB instead
    if (TestConfig.testMode)
        return Room.inMemoryDatabaseBuilder<LogDatabase> { LogDatabaseConstructor.initialize() }
            .setDriver(BundledSQLiteDriver())

    return Room.databaseBuilder<LogDatabase>(
        context = SharedPrefKmmInitContentProvider.appContext,
        name = "FDR-$collection.db"
    ) { LogDatabaseConstructor.initialize() }.setDriver(BundledSQLiteDriver())
}