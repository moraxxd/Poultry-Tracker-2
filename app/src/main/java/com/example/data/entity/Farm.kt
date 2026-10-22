package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farms")
data class Farm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 1,
    val name: String,
    val initialAdultChickens: Int,
    val initialChicks: Int,
    val initialDate: String, // YYYY-MM-DD
    val initialDateMillis: Long,
    val preferredLanguage: String = "en", // "en" or "hi"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
