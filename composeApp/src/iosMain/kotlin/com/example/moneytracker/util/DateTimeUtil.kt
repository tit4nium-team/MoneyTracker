package com.example.moneytracker.util

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.NSTimeZone
import platform.Foundation.NSLocale
// import platform.Foundation.currentLocale // Comentado/Removido
// import platform.Foundation.localTimeZone // Comentado/Removido
// import platform.Foundation.localeWithLocaleIdentifier // Comentado/Removido

actual object DateTimeUtil {
    actual fun formatChatTimestamp(timestampString: String): String {
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
        }
        val date = dateFormatter.dateFromString(timestampString)

        return if (date != null) {
            val outputFormatter = NSDateFormatter().apply {
                dateFormat = "HH:mm"
                locale = NSLocale.currentLocale // Alterado para propriedade
                timeZone = NSTimeZone.localTimeZone // Mantido como propriedade (já estava assim implicitamente)
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
                dateFormat = "dd/MM/yyyy HH:mm"
                locale = NSLocale.currentLocale // Alterado para propriedade
                timeZone = NSTimeZone.localTimeZone // Mantido como propriedade
            }
            return outputFormatter.stringFromDate(date)
        }

        val inputFormatter = NSDateFormatter().apply {
            locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX") // Mantido como método
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
                dateFormat = "dd/MM/yyyy HH:mm"
                locale = NSLocale.currentLocale // Alterado para propriedade
                timeZone = NSTimeZone.localTimeZone // Mantido como propriedade
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
                locale = NSLocale.currentLocale // Alterado para propriedade
                timeZone = NSTimeZone.localTimeZone // Mantido como propriedade
            }
            formatter.stringFromDate(date)
        } catch (e: Exception) {
            "Data inválida"
        }
    }
}
