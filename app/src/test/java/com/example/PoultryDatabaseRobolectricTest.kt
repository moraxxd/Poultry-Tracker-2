package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.entity.DailyRecord
import com.example.data.entity.Farm
import com.example.data.repository.FarmRepository
import com.example.util.BackupManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PoultryDatabaseRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: FarmRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FarmRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInitialFarmSetupAndOpeningInventory() = runBlocking {
        val farm = Farm(
            id = 1L,
            name = "Sunrise Broiler Farm",
            initialAdultChickens = 200,
            initialChicks = 100,
            initialDate = "2026-10-01",
            initialDateMillis = 1760000000000L,
            preferredLanguage = "en"
        )
        repository.saveFarm(farm)

        val loadedFarm = repository.getFarm()
        assertNotNull(loadedFarm)
        assertEquals("Sunrise Broiler Farm", loadedFarm?.name)
        assertEquals(200, loadedFarm?.initialAdultChickens)
        assertEquals(100, loadedFarm?.initialChicks)

        // Opening inventory for first date should be from initial farm counts
        val (openingAdults, openingChicks) = repository.getOpeningInventoryForDate("2026-10-01")
        assertEquals(200, openingAdults)
        assertEquals(100, openingChicks)
    }

    @Test
    fun testAddDailyRecordAndChaining() = runBlocking {
        val farm = Farm(
            id = 1L,
            name = "Sunrise Farm",
            initialAdultChickens = 100,
            initialChicks = 50,
            initialDate = "2026-10-01",
            initialDateMillis = 1760000000000L
        )
        repository.saveFarm(farm)

        // Day 1: 100 adults - 2 dead - 8 sold = 90 closing
        val day1 = DailyRecord(
            recordDateString = "2026-10-01",
            dateMillis = 1760000000000L,
            recordTimeMillis = 1760000000000L,
            openingAdults = 100,
            adultsAdded = 0,
            adultDeaths = 2,
            adultsSold = 8,
            closingAdults = 90,
            openingChicks = 50,
            chicksAdded = 10,
            chickDeaths = 1,
            chicksSold = 0,
            closingChicks = 59,
            eggsCollected = 45
        )
        val saveDay1Result = repository.saveDailyRecord(day1)
        assertTrue(saveDay1Result.isSuccess)

        // Check opening inventory for next day (2026-10-02)
        val (nextDayAdults, nextDayChicks) = repository.getOpeningInventoryForDate("2026-10-02")
        assertEquals(90, nextDayAdults)
        assertEquals(59, nextDayChicks)

        // Day 2: 90 adults + 20 added - 1 dead - 0 sold = 109 closing
        val day2 = DailyRecord(
            recordDateString = "2026-10-02",
            dateMillis = 1760086400000L,
            recordTimeMillis = 1760086400000L,
            openingAdults = 90,
            adultsAdded = 20,
            adultDeaths = 1,
            adultsSold = 0,
            closingAdults = 109,
            openingChicks = 59,
            chicksAdded = 0,
            chickDeaths = 0,
            chicksSold = 5,
            closingChicks = 54,
            eggsCollected = 55
        )
        val saveDay2Result = repository.saveDailyRecord(day2)
        assertTrue(saveDay2Result.isSuccess)

        val latest = repository.getLatestRecord()
        assertNotNull(latest)
        assertEquals("2026-10-02", latest?.recordDateString)
        assertEquals(109, latest?.closingAdults)
        assertEquals(54, latest?.closingChicks)
    }

    @Test
    fun testRejectNegativeClosingInventory() = runBlocking {
        val farm = Farm(
            id = 1L,
            name = "Test Farm",
            initialAdultChickens = 10,
            initialChicks = 10,
            initialDate = "2026-10-01",
            initialDateMillis = 1760000000000L
        )
        repository.saveFarm(farm)

        // Trying to remove 20 birds when only 10 exist
        val invalidRecord = DailyRecord(
            recordDateString = "2026-10-01",
            dateMillis = 1760000000000L,
            recordTimeMillis = 1760000000000L,
            openingAdults = 10,
            adultsAdded = 0,
            adultDeaths = 5,
            adultsSold = 15, // Closing would be 10 - 5 - 15 = -10
            closingAdults = -10,
            openingChicks = 10,
            closingChicks = 10
        )
        val result = repository.saveDailyRecord(invalidRecord)
        assertTrue(result.isFailure)
    }

    @Test
    fun testHistoricalRecordEditingTriggersRecalculation() = runBlocking {
        val farm = Farm(
            id = 1L,
            name = "Test Farm",
            initialAdultChickens = 100,
            initialChicks = 50,
            initialDate = "2026-10-01",
            initialDateMillis = 1760000000000L
        )
        repository.saveFarm(farm)

        // Day 1: 100 opening - 10 sold = 90 closing
        val day1 = DailyRecord(
            recordDateString = "2026-10-01",
            dateMillis = 1760000000000L,
            recordTimeMillis = 1760000000000L,
            openingAdults = 100,
            adultsSold = 10,
            closingAdults = 90,
            openingChicks = 50,
            closingChicks = 50
        )
        repository.saveDailyRecord(day1)

        // Day 2: 90 opening - 5 sold = 85 closing
        val day2 = DailyRecord(
            recordDateString = "2026-10-02",
            dateMillis = 1760086400000L,
            recordTimeMillis = 1760086400000L,
            openingAdults = 90,
            adultsSold = 5,
            closingAdults = 85,
            openingChicks = 50,
            closingChicks = 50
        )
        repository.saveDailyRecord(day2)

        // Now edit Day 1: Instead of 10 sold, suppose only 5 were sold!
        // New Day 1 closing should be 100 - 5 = 95.
        // And Day 2 opening should automatically become 95, so Day 2 closing becomes 95 - 5 = 90!
        val editedDay1 = day1.copy(
            adultsSold = 5,
            closingAdults = 95
        )
        repository.saveDailyRecord(editedDay1)

        val updatedDay2 = repository.getRecordByDate("2026-10-02")
        assertNotNull(updatedDay2)
        assertEquals(95, updatedDay2?.openingAdults)
        assertEquals(90, updatedDay2?.closingAdults)
    }

    @Test
    fun testCsvAndJsonBackupExportAndValidation() = runBlocking {
        val farm = Farm(
            id = 1L,
            name = "Green Acres Poultry",
            initialAdultChickens = 150,
            initialChicks = 75,
            initialDate = "2026-10-01",
            initialDateMillis = 1760000000000L
        )
        val records = listOf(
            DailyRecord(
                id = 1L,
                recordDateString = "2026-10-01",
                dateMillis = 1760000000000L,
                recordTimeMillis = 1760000000000L,
                openingAdults = 150,
                closingAdults = 148,
                adultDeaths = 2,
                openingChicks = 75,
                closingChicks = 75,
                eggsCollected = 80
            )
        )

        val csv = BackupManager.generateRecordsCsv(records)
        assertTrue(csv.contains("Green Acres Poultry") || csv.contains("2026-10-01"))
        assertTrue(csv.contains("150"))
        assertTrue(csv.contains("80"))

        val json = BackupManager.generateBackupJson(farm, records)
        assertTrue(json.contains("Green Acres Poultry"))

        val parsed = BackupManager.parseAndValidateBackup(json)
        assertTrue(parsed.isSuccess)
        val (restoredFarm, restoredRecords) = parsed.getOrThrow()
        assertEquals("Green Acres Poultry", restoredFarm.name)
        assertEquals(1, restoredRecords.size)
        assertEquals(80, restoredRecords.first().eggsCollected)
    }
}
