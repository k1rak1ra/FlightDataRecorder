package net.k1ra.flight_data_recorder.feature.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface LogDao {
    @Insert
    suspend fun insert(item: LogEntity)

    @Query("SELECT count(*) FROM LogEntity")
    suspend fun countAll() : Int

    @Query("SELECT * FROM LogEntity")
    suspend fun getAll() : List<LogEntity>

    @Query("SELECT count(*) FROM LogEntity WHERE (iv = :iv)")
    suspend fun getByIv(iv: ByteArray) : Int

    @Query("DELETE FROM LogEntity WHERE (id <= :id)")
    suspend fun deleteBeforeId(id: Long)
}