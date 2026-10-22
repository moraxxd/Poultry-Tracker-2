package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.InventoryAdjustment
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryAdjustmentDao {
    @Query("SELECT * FROM inventory_adjustments ORDER BY timestamp DESC")
    fun getAllAdjustmentsFlow(): Flow<List<InventoryAdjustment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: InventoryAdjustment): Long

    @Query("DELETE FROM inventory_adjustments")
    suspend fun deleteAllAdjustments()
}
