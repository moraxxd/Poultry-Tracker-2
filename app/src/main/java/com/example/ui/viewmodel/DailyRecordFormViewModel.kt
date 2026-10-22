package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.DailyRecord
import com.example.data.repository.FarmRepository
import com.example.domain.model.InventoryCalculator
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecordFormUiState(
    val recordId: Long? = null,
    val isEditMode: Boolean = false,
    val recordDateString: String = DateUtils.getCurrentDateString(),
    val recordTimeMillis: Long = System.currentTimeMillis(),

    // Adults
    val openingAdults: Int = 0,
    val adultsAdded: String = "0",
    val adultDeaths: String = "0",
    val adultsSold: String = "0",
    val manualClosingAdults: String = "",

    // Chicks
    val openingChicks: Int = 0,
    val chicksAdded: String = "0",
    val chickDeaths: String = "0",
    val chicksSold: String = "0",
    val manualClosingChicks: String = "",

    // Eggs
    val eggsCollected: String = "0",

    // Notes
    val notes: String = "",
    val healthObservations: String = "",
    val treatments: String = "",

    // Flags & Validations
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val warningMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    val isDateOverwriting: Boolean = false
) {
    val intAdultsAdded: Int get() = adultsAdded.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val intAdultDeaths: Int get() = adultDeaths.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val intAdultsSold: Int get() = adultsSold.toIntOrNull()?.coerceAtLeast(0) ?: 0

    val intChicksAdded: Int get() = chicksAdded.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val intChickDeaths: Int get() = chickDeaths.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val intChicksSold: Int get() = chicksSold.toIntOrNull()?.coerceAtLeast(0) ?: 0

    val intEggsCollected: Int get() = eggsCollected.toIntOrNull()?.coerceAtLeast(0) ?: 0

    val calculatedClosingAdults: Int
        get() = InventoryCalculator.calculateClosingAdults(
            openingAdults, intAdultsAdded, intAdultDeaths, intAdultsSold
        )

    val calculatedClosingChicks: Int
        get() = InventoryCalculator.calculateClosingChicks(
            openingChicks, intChicksAdded, intChickDeaths, intChicksSold
        )

    val isAdultNegative: Boolean get() = calculatedClosingAdults < 0
    val isChickNegative: Boolean get() = calculatedClosingChicks < 0

    val totalLiveBirds: Int
        get() = InventoryCalculator.calculateTotalLiveBirds(
            closingAdults = if (manualClosingAdults.isNotBlank()) manualClosingAdults.toIntOrNull() ?: calculatedClosingAdults else calculatedClosingAdults,
            closingChicks = if (manualClosingChicks.isNotBlank()) manualClosingChicks.toIntOrNull() ?: calculatedClosingChicks else calculatedClosingChicks
        )

    val totalDeathsToday: Int
        get() = InventoryCalculator.calculateTotalDeaths(intAdultDeaths, intChickDeaths)

    val isAdultMismatch: Boolean
        get() = manualClosingAdults.isNotBlank() && (manualClosingAdults.toIntOrNull() != calculatedClosingAdults)

    val isChickMismatch: Boolean
        get() = manualClosingChicks.isNotBlank() && (manualClosingChicks.toIntOrNull() != calculatedClosingChicks)

    val canSave: Boolean
        get() = !isSaving && !isAdultNegative && !isChickNegative
}

class DailyRecordFormViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(RecordFormUiState())
    val uiState: StateFlow<RecordFormUiState> = _uiState.asStateFlow()

    fun initialize(recordId: Long? = null, initialDate: String? = null) {
        viewModelScope.launch {
            if (recordId != null && recordId > 0) {
                val record = repository.getRecordById(recordId)
                if (record != null) {
                    _uiState.value = RecordFormUiState(
                        recordId = record.id,
                        isEditMode = true,
                        recordDateString = record.recordDateString,
                        recordTimeMillis = record.recordTimeMillis,
                        openingAdults = record.openingAdults,
                        adultsAdded = record.adultsAdded.toString(),
                        adultDeaths = record.adultDeaths.toString(),
                        adultsSold = record.adultsSold.toString(),
                        manualClosingAdults = if (record.closingAdults != record.openingAdults + record.adultsAdded - record.adultDeaths - record.adultsSold) record.closingAdults.toString() else "",
                        openingChicks = record.openingChicks,
                        chicksAdded = record.chicksAdded.toString(),
                        chickDeaths = record.chickDeaths.toString(),
                        chicksSold = record.chicksSold.toString(),
                        manualClosingChicks = if (record.closingChicks != record.openingChicks + record.chicksAdded - record.chickDeaths - record.chicksSold) record.closingChicks.toString() else "",
                        eggsCollected = record.eggsCollected.toString(),
                        notes = record.notes,
                        healthObservations = record.healthObservations,
                        treatments = record.treatments
                    )
                    return@launch
                }
            }

            // New record mode
            val targetDate = initialDate ?: DateUtils.getCurrentDateString()
            loadOpeningInventoryForDate(targetDate)
        }
    }

    private suspend fun loadOpeningInventoryForDate(dateStr: String) {
        val (openingA, openingC) = repository.getOpeningInventoryForDate(dateStr)
        val existingRecord = repository.getRecordByDate(dateStr)

        if (existingRecord != null) {
            // Load existing record for this date
            _uiState.value = RecordFormUiState(
                recordId = existingRecord.id,
                isEditMode = true,
                recordDateString = existingRecord.recordDateString,
                recordTimeMillis = existingRecord.recordTimeMillis,
                openingAdults = existingRecord.openingAdults,
                adultsAdded = existingRecord.adultsAdded.toString(),
                adultDeaths = existingRecord.adultDeaths.toString(),
                adultsSold = existingRecord.adultsSold.toString(),
                openingChicks = existingRecord.openingChicks,
                chicksAdded = existingRecord.chicksAdded.toString(),
                chickDeaths = existingRecord.chickDeaths.toString(),
                chicksSold = existingRecord.chicksSold.toString(),
                eggsCollected = existingRecord.eggsCollected.toString(),
                notes = existingRecord.notes,
                healthObservations = existingRecord.healthObservations,
                treatments = existingRecord.treatments,
                isDateOverwriting = true
            )
        } else {
            _uiState.value = _uiState.value.copy(
                recordId = null,
                isEditMode = false,
                recordDateString = dateStr,
                openingAdults = openingA,
                openingChicks = openingC,
                isDateOverwriting = false
            )
        }
    }

    fun onDateChanged(newDate: String) {
        viewModelScope.launch {
            loadOpeningInventoryForDate(newDate)
        }
    }

    fun onTimeChanged(newTimeMillis: Long) {
        _uiState.value = _uiState.value.copy(recordTimeMillis = newTimeMillis)
    }

    fun onAdultsAddedChanged(v: String) {
        _uiState.value = _uiState.value.copy(adultsAdded = sanitizeNumberInput(v))
    }

    fun onAdultDeathsChanged(v: String) {
        _uiState.value = _uiState.value.copy(adultDeaths = sanitizeNumberInput(v))
    }

    fun onAdultsSoldChanged(v: String) {
        _uiState.value = _uiState.value.copy(adultsSold = sanitizeNumberInput(v))
    }

    fun onManualClosingAdultsChanged(v: String) {
        _uiState.value = _uiState.value.copy(manualClosingAdults = sanitizeNumberInput(v))
    }

    fun onChicksAddedChanged(v: String) {
        _uiState.value = _uiState.value.copy(chicksAdded = sanitizeNumberInput(v))
    }

    fun onChickDeathsChanged(v: String) {
        _uiState.value = _uiState.value.copy(chickDeaths = sanitizeNumberInput(v))
    }

    fun onChicksSoldChanged(v: String) {
        _uiState.value = _uiState.value.copy(chicksSold = sanitizeNumberInput(v))
    }

    fun onManualClosingChicksChanged(v: String) {
        _uiState.value = _uiState.value.copy(manualClosingChicks = sanitizeNumberInput(v))
    }

    fun onEggsCollectedChanged(v: String) {
        _uiState.value = _uiState.value.copy(eggsCollected = sanitizeNumberInput(v))
    }

    fun onNotesChanged(v: String) {
        _uiState.value = _uiState.value.copy(notes = v)
    }

    fun onHealthObservationsChanged(v: String) {
        _uiState.value = _uiState.value.copy(healthObservations = v)
    }

    fun onTreatmentsChanged(v: String) {
        _uiState.value = _uiState.value.copy(treatments = v)
    }

    private fun sanitizeNumberInput(input: String): String {
        return input.filter { it.isDigit() }
    }

    fun saveRecord(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return

        if (state.isAdultNegative) {
            _uiState.value = state.copy(errorMessage = "Closing adult chicken count cannot be negative (${state.calculatedClosingAdults}).")
            return
        }
        if (state.isChickNegative) {
            _uiState.value = state.copy(errorMessage = "Closing chick count cannot be negative (${state.calculatedClosingChicks}).")
            return
        }

        _uiState.value = state.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            val record = DailyRecord(
                id = state.recordId ?: 0L,
                farmId = 1L,
                recordDateString = state.recordDateString,
                dateMillis = DateUtils.parseDateStringToMillis(state.recordDateString),
                recordTimeMillis = state.recordTimeMillis,
                openingAdults = state.openingAdults,
                adultsAdded = state.intAdultsAdded,
                adultDeaths = state.intAdultDeaths,
                adultsSold = state.intAdultsSold,
                closingAdults = state.calculatedClosingAdults,
                openingChicks = state.openingChicks,
                chicksAdded = state.intChicksAdded,
                chickDeaths = state.intChickDeaths,
                chicksSold = state.intChicksSold,
                closingChicks = state.calculatedClosingChicks,
                eggsCollected = state.intEggsCollected,
                notes = state.notes.trim(),
                healthObservations = state.healthObservations.trim(),
                treatments = state.treatments.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val manualAdults = state.manualClosingAdults.toIntOrNull()
            val manualChicks = state.manualClosingChicks.toIntOrNull()

            val result = repository.saveDailyRecord(
                record = record,
                manualClosingAdults = manualAdults,
                manualClosingChicks = manualChicks,
                adjustmentReason = "User manual closing adjustment on ${state.recordDateString}"
            )

            if (result.isSuccess) {
                _uiState.value = state.copy(
                    isSaving = false,
                    isSavedSuccess = true,
                    errorMessage = null
                )
                onSuccess()
            } else {
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to save record"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
