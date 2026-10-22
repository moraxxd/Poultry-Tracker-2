package com.example.domain.model

data class DashboardStats(
    val liveAdultChickens: Int = 0,
    val liveChicks: Int = 0,
    val totalLiveBirds: Int = 0,
    val eggsToday: Int = 0,
    val adultDeathsToday: Int = 0,
    val chickDeathsToday: Int = 0,
    val totalDeathsToday: Int = 0,
    val lastRecordDateString: String? = null,
    val lastRecordTimeMillis: Long? = null,
    val totalRecordsCount: Int = 0
)
