package net.k1ra.flight_data_recorder.feature.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.k1ra.flight_data_recorder.database.FlightDataRecorderDatabase
import net.k1ra.sharedprefkmm.SharedPrefKmmInitContentProvider
import net.k1ra.sharedprefkmm.util.TestConfig

internal actual object DriverFactory {
    actual fun createDriver(collection: String): SqlDriver {
        //If we're in test mode and can't use Context, use in-memory DB instead
        return if (TestConfig.testMode) {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                FlightDataRecorderDatabase.Schema.create(this)
            }
        } else {
            AndroidSqliteDriver(
                FlightDataRecorderDatabase.Schema,
                SharedPrefKmmInitContentProvider.appContext,
                "FlightDataRecorder.db"
            )
        }
    }
}