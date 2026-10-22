package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.DailyRecord
import com.example.data.entity.Farm
import com.example.data.repository.FarmRepository
import com.example.domain.model.InventoryCalculator
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ReportPeriod {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL_TIME,
    CUSTOM
}

data class ChartPoint(
    val label: String,
    val value: Float,
    val secondaryValue: Float = 0f
)

data class ReportsUiState(
    val selectedPeriod: ReportPeriod = ReportPeriod.LAST_7_DAYS,
    val customStartDate: String = DateUtils.getDaysAgoDateString(7),
    val customEndDate: String = DateUtils.getCurrentDateString(),

    // Eggs metrics
    val eggsToday: Int = 0,
    val eggsLast7Days: Int = 0,
    val eggsLast30Days: Int = 0,
    val eggsPeriodTotal: Int = 0,
    val avgDailyEggProduction: Double = 0.0,

    // Mortality metrics
    val totalAdultDeaths: Int = 0,
    val totalChickDeaths: Int = 0,
    val totalDeaths: Int = 0,
    val periodMortalityRatePercent: Double? = null,

    // Current stock metrics
    val currentAdults: Int = 0,
    val currentChicks: Int = 0,
    val currentTotalLiveBirds: Int = 0,

    // Charts
    val eggChartPoints: List<ChartPoint> = emptyList(),
    val deathChartPoints: List<ChartPoint> = emptyList(),
    val populationChartPoints: List<ChartPoint> = emptyList(),

    val recordsInPeriod: List<DailyRecord> = emptyList(),
    val hasData: Boolean = false
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmRepository(AppDatabase.getInstance(application))

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.LAST_7_DAYS)
    private val _customStartDate = MutableStateFlow(DateUtils.getDaysAgoDateString(7))
    private val _customEndDate = MutableStateFlow(DateUtils.getCurrentDateString())

    val uiState: StateFlow<ReportsUiState> = combine(
        repository.getFarmFlow(),
        repository.getAllRecordsFlow(),
        _selectedPeriod,
        _customStartDate,
        _customEndDate
    ) { farm, allRecords, period, customStart, customEnd ->
        computeReportsState(farm, allRecords, period, customStart, customEnd)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun setCustomDateRange(start: String, end: String) {
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedPeriod.value = ReportPeriod.CUSTOM
    }

    private fun computeReportsState(
        farm: Farm?,
        allRecords: List<DailyRecord>,
        period: ReportPeriod,
        customStart: String,
        customEnd: String
    ): ReportsUiState {
        val todayStr = DateUtils.getCurrentDateString()
        val sevenDaysAgoStr = DateUtils.getDaysAgoDateString(7)
        val thirtyDaysAgoStr = DateUtils.getDaysAgoDateString(30)

        val eggsToday = allRecords.firstOrNull { it.recordDateString == todayStr }?.eggsCollected ?: 0
        val eggs7 = allRecords.filter { it.recordDateString >= sevenDaysAgoStr }.sumOf { it.eggsCollected }
        val eggs30 = allRecords.filter { it.recordDateString >= thirtyDaysAgoStr }.sumOf { it.eggsCollected }

        // Current stock
        val latest = allRecords.maxByOrNull { it.recordDateString }
        val currentAdults = latest?.closingAdults ?: farm?.initialAdultChickens ?: 0
        val currentChicks = latest?.closingChicks ?: farm?.initialChicks ?: 0
        val currentTotalLive = currentAdults + currentChicks

        // Filter for selected period
        val periodRecords = when (period) {
            ReportPeriod.TODAY -> allRecords.filter { it.recordDateString == todayStr }
            ReportPeriod.LAST_7_DAYS -> allRecords.filter { it.recordDateString >= sevenDaysAgoStr }
            ReportPeriod.LAST_30_DAYS -> allRecords.filter { it.recordDateString >= thirtyDaysAgoStr }
            ReportPeriod.ALL_TIME -> allRecords
            ReportPeriod.CUSTOM -> allRecords.filter {
                it.recordDateString in customStart..customEnd
            }
        }.sortedBy { it.recordDateString }

        val periodEggsTotal = periodRecords.sumOf { it.eggsCollected }
        val avgEggs = if (periodRecords.isNotEmpty()) {
            periodEggsTotal.toDouble() / periodRecords.size.toDouble()
        } else {
            0.0
        }

        val totalAdultDeaths = periodRecords.sumOf { it.adultDeaths }
        val totalChickDeaths = periodRecords.sumOf { it.chickDeaths }
        val totalDeaths = totalAdultDeaths + totalChickDeaths

        // Average daily mortality rate across records in period
        val openingLiveSum = periodRecords.sumOf { it.openingTotalLiveBirds }
        val mortalityRate: Double? = if (openingLiveSum > 0) {
            (totalDeaths.toDouble() / openingLiveSum.toDouble()) * 100.0
        } else {
            null
        }

        // Build chart points
        val eggPoints = periodRecords.map {
            ChartPoint(
                label = it.recordDateString.substring(5), // MM-DD
                value = it.eggsCollected.toFloat()
            )
        }

        val deathPoints = periodRecords.map {
            ChartPoint(
                label = it.recordDateString.substring(5),
                value = it.adultDeaths.toFloat(),
                secondaryValue = it.chickDeaths.toFloat()
            )
        }

        val populationPoints = periodRecords.map {
            ChartPoint(
                label = it.recordDateString.substring(5),
                value = it.closingAdults.toFloat(),
                secondaryValue = it.closingChicks.toFloat()
            )
        }

        return ReportsUiState(
            selectedPeriod = period,
            customStartDate = customStart,
            customEndDate = customEnd,
            eggsToday = eggsToday,
            eggsLast7Days = eggs7,
            eggsLast30Days = eggs30,
            eggsPeriodTotal = periodEggsTotal,
            avgDailyEggProduction = avgEggs,
            totalAdultDeaths = totalAdultDeaths,
            totalChickDeaths = totalChickDeaths,
            totalDeaths = totalDeaths,
            periodMortalityRatePercent = mortalityRate,
            currentAdults = currentAdults,
            currentChicks = currentChicks,
            currentTotalLiveBirds = currentTotalLive,
            eggChartPoints = eggPoints,
            deathChartPoints = deathPoints,
            populationChartPoints = populationPoints,
            recordsInPeriod = periodRecords,
            hasData = periodRecords.isNotEmpty()
        )
    }
}
