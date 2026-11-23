package net.k1ra.flight_data_recorder.feature.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.eygraber.sqldelight.androidx.driver.opfs.opfsWorker

internal actual fun createDatabaseBuilder(collection: String): RoomDatabase.Builder<LogDatabase> {
    return Room.databaseBuilder<LogDatabase>("FDR-$collection.db") {
        LogDatabaseConstructor.initialize()
    }.setDriver(WebWorkerSQLiteDriver(opfsWorker().worker))
}