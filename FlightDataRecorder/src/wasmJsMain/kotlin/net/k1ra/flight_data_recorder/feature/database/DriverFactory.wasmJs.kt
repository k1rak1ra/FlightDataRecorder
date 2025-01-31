package net.k1ra.flight_data_recorder.feature.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import net.k1ra.flight_data_recorder.database.FlightDataRecorderDatabase

internal actual object DriverFactory {
    actual suspend fun createDriver(collection: String): SqlDriver {
        val driver = createDefaultWebWorkerDriver()
        FlightDataRecorderDatabase.Schema.awaitCreate(driver)
        return driver
    }
}