package com.belive.dating.extensions

import android.os.Build
import android.os.CountDownTimer
import android.text.format.DateUtils
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

fun formatTimeForBoost(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

fun isValidDate(dateStr: String): Boolean {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.isLenient = false // Strict parsing

    return try {
        val date = sdf.parse(dateStr)
        date != null
    } catch (e: Exception) {
        catchLog(gsonString(e))
        false
    }
}

fun calculateTimeDifference(dateString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val givenDate = sdf.parse(dateString) ?: return ""

        val currentDate = Date() // Get the current date and time

        // Calculate the time difference in milliseconds
        val timeDifference = currentDate.time - givenDate.time

        // Convert the time difference into various units
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
        val days = TimeUnit.MILLISECONDS.toDays(timeDifference)

        // Return the formatted time difference
        when {
            seconds < 60 -> "$seconds s"
            minutes < 60 -> "$minutes m"
            hours < 24 -> "$hours h"
            days == 1L -> "Yesterday"
            days > 1L -> "$days d"
            else -> "Just Now"
        }
    } catch (e: Exception) {
        ""
    }
}

fun calculateAge(year: Int, month: Int, day: Int): Int {
    val today = Calendar.getInstance()
    val birthDate = Calendar.getInstance()
    birthDate.set(year, month - 1, day) // Months are 0-indexed in Calendar

    var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)

    // Adjust age if the current month and day are before the birth month and day
    if (today[Calendar.MONTH] < birthDate[Calendar.MONTH] ||
        (today[Calendar.MONTH] == birthDate[Calendar.MONTH] &&
                today[Calendar.DAY_OF_MONTH] < birthDate[Calendar.DAY_OF_MONTH])
    ) {
        age--
    }

    return age
}

fun convertLongToStringDate(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}

fun convertMinutesToHoursMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        "$hours:$minutes"
    } else {
        "$minutes"
    }
}

fun formatDateForChatGroupTitle(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return if (DateUtils.isToday(timestamp)) {
        "Today"
    } else if (DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
        "Yesterday"
    } else {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(calendar.time)
    }
}

fun isDateInPast(dateString: String): Boolean {
    return try {
        // Define the date format matching the input string
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        // Set the time zone to UTC
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        // Parse the input date string to a Date object
        val parsedDate = sdf.parse(dateString)
        // Get the current time
        val currentDate = Date()
        // Compare the parsed date with the current time
        parsedDate?.before(currentDate) == true
    } catch (e: Exception) {
        e.printStackTrace()
        false // Return false if parsing fails
    }
}

fun convertDateString(inputDate: String): String {
    logger("--catch--", "inputDate: $inputDate")
    try {
        // Define input and output date formats
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormatter.timeZone = TimeZone.getTimeZone("UTC") // Ensure UTC parsing

        val outputFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        // Parse input date
        val date = inputFormatter.parse(inputDate)

        // Format and return
        return date?.let { outputFormatter.format(it) } ?: "Invalid date"
    } catch (e: Exception) {
        e.printStackTrace()
        return "Invalid date"
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

fun iso8601ToMillis(iso8601String: String): Long {
    return try {
        // Define input ISO 8601 format
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure parsing as UTC

        // Parse the ISO 8601 date string into a Date object
        val date: Date = inputFormat.parse(iso8601String)!!

        // Convert Date to milliseconds since epoch
        date.time
    } catch (e: Exception) {
        e.printStackTrace()
        -1L // Return -1 in case of an error
    }
}

fun convertLongToDate(millis: Long, dateFormat: String): String? {
    try {
        val format = SimpleDateFormat(dateFormat, Locale.getDefault())
        val date = Date(millis)
        return format.format(date)
    } catch (e: Exception) {
        catchLog("convertLongToDate: ${e.printStackTrace()}")
        return null
    }
}

fun startCountdownToEndOfDay(
    startMillis: Long,
    onTick: (remainingMillis: Long) -> Unit,
    onFinish: () -> Unit,
): CountDownTimer {
    val endMillis = getEndOfDayInMillis(startMillis)
    val duration = endMillis - startMillis

    return object : CountDownTimer(duration, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            onTick(millisUntilFinished)
        }

        override fun onFinish() {
            onFinish()
        }
    }.also {
        it.start()
    }
}

fun getEndOfDayInMillis(timestampMillis: Long): Long {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val zoneId = ZoneId.systemDefault()
        val instant = Instant.ofEpochMilli(timestampMillis)
        val date = instant.atZone(zoneId).toLocalDate()
        val endOfDay = date.atTime(23, 59, 59, 999_000_000) // 999 milliseconds
        return endOfDay.atZone(zoneId).toInstant().toEpochMilli()
    } else {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestampMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
}

fun convertDateToTime(dateString: String): String {
    // Define input date format
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure parsing as UTC

    // Define output format
    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault() // Convert to device local time

    // Force AM/PM to uppercase
    val dateFormatSymbols = DateFormatSymbols(Locale.getDefault())
    dateFormatSymbols.amPmStrings = arrayOf("AM", "PM") // Override to uppercase
    outputFormat.dateFormatSymbols = dateFormatSymbols

    // Parse the input date and format to output
    val date: Date = inputFormat.parse(dateString)!!
    return outputFormat.format(date)
}