package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val PATTERN_DATE = "yyyy-MM-dd"
    private const val PATTERN_DISPLAY = "dd MMM yyyy"
    private const val PATTERN_TIME = "hh:mm a"
    private const val PATTERN_DATE_TIME = "dd MMM yyyy, hh:mm a"

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat(PATTERN_DATE, Locale.US)
        return sdf.format(Date())
    }

    fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat(PATTERN_TIME, Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat(PATTERN_DATE, Locale.US)
            val date = sdf.parse(dateString) ?: return dateString
            val out = SimpleDateFormat(PATTERN_DISPLAY, Locale.getDefault())
            out.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDateTimeForDisplay(millis: Long): String {
        return try {
            val out = SimpleDateFormat(PATTERN_DATE_TIME, Locale.getDefault())
            out.format(Date(millis))
        } catch (e: Exception) {
            millis.toString()
        }
    }

    fun formatTimeForDisplay(millis: Long): String {
        return try {
            val out = SimpleDateFormat(PATTERN_TIME, Locale.getDefault())
            out.format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }

    fun parseDateStringToMillis(dateString: String): Long {
        return try {
            val sdf = SimpleDateFormat(PATTERN_DATE, Locale.US)
            sdf.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun formatMillisToDateString(millis: Long): String {
        val sdf = SimpleDateFormat(PATTERN_DATE, Locale.US)
        return sdf.format(Date(millis))
    }

    fun getDaysAgoDateString(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val sdf = SimpleDateFormat(PATTERN_DATE, Locale.US)
        return sdf.format(cal.time)
    }

    fun getYesterdayDateString(): String {
        return getDaysAgoDateString(1)
    }
}
