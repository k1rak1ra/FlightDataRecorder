package net.k1ra.flight_data_recorder.feature.database

import app.cash.sqldelight.db.SqlDriver

internal expect object DriverFactory {
    suspend fun createDriver(collection: String): SqlDriver
}