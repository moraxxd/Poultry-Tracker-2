package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.AppDatabase
import com.example.data.entity.DailyRecord
import com.example.data.entity.Farm
import com.example.data.entity.InventoryAdjustment
import com.example.domain.model.InventoryCalculator
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

class FarmRepository(private val database: AppDatabase) {

    private val farmDao = database.farmDao()
    private val recordDao = database.dailyRecordDao()
    private val adjustmentDao = database.inventoryAdjustmentDao()

    // -------------------------------------------------------------
    // Farm Profile
    // -------------------------------------------------------------

    fun getFarmFlow(): Flow<Farm?> = farmDao.getFarmFlow()

    suspend fun getFarm(): Farm? = farmDao.getFarm()

    suspend fun saveFarm(farm: Farm): Long {
        return farmDao.insertFarm(farm)
    }

    suspend fun updateFarmName(newName: String) {
        val current = farmDao.getFarm() ?: return
        farmDao.updateFarm(current.copy(name = newName.trim(), updatedAt = System.currentTimeMillis()))
    }

    suspend fun updatePreferredLanguage(newLang: String) {
        val current = farmDao.getFarm() ?: return
        farmDao.updateFarm(current.copy(preferredLanguage = newLang, updatedAt = System.currentTimeMillis()))
    }

    // -------------------------------------------------------------
    // Daily Records
    // -------------------------------------------------------------

    fun getAllRecordsFlow(): Flow<List<DailyRecord>> = recordDao.getAllRecordsFlow()

    suspend fun getAllRecordsAsc(): List<DailyRecord> = recordDao.getAllRecordsAsc()

    suspend fun getAllRecordsDesc(): List<DailyRecord> = recordDao.getAllRecordsDesc()

    fun getLatestRecordFlow(): Flow<DailyRecord?> = recordDao.getLatestRecordFlow()

    suspend fun getLatestRecord(): DailyRecord? = recordDao.getLatestRecord()

    fun getRecordByIdFlow(id: Long): Flow<DailyRecord?> = recordDao.getRecordByIdFlow(id)

    suspend fun getRecordById(id: Long): DailyRecord? = recordDao.getRecordById(id)

    suspend fun getRecordByDate(dateString: String): DailyRecord? = recordDao.getRecordByDate(dateString)

    fun getRecordsBetweenDatesFlow(start: String, end: String): Flow<List<DailyRecord>> =
        recordDao.getRecordsBetweenDatesFlow(start, end)

    suspend fun getRecordsBetweenDatesAsc(start: String, end: String): List<DailyRecord> =
        recordDao.getRecordsBetweenDatesAsc(start, end)

    /**
     * Determines opening inventory for a target date string (YYYY-MM-DD).
     * If an earlier record exists, use that preceding record's closing inventory.
     * Otherwise, fallback to initial farm inventory.
     */
    suspend fun getOpeningInventoryForDate(dateString: String): Pair<Int, Int> {
        val preceding = recordDao.getPrecedingRecord(dateString)
        if (preceding != null) {
            return Pair(preceding.closingAdults, preceding.closingChicks)
        }
        val farm = farmDao.getFarm()
        return if (farm != null) {
            Pair(farm.initialAdultChickens, farm.initialChicks)
        } else {
            Pair(0, 0)
        }
    }

    /**
     * Saves a daily record and automatically runs chronological inventory recalculation
     * for all records within a database transaction.
     */
    suspend fun saveDailyRecord(
        record: DailyRecord,
        manualClosingAdults: Int? = null,
        manualClosingChicks: Int? = null,
        adjustmentReason: String = ""
    ): Result<Long> {
        // Enforce inventory calculations
        val calcClosingAdults = InventoryCalculator.calculateClosingAdults(
            record.openingAdults,
            record.adultsAdded,
            record.adultDeaths,
            record.adultsSold
        )
        val calcClosingChicks = InventoryCalculator.calculateClosingChicks(
            record.openingChicks,
            record.chicksAdded,
            record.chickDeaths,
            record.chicksSold
        )

        val finalClosingAdults = manualClosingAdults ?: calcClosingAdults
        val finalClosingChicks = manualClosingChicks ?: calcClosingChicks

        if (finalClosingAdults < 0) {
            return Result.failure(IllegalArgumentException("Calculated closing adult count cannot be negative: $finalClosingAdults"))
        }
        if (finalClosingChicks < 0) {
            return Result.failure(IllegalArgumentException("Calculated closing chick count cannot be negative: $finalClosingChicks"))
        }

        return try {
            val recordId = database.withTransaction {
                // Check if a record already exists on this date
                val existing = recordDao.getRecordByDate(record.recordDateString)
                val targetId = if (existing != null) existing.id else record.id

                val recordToSave = record.copy(
                    id = targetId,
                    closingAdults = finalClosingAdults,
                    closingChicks = finalClosingChicks,
                    updatedAt = System.currentTimeMillis()
                )

                val savedId = recordDao.insertRecord(recordToSave)

                // Log adjustment if manual closing differs from formula
                if (manualClosingAdults != null && manualClosingAdults != calcClosingAdults) {
                    adjustmentDao.insertAdjustment(
                        InventoryAdjustment(
                            recordDateString = record.recordDateString,
                            birdType = "ADULT",
                            calculatedCount = calcClosingAdults,
                            actualCount = manualClosingAdults,
                            adjustmentDifference = manualClosingAdults - calcClosingAdults,
                            reason = adjustmentReason
                        )
                    )
                }
                if (manualClosingChicks != null && manualClosingChicks != calcClosingChicks) {
                    adjustmentDao.insertAdjustment(
                        InventoryAdjustment(
                            recordDateString = record.recordDateString,
                            birdType = "CHICK",
                            calculatedCount = calcClosingChicks,
                            actualCount = manualClosingChicks,
                            adjustmentDifference = manualClosingChicks - calcClosingChicks,
                            reason = adjustmentReason
                        )
                    )
                }

                // Recalculate subsequent records to ensure consistent chaining
                recalculateChronologicalInventoryInternal()
                savedId
            }
            Result.success(recordId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a record by ID and recalculates all subsequent inventory in a transaction.
     */
    suspend fun deleteRecord(id: Long): Result<Unit> {
        return try {
            database.withTransaction {
                recordDao.deleteRecordById(id)
                recalculateChronologicalInventoryInternal()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Internal method to sequentially chain opening -> closing for all records
     * in chronological order. Must be invoked inside a transaction.
     */
    private suspend fun recalculateChronologicalInventoryInternal() {
        val farm = farmDao.getFarm() ?: return
        val records = recordDao.getAllRecordsAsc()
        if (records.isEmpty()) return

        var previousClosingAdults = farm.initialAdultChickens
        var previousClosingChicks = farm.initialChicks

        val updatedList = mutableListOf<DailyRecord>()

        for (record in records) {
            val openingAdults = previousClosingAdults
            val openingChicks = previousClosingChicks

            val newClosingAdults = max(
                0,
                openingAdults + record.adultsAdded - record.adultDeaths - record.adultsSold
            )
            val newClosingChicks = max(
                0,
                openingChicks + record.chicksAdded - record.chickDeaths - record.chicksSold
            )

            val updated = record.copy(
                openingAdults = openingAdults,
                closingAdults = newClosingAdults,
                openingChicks = openingChicks,
                closingChicks = newClosingChicks
            )

            updatedList.add(updated)

            previousClosingAdults = newClosingAdults
            previousClosingChicks = newClosingChicks
        }

        recordDao.updateAll(updatedList)
    }

    /**
     * Public recalculation trigger.
     */
    suspend fun recalculateAllRecords(): Result<Unit> {
        return try {
            database.withTransaction {
                recalculateChronologicalInventoryInternal()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // Backup & Restore
    // -------------------------------------------------------------

    suspend fun restoreDatabase(farm: Farm, records: List<DailyRecord>): Result<Unit> {
        return try {
            database.withTransaction {
                recordDao.deleteAllRecords()
                adjustmentDao.deleteAllAdjustments()
                farmDao.deleteAllFarms()

                farmDao.insertFarm(farm)
                recordDao.insertAll(records)

                // Recalculate to ensure absolute inventory integrity
                recalculateChronologicalInventoryInternal()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetAllData(): Result<Unit> {
        return try {
            database.withTransaction {
                recordDao.deleteAllRecords()
                adjustmentDao.deleteAllAdjustments()
                farmDao.deleteAllFarms()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
