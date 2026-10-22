package com.example.domain.model

object InventoryCalculator {

    /**
     * Closing adult chickens = Opening adult chickens + Adult chickens added - Adult chicken deaths - Adult chickens sold or removed.
     */
    fun calculateClosingAdults(
        openingAdults: Int,
        adultsAdded: Int,
        adultDeaths: Int,
        adultsSold: Int
    ): Int {
        return openingAdults + adultsAdded - adultDeaths - adultsSold
    }

    /**
     * Closing chicks = Opening chicks + New chicks added - Chick deaths - Chicks sold or removed.
     */
    fun calculateClosingChicks(
        openingChicks: Int,
        chicksAdded: Int,
        chickDeaths: Int,
        chicksSold: Int
    ): Int {
        return openingChicks + chicksAdded - chickDeaths - chicksSold
    }

    /**
     * Total live birds = Closing adult chickens + Closing chicks.
     * Eggs are NEVER included in live birds.
     */
    fun calculateTotalLiveBirds(closingAdults: Int, closingChicks: Int): Int {
        return closingAdults + closingChicks
    }

    /**
     * Total deaths today = Adult chicken deaths + Chick deaths.
     */
    fun calculateTotalDeaths(adultDeaths: Int, chickDeaths: Int): Int {
        return adultDeaths + chickDeaths
    }

    /**
     * Daily mortality rate = (Total deaths / Opening total live birds) * 100.
     * Returns null if opening total live birds is 0.
     */
    fun calculateMortalityRate(totalDeaths: Int, openingTotalLiveBirds: Int): Double? {
        if (openingTotalLiveBirds <= 0) return null
        return (totalDeaths.toDouble() / openingTotalLiveBirds.toDouble()) * 100.0
    }
}
