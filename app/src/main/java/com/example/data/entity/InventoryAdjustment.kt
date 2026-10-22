package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_adjustments")
data class InventoryAdjustment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordDateString: String,
    val birdType: String, // "ADULT" or "CHICK"
    val calculatedCount: Int,
    val actualCount: Int,
    val adjustmentDifference: Int, // actual - calculated
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
