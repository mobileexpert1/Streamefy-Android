package com.streamefy.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

object ExpirationUtil {

    fun getExpirationTimestamp(): String {
        // Create a Calendar instance for the current time
        val calendar = Calendar.getInstance()

        // Add 2 hours to the current time
        calendar.add(Calendar.HOUR_OF_DAY, 2)

        // Define the date format pattern
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        // Set the time zone to UTC (or your preferred time zone)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Format and return the expiration time
        return dateFormat.format(calendar.time)
    }
}
