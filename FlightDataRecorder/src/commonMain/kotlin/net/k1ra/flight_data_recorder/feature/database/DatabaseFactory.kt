package net.k1ra.flight_data_recorder.feature.database

import androidx.room3.RoomDatabase
import net.k1ra.sharedprefkmm.util.IODispatcher

/** Platform builder, including the SQLiteDriver. Must be built (not just constructed) so Room initializes its coroutine scope. */
internal expect fun createDatabaseBuilder(collection: String): RoomDatabase.Builder<LogDatabase>

internal fun buildLogDatabase(collection: String): LogDatabase =
    createDatabaseBuilder(collection)
        .setQueryCoroutineContext(IODispatcher)
        .build()