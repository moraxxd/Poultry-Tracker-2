package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_records",
    indices = [
        Index(value = ["recordDateString"], unique = true),
        Index(value = ["dateMillis"])
    ]
)
data class DailyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val farmId: Long = 1,
    val recordDateString: String, // YYYY-MM-DD
    val dateMillis: Long,
    val recordTimeMillis: Long,

    // Adult chickens
    val openingAdults: Int,
    val adultsAdded: Int = 0,
    val adultDeaths: Int = 0,
    val adultsSold: Int = 0,
    val closingAdults: Int,

    // Chicks
    val openingChicks: Int,
    val chicksAdded: Int = 0,
    val chickDeaths: Int = 0,
    val chicksSold: Int = 0,
    val closingChicks: Int,

    // Egg production
    val eggsCollected: Int = 0,

    // Observations & Health
    val notes: String = "",
    val healthObservations: String = "",
    val treatments: String = "",

    // Audit
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalLiveBirds: Int get() = closingAdults + closingChicks
    val totalDeaths: Int get() = adultDeaths + chickDeaths
    val openingTotalLiveBirds: Int get() = openingAdults + openingChicks

    /**
     * Daily mortality rate percentage = (Total Deaths / Opening Live Birds) * 100
     * Returns null if opening total live birds is 0 to avoid division by zero.
     */
    val dailyMortalityRatePercent: Double?
        get() = if (openingTotalLiveBirds > 0) {
            (totalDeaths.toDouble() / openingTotalLiveBirds.toDouble()) * 100.0
        } else {
            null
        }
}
