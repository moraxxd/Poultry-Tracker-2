package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DailyRecordDao
import com.example.data.dao.FarmDao
import com.example.data.dao.InventoryAdjustmentDao
import com.example.data.entity.DailyRecord
import com.example.data.entity.Farm
import com.example.data.entity.InventoryAdjustment

@Database(
    entities = [Farm::class, DailyRecord::class, InventoryAdjustment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun inventoryAdjustmentDao(): InventoryAdjustmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poultry_farm_tracker.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
