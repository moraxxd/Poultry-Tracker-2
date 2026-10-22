package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.DailyRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records ORDER BY recordDateString DESC, id DESC")
    fun getAllRecordsFlow(): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records ORDER BY recordDateString ASC, id ASC")
    suspend fun getAllRecordsAsc(): List<DailyRecord>

    @Query("SELECT * FROM daily_records ORDER BY recordDateString DESC, id DESC")
    suspend fun getAllRecordsDesc(): List<DailyRecord>

    @Query("SELECT * FROM daily_records WHERE id = :id")
    fun getRecordByIdFlow(id: Long): Flow<DailyRecord?>

    @Query("SELECT * FROM daily_records WHERE id = :id")
    suspend fun getRecordById(id: Long): DailyRecord?

    @Query("SELECT * FROM daily_records WHERE recordDateString = :dateString LIMIT 1")
    suspend fun getRecordByDate(dateString: String): DailyRecord?

    @Query("SELECT * FROM daily_records ORDER BY recordDateString DESC LIMIT 1")
    fun getLatestRecordFlow(): Flow<DailyRecord?>

    @Query("SELECT * FROM daily_records ORDER BY recordDateString DESC LIMIT 1")
    suspend fun getLatestRecord(): DailyRecord?

    @Query("SELECT * FROM daily_records WHERE recordDateString < :dateString ORDER BY recordDateString DESC LIMIT 1")
    suspend fun getPrecedingRecord(dateString: String): DailyRecord?

    @Query("SELECT * FROM daily_records WHERE recordDateString > :dateString ORDER BY recordDateString ASC")
    suspend fun getSubsequentRecords(dateString: String): List<DailyRecord>

    @Query("SELECT * FROM daily_records WHERE recordDateString BETWEEN :startDate AND :endDate ORDER BY recordDateString DESC")
    fun getRecordsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE recordDateString BETWEEN :startDate AND :endDate ORDER BY recordDateString ASC")
    suspend fun getRecordsBetweenDatesAsc(startDate: String, endDate: String): List<DailyRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DailyRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<DailyRecord>)

    @Update
    suspend fun updateRecord(record: DailyRecord)

    @Update
    suspend fun updateAll(records: List<DailyRecord>)

    @Delete
    suspend fun deleteRecord(record: DailyRecord)

    @Query("DELETE FROM daily_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM daily_records")
    suspend fun deleteAllRecords()
}
