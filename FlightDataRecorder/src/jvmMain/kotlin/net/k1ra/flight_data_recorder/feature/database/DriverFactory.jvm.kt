package net.k1ra.flight_data_recorder.feature.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.k1ra.flight_data_recorder.database.FlightDataRecorderDatabase
import net.k1ra.sharedprefkmm.StorageManager
import java.io.File

internal actual object DriverFactory {
    actual suspend fun createDriver(collection: String): SqlDriver {
        val dbLocation = File(StorageManager.getLocalStorageDir(collection))
        if (!dbLocation.exists())
            dbLocation.mkdirs()

        val dbFile = File("${StorageManager.getLocalStorageDir(collection)}FDRBatch.db")
        return if (dbFile.exists()) {
            JdbcSqliteDriver("jdbc:sqlite:${StorageManager.getLocalStorageDir(collection)}FDRBatch.db")
        } else {
            JdbcSqliteDriver("jdbc:sqlite:${StorageManager.getLocalStorageDir(collection)}FDRBatch.db").apply {
                FlightDataRecorderDatabase.Schema.awaitCreate(this)
            }
        }
    }
}