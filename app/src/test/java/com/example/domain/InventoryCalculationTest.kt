package com.example.domain

import com.example.domain.model.InventoryCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryCalculationTest {

    @Test
    fun testClosingAdultChickensFormula() {
        val opening = 100
        val added = 20
        val deaths = 5
        val sold = 10
        // 100 + 20 - 5 - 10 = 105
        val closing = InventoryCalculator.calculateClosingAdults(opening, added, deaths, sold)
        assertEquals(105, closing)
    }

    @Test
    fun testClosingChicksFormula() {
        val opening = 50
        val added = 30
        val deaths = 2
        val sold = 8
        // 50 + 30 - 2 - 8 = 70
        val closing = InventoryCalculator.calculateClosingChicks(opening, added, deaths, sold)
        assertEquals(70, closing)
    }

    @Test
    fun testTotalLiveBirdsNeverIncludesEggs() {
        val closingAdults = 105
        val closingChicks = 70
        // Eggs should never be added to live birds
        val totalLiveBirds = InventoryCalculator.calculateTotalLiveBirds(closingAdults, closingChicks)
        assertEquals(175, totalLiveBirds)
    }

    @Test
    fun testTotalDeathsFormula() {
        val adultDeaths = 4
        val chickDeaths = 6
        val totalDeaths = InventoryCalculator.calculateTotalDeaths(adultDeaths, chickDeaths)
        assertEquals(10, totalDeaths)
    }

    @Test
    fun testMortalityRateWhenOpeningIsPositive() {
        val totalDeaths = 5
        val openingLiveBirds = 100
        val rate = InventoryCalculator.calculateMortalityRate(totalDeaths, openingLiveBirds)
        // (5 / 100) * 100 = 5.0%
        assertEquals(5.0, rate!!, 0.001)
    }

    @Test
    fun testMortalityRateWhenOpeningIsZero() {
        val totalDeaths = 0
        val openingLiveBirds = 0
        val rate = InventoryCalculator.calculateMortalityRate(totalDeaths, openingLiveBirds)
        // Must return null (N/A) without division by zero crash
        assertNull(rate)
    }

    @Test
    fun testDetectNegativeClosingStock() {
        val opening = 10
        val added = 0
        val deaths = 5
        val sold = 10
        // 10 + 0 - 5 - 10 = -5
        val closing = InventoryCalculator.calculateClosingAdults(opening, added, deaths, sold)
        assertTrue(closing < 0)
    }
}
