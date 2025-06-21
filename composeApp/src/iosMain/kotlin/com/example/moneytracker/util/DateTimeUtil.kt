package com.example.moneytracker.util

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDate
import platform.Foundation.NSNumber
import platform.Foundation.numberWithLongLong
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.NSTimeZone
import platform.Foundation.autoupdatingCurrentTimeZone
import platform.Foundation.Locale
import platform.Foundation.currentLocale

actual object DateTimeUtil {
    actual fun formatChatTimestamp(timestampString: String): String {
        // iOS specific date formatting for chat timestamp
        // Assuming timestampString is "yyyy-MM-dd'T'HH:mm:ss.SSSSSS" from Gemini
        // This is a common ISO-like format.
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
            // locale = NSLocale.systemLocale // Use system locale if needed for parsing
            // timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0) // Assume UTC if not specified
        }
        val date = dateFormatter.dateFromString(timestampString)

        return if (date != null) {
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "HH:mm"
                locale = Locale.currentLocale()
                timeZone = NSTimeZone.autoupdatingCurrentTimeZone()
            }
            outputFormatter.stringFromDate(date)
        } else {
            "agora" // Fallback
        }
    }

    actual fun formatDateForDashboard(dateString: String): String {
        // This function is tricky because the input `dateString` format was very specific
        // "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy". NSDateFormatter might struggle with 'GMT'XXX.
        // A more KMM-friendly approach is to work with epoch milliseconds (Long) in common code.

        // First, try if it's an epoch Long string
        dateString.toLongOrNull()?.let { millis ->
            val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy HH:mm"
                locale = Locale.currentLocale()
                timeZone = NSTimeZone.autoupdatingCurrentTimeZone()
            }
            return outputFormatter.stringFromDate(date)
        }

        // Fallback: Attempt to parse the complex string. This might be brittle.
        // "EEE MMM dd HH:mm:ss 'GMT'Z yyyy" is closer to what NSDateFormatter might expect for RFC 822
        // or "EEE MMM dd HH:mm:ss zzz yyyy"
        // The 'XXX' for ISO 8601 zone is often problematic across platforms.
        // Let's try a common format it might handle, but this is a guess.
        val inputFormatter = NSDateFormatter().apply {
            dateFormat = "EEE MMM dd HH:mm:ss ZZZZ yyyy" // Common format, ZZZZ for GMT+/-HHMM
            locale = Locale("en_US_POSIX") // Important for fixed-format strings
        }
        var date = inputFormatter.dateFromString(dateString)

        if (date == null) {
            // Try another common variant if the first fails
            inputFormatter.dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy"
             date = inputFormatter.dateFromString(dateString)
        }

        return if (date != null) {
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy HH:mm"
                locale = Locale.currentLocale()
                timeZone = NSTimeZone.autoupdatingCurrentTimeZone()
            }
            outputFormatter.stringFromDate(date)
        } else {
            dateString // Fallback
        }
    }

    actual fun formatDateForBudgetViewModel(timestamp: Long): String {
        return try {
            val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
            val formatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy"
                locale = Locale.currentLocale()
                timeZone = NSTimeZone.autoupdatingCurrentTimeZone()
            }
            formatter.stringFromDate(date)
        } catch (e: Exception) {
            "Data inválida"
        }
    }
}
