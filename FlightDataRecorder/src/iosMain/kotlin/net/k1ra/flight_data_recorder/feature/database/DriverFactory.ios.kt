package net.k1ra.flight_data_recorder.feature.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.k1ra.flight_data_recorder.database.FlightDataRecorderDatabase


internal actual object DriverFactory {
    actual fun createDriver(collection: String): SqlDriver {
        return NativeSqliteDriver(FlightDataRecorderDatabase.Schema, "FlightDataRecorder.db")
    }
}