package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShotDao {
    @Query("SELECT * FROM saved_shots ORDER BY timestamp DESC")
    fun getAllShots(): Flow<List<ShotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShot(shot: ShotEntity): Long

    @Query("DELETE FROM saved_shots WHERE id = :id")
    suspend fun deleteShotById(id: Long)

    @Query("DELETE FROM saved_shots")
    suspend fun clearAll()
}
