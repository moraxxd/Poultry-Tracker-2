package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.DailyRecord
import com.example.data.entity.Farm
import com.example.data.preferences.FarmPreferences
import com.example.data.repository.FarmRepository
import com.example.domain.model.DashboardStats
import com.example.util.BackupManager
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = FarmRepository(database)
    val preferences = FarmPreferences(application)

    val farm: StateFlow<Farm?> = repository.getFarmFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allRecords: StateFlow<List<DailyRecord>> = repository.getAllRecordsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestRecord: StateFlow<DailyRecord?> = repository.getLatestRecordFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val language: StateFlow<String> = preferences.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val isSetupCompleted: StateFlow<Boolean> = preferences.isSetupCompletedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Operation status messages
    val userMessage = MutableStateFlow<String?>(null)
    val isOperationInProgress = MutableStateFlow(false)

    val dashboardStats: StateFlow<DashboardStats> = combine(
        farm,
        allRecords,
        latestRecord
    ) { currentFarm, records, latest ->
        if (currentFarm == null) {
            DashboardStats()
        } else {
            val todayStr = DateUtils.getCurrentDateString()
            val todayRecord = records.firstOrNull { it.recordDateString == todayStr }

            // Current live stock is derived from the latest chronologically recorded balance,
            // or initial inventory if no records exist yet.
            val currentAdults = latest?.closingAdults ?: currentFarm.initialAdultChickens
            val currentChicks = latest?.closingChicks ?: currentFarm.initialChicks
            val totalBirds = currentAdults + currentChicks

            DashboardStats(
                liveAdultChickens = currentAdults,
                liveChicks = currentChicks,
                totalLiveBirds = totalBirds,
                eggsToday = todayRecord?.eggsCollected ?: 0,
                adultDeathsToday = todayRecord?.adultDeaths ?: 0,
                chickDeathsToday = todayRecord?.chickDeaths ?: 0,
                totalDeathsToday = todayRecord?.totalDeaths ?: 0,
                lastRecordDateString = latest?.recordDateString ?: currentFarm.initialDate,
                lastRecordTimeMillis = latest?.recordTimeMillis ?: currentFarm.initialDateMillis,
                totalRecordsCount = records.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun setupFarm(
        name: String,
        initialAdults: Int,
        initialChicks: Int,
        initialDate: String,
        preferredLanguage: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isOperationInProgress.value = true
            val farm = Farm(
                id = 1L,
                name = name.trim(),
                initialAdultChickens = initialAdults.coerceAtLeast(0),
                initialChicks = initialChicks.coerceAtLeast(0),
                initialDate = initialDate,
                initialDateMillis = DateUtils.parseDateStringToMillis(initialDate),
                preferredLanguage = preferredLanguage,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveFarm(farm)
            preferences.setLanguage(preferredLanguage)
            preferences.setSetupCompleted(true)
            isOperationInProgress.value = false
            onSuccess()
        }
    }

    fun updateFarmName(newName: String) {
        viewModelScope.launch {
            repository.updateFarmName(newName)
            userMessage.value = "Farm name updated"
        }
    }

    fun setLanguage(langCode: String) {
        viewModelScope.launch {
            preferences.setLanguage(langCode)
            repository.updatePreferredLanguage(langCode)
        }
    }

    fun deleteRecord(recordId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            isOperationInProgress.value = true
            val result = repository.deleteRecord(recordId)
            isOperationInProgress.value = false
            if (result.isSuccess) {
                userMessage.value = "Record deleted"
                onDone()
            } else {
                userMessage.value = "Failed to delete record: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            isOperationInProgress.value = true
            repository.resetAllData()
            preferences.clearPreferences()
            isOperationInProgress.value = false
            onComplete()
        }
    }

    fun restoreFromJson(jsonString: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            isOperationInProgress.value = true
            val parseResult = BackupManager.parseAndValidateBackup(jsonString)
            if (parseResult.isFailure) {
                isOperationInProgress.value = false
                onComplete(false, parseResult.exceptionOrNull()?.message)
                return@launch
            }

            val (farm, records) = parseResult.getOrThrow()
            val restoreResult = repository.restoreDatabase(farm, records)
            if (restoreResult.isSuccess) {
                preferences.setLanguage(farm.preferredLanguage)
                preferences.setSetupCompleted(true)
                isOperationInProgress.value = false
                onComplete(true, null)
            } else {
                isOperationInProgress.value = false
                onComplete(false, restoreResult.exceptionOrNull()?.message)
            }
        }
    }

    fun clearUserMessage() {
        userMessage.value = null
    }
}
