package net.k1ra.flight_data_recorder.feature.database

import net.k1ra.flightdatarecorder.database.BatchLogStoreQueries

internal object DatabaseFactory {
    fun provideDatabase(collection: String) : BatchLogStoreQueries {
        return BatchLogStoreQueries(DriverFactory.createDriver(collection))
    }
}