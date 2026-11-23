package net.k1ra.flight_data_recorder.feature.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: ByteArray,
    val iv: ByteArray
)