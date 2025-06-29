package com.example.moneytracker.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.*

actual object DateTimeUtil {
    actual fun formatChatTimestamp(timestampString: String): String {
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
        }
        val date = dateFormatter.dateFromString(timestampString)

        return if (date != null) {
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "HH:mm"
                locale = NSLocale.currentLocale()
                timeZone = NSTimeZone.localTimeZone
            }
            outputFormatter.stringFromDate(date)
        } else {
            "agora"
        }
    }

    actual fun formatDateForDashboard(dateString: String): String {
        dateString.toLongOrNull()?.let { millis ->
            val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy HH:mm" // Kept original for broader use if needed
                locale = NSLocale.currentLocale()
                timeZone = NSTimeZone.localTimeZone
            }
            return outputFormatter.stringFromDate(date)
        }

        // Try common ISO formats first
        val isoFormatters = listOf(
            NSDateFormatter().apply { dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0) },
            NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
        )
        for (formatter in isoFormatters) {
            formatter.dateFromString(dateString)?.let { date ->
                val outputFormatter = NSDateFormatter().apply {
                    dateFormat = "dd/MM/yyyy" // Specific for chart needs if parsed as simple date
                    locale = NSLocale.currentLocale()
                    timeZone = NSTimeZone.localTimeZone
                }
                return outputFormatter.stringFromDate(date)
            }
        }


        val inputFormatter = NSDateFormatter().apply {
            locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX")
            dateFormat = "EEE MMM dd HH:mm:ss Z yyyy"
        }
        var date = inputFormatter.dateFromString(dateString)

        if (date == null) {
            inputFormatter.dateFormat = "EEE MMM dd HH:mm:ss ZZZZ yyyy"
            date = inputFormatter.dateFromString(dateString)
        }
        if (date == null) {
            inputFormatter.dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy"
             date = inputFormatter.dateFromString(dateString)
        }

        return if (date != null) {
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy HH:mm" // Kept original
                locale = NSLocale.currentLocale()
                timeZone = NSTimeZone.localTimeZone
            }
            outputFormatter.stringFromDate(date)
        } else {
            dateString
        }
    }

    actual fun formatDateForBudgetViewModel(timestamp: Long): String {
        return try {
            val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
            val formatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy"
                locale = NSLocale.currentLocale()
                timeZone = NSTimeZone.localTimeZone
            }
            formatter.stringFromDate(date)
        } catch (e: Exception) {
            "Data inválida"
        }
    }

    actual fun stringToDate(dateString: String): LocalDate {
        // Attempt to parse "yyyy-MM-dd" or "yyyy-MM-ddTHH:mm:ss.SSSZ" (kotlinx.datetime.LocalDate format)
        try {
            return dateString.substringBefore("T").toLocalDate()
        } catch (e: IllegalArgumentException) {
            // Fallback to NSDateFormatter for other formats
        }

        val formatters = listOf(
            NSDateFormatter().apply { dateFormat = "EEE MMM dd HH:mm:ss Z yyyy"; locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX") },
            NSDateFormatter().apply { dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0) },
            NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
            // Add more formats here if needed
        )

        for (formatter in formatters) {
            formatter.dateFromString(dateString)?.let { nsDate ->
                // Convert NSDate to kotlinx.datetime.LocalDate
                val calendar = NSCalendar.currentCalendar
                val components = calendar.components(NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay, fromDate = nsDate)
                return LocalDate(components.year.toInt(), components.month.toInt(), components.day.toInt())
            }
        }
        // Fallback if no format matches
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
}
