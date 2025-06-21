package com.example.moneytracker.util

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDate
// NSNumber and numberWithLongLong are not directly used for date formatting with NSDateFormatter
// import platform.Foundation.NSNumber
// import platform.Foundation.numberWithLongLong
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.NSTimeZone
// import platform.Foundation.autoupdatingCurrentTimeZone // Replaced
import platform.Foundation.NSLocale // Correct import for NSLocale
// import platform.Foundation.currentLocale // Replaced by NSLocale.currentLocale (property access)

actual object DateTimeUtil {
    actual fun formatChatTimestamp(timestampString: String): String {
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
            // locale = NSLocale.systemLocale // Use system locale if needed for parsing
            // timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0) // Assume UTC if not specified
        }
        val date = dateFormatter.dateFromString(timestampString)

        return if (date != null) {
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "HH:mm"
                locale = NSLocale.currentLocale() // Corrigido
                timeZone = NSTimeZone.localTimeZone // Corrigido
            }
            outputFormatter.stringFromDate(date)
        } else {
            "agora" // Fallback
        }
    }

    actual fun formatDateForDashboard(dateString: String): String {
        dateString.toLongOrNull()?.let { millis ->
            val date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy HH:mm"
                locale = NSLocale.currentLocale() // Corrigido
                timeZone = NSTimeZone.localTimeZone // Corrigido
            }
            return outputFormatter.stringFromDate(date)
        }

        val inputFormatter = NSDateFormatter().apply {
            // dateFormat = "EEE MMM dd HH:mm:ss ZZZZ yyyy"
            // Using "en_US_POSIX" is crucial for fixed format date strings like this
            // to prevent issues with user's locale/calendar settings.
            locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX") // Corrigido
            // Attempting to parse a complex format like "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy"
            // can be very tricky with NSDateFormatter. 'XXX' is particularly problematic.
            // A common pattern that sometimes works for GMT offsets:
            dateFormat = "EEE MMM dd HH:mm:ss Z yyyy" // Z, ZZ, ZZZ for RFC822 style timezone, ZZZZ for GMT, ZZZZZ for ISO8601
                                                      // Let's try with Z, then ZZZZ if it fails.
        }
        var date = inputFormatter.dateFromString(dateString)

        if (date == null) {
            inputFormatter.dateFormat = "EEE MMM dd HH:mm:ss ZZZZ yyyy"
            date = inputFormatter.dateFromString(dateString)
        }
        if (date == null) {
            // Try another common variant if the first fails
            inputFormatter.dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy" // zzz for general timezone like PST
             date = inputFormatter.dateFromString(dateString)
        }


        return if (date != null) {
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "dd/MM/yyyy HH:mm"
                locale = NSLocale.currentLocale() // Corrigido
                timeZone = NSTimeZone.localTimeZone // Corrigido
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
                locale = NSLocale.currentLocale() // Corrigido
                timeZone = NSTimeZone.localTimeZone // Corrigido
            }
            formatter.stringFromDate(date)
        } catch (e: Exception) {
            "Data inválida"
        }
    }
}
