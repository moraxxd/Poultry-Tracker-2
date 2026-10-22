package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.entity.DailyRecord
import com.example.data.entity.Farm
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupManager {

    /**
     * Generates RFC-compliant CSV string for Daily Records
     */
    fun generateRecordsCsv(records: List<DailyRecord>): String {
        val sb = StringBuilder()
        sb.append("Date,Opening Adults,Adults Added,Adult Deaths,Adults Sold,Closing Adults,Opening Chicks,Chicks Added,Chick Deaths,Chicks Sold,Closing Chicks,Total Live Birds,Eggs Collected,Total Deaths,Notes,Observations,Treatments\n")

        for (r in records) {
            sb.append(escapeCsv(r.recordDateString)).append(",")
            sb.append(r.openingAdults).append(",")
            sb.append(r.adultsAdded).append(",")
            sb.append(r.adultDeaths).append(",")
            sb.append(r.adultsSold).append(",")
            sb.append(r.closingAdults).append(",")
            sb.append(r.openingChicks).append(",")
            sb.append(r.chicksAdded).append(",")
            sb.append(r.chickDeaths).append(",")
            sb.append(r.chicksSold).append(",")
            sb.append(r.closingChicks).append(",")
            sb.append(r.totalLiveBirds).append(",")
            sb.append(r.eggsCollected).append(",")
            sb.append(r.totalDeaths).append(",")
            sb.append(escapeCsv(r.notes)).append(",")
            sb.append(escapeCsv(r.healthObservations)).append(",")
            sb.append(escapeCsv(r.treatments)).append("\n")
        }
        return sb.toString()
    }

    /**
     * Generates RFC-compliant CSV string for Farm Reports
     */
    fun generateReportsCsv(farm: Farm?, records: List<DailyRecord>): String {
        val sb = StringBuilder()
        sb.append("Farm Report Summary\n")
        sb.append("Farm Name,").append(escapeCsv(farm?.name ?: "Poultry Farm")).append("\n")
        sb.append("Export Date,").append(DateUtils.getCurrentDateString()).append("\n")
        sb.append("Total Daily Records,").append(records.size).append("\n\n")

        sb.append("Date,Live Adults,Live Chicks,Total Live Birds,Eggs Collected,Adult Deaths,Chick Deaths,Total Deaths,Mortality Rate %\n")
        for (r in records) {
            val mortalityStr = r.dailyMortalityRatePercent?.let { String.format(java.util.Locale.US, "%.2f%%", it) } ?: "N/A"
            sb.append(escapeCsv(r.recordDateString)).append(",")
            sb.append(r.closingAdults).append(",")
            sb.append(r.closingChicks).append(",")
            sb.append(r.totalLiveBirds).append(",")
            sb.append(r.eggsCollected).append(",")
            sb.append(r.adultDeaths).append(",")
            sb.append(r.chickDeaths).append(",")
            sb.append(r.totalDeaths).append(",")
            sb.append(mortalityStr).append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Generates a full JSON string preserving farm profile and all daily records
     */
    fun generateBackupJson(farm: Farm, records: List<DailyRecord>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "Poultry Farm Tracker")
        root.put("exportedAt", System.currentTimeMillis())

        val farmObj = JSONObject()
        farmObj.put("id", farm.id)
        farmObj.put("name", farm.name)
        farmObj.put("initialAdultChickens", farm.initialAdultChickens)
        farmObj.put("initialChicks", farm.initialChicks)
        farmObj.put("initialDate", farm.initialDate)
        farmObj.put("initialDateMillis", farm.initialDateMillis)
        farmObj.put("preferredLanguage", farm.preferredLanguage)
        farmObj.put("createdAt", farm.createdAt)
        farmObj.put("updatedAt", farm.updatedAt)
        root.put("farm", farmObj)

        val recordsArray = JSONArray()
        for (r in records) {
            val rObj = JSONObject()
            rObj.put("id", r.id)
            rObj.put("farmId", r.farmId)
            rObj.put("recordDateString", r.recordDateString)
            rObj.put("dateMillis", r.dateMillis)
            rObj.put("recordTimeMillis", r.recordTimeMillis)
            rObj.put("openingAdults", r.openingAdults)
            rObj.put("adultsAdded", r.adultsAdded)
            rObj.put("adultDeaths", r.adultDeaths)
            rObj.put("adultsSold", r.adultsSold)
            rObj.put("closingAdults", r.closingAdults)
            rObj.put("openingChicks", r.openingChicks)
            rObj.put("chicksAdded", r.chicksAdded)
            rObj.put("chickDeaths", r.chickDeaths)
            rObj.put("chicksSold", r.chicksSold)
            rObj.put("closingChicks", r.closingChicks)
            rObj.put("eggsCollected", r.eggsCollected)
            rObj.put("notes", r.notes)
            rObj.put("healthObservations", r.healthObservations)
            rObj.put("treatments", r.treatments)
            rObj.put("createdAt", r.createdAt)
            rObj.put("updatedAt", r.updatedAt)
            recordsArray.put(rObj)
        }
        root.put("records", recordsArray)

        return root.toString(2)
    }

    /**
     * Parses and validates a JSON backup before inserting into DB
     */
    fun parseAndValidateBackup(jsonString: String): Result<Pair<Farm, List<DailyRecord>>> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("farm") || !root.has("records")) {
                return Result.failure(IllegalArgumentException("Missing required farm or records structure."))
            }

            val farmObj = root.getJSONObject("farm")
            val farm = Farm(
                id = farmObj.optLong("id", 1L),
                name = farmObj.optString("name", "Poultry Farm"),
                initialAdultChickens = farmObj.optInt("initialAdultChickens", 0).coerceAtLeast(0),
                initialChicks = farmObj.optInt("initialChicks", 0).coerceAtLeast(0),
                initialDate = farmObj.optString("initialDate", DateUtils.getCurrentDateString()),
                initialDateMillis = farmObj.optLong("initialDateMillis", System.currentTimeMillis()),
                preferredLanguage = farmObj.optString("preferredLanguage", "en"),
                createdAt = farmObj.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = farmObj.optLong("updatedAt", System.currentTimeMillis())
            )

            val recordsArray = root.getJSONArray("records")
            val recordsList = mutableListOf<DailyRecord>()
            for (i in 0 until recordsArray.length()) {
                val rObj = recordsArray.getJSONObject(i)
                val dateStr = rObj.getString("recordDateString")
                if (dateStr.isBlank()) {
                    return Result.failure(IllegalArgumentException("Record at index $i is missing record date."))
                }

                val record = DailyRecord(
                    id = rObj.optLong("id", 0L),
                    farmId = farm.id,
                    recordDateString = dateStr,
                    dateMillis = rObj.optLong("dateMillis", DateUtils.parseDateStringToMillis(dateStr)),
                    recordTimeMillis = rObj.optLong("recordTimeMillis", System.currentTimeMillis()),
                    openingAdults = rObj.optInt("openingAdults", 0).coerceAtLeast(0),
                    adultsAdded = rObj.optInt("adultsAdded", 0).coerceAtLeast(0),
                    adultDeaths = rObj.optInt("adultDeaths", 0).coerceAtLeast(0),
                    adultsSold = rObj.optInt("adultsSold", 0).coerceAtLeast(0),
                    closingAdults = rObj.optInt("closingAdults", 0).coerceAtLeast(0),
                    openingChicks = rObj.optInt("openingChicks", 0).coerceAtLeast(0),
                    chicksAdded = rObj.optInt("chicksAdded", 0).coerceAtLeast(0),
                    chickDeaths = rObj.optInt("chickDeaths", 0).coerceAtLeast(0),
                    chicksSold = rObj.optInt("chicksSold", 0).coerceAtLeast(0),
                    closingChicks = rObj.optInt("closingChicks", 0).coerceAtLeast(0),
                    eggsCollected = rObj.optInt("eggsCollected", 0).coerceAtLeast(0),
                    notes = rObj.optString("notes", ""),
                    healthObservations = rObj.optString("healthObservations", ""),
                    treatments = rObj.optString("treatments", ""),
                    createdAt = rObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = rObj.optLong("updatedAt", System.currentTimeMillis())
                )
                recordsList.add(record)
            }

            Result.success(Pair(farm, recordsList))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Writes content to a user-selected SAF Uri
     */
    fun writeToUri(context: Context, uri: Uri, content: String): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reads text content from a user-selected SAF Uri
     */
    fun readFromUri(context: Context, uri: Uri): Result<String> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: ""
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Prepares a file in cache and launches Android Share Sheet
     */
    fun shareContent(context: Context, fileName: String, mimeType: String, content: String) {
        try {
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Poultry Farm File"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
