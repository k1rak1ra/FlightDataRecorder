package net.k1ra.flight_data_recorder.feature.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(entities = [LogEntity::class], version = 1)
@ConstructedBy(LogDatabaseConstructor::class)
abstract class LogDatabase : RoomDatabase() {
    abstract fun getDao(): LogDao
}

@Suppress("KotlinNoActualForExpect")
expect object LogDatabaseConstructor : RoomDatabaseConstructor<LogDatabase> {
    override fun initialize(): LogDatabase
}