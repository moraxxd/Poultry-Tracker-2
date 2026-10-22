package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Farm
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms ORDER BY id ASC LIMIT 1")
    fun getFarmFlow(): Flow<Farm?>

    @Query("SELECT * FROM farms ORDER BY id ASC LIMIT 1")
    suspend fun getFarm(): Farm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: Farm): Long

    @Update
    suspend fun updateFarm(farm: Farm)

    @Query("DELETE FROM farms")
    suspend fun deleteAllFarms()
}
