package com.example.moneytracker.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual object DateTimeUtil {
    actual fun formatChatTimestamp(timestampString: String): String {
        return try {
            // Assumes timestampString is "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
            // This might need adjustment if the Gemini service timestamp format changes
            val inputPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
            val parser = SimpleDateFormat(inputPattern, Locale.getDefault())
            // Potentially, the service returns UTC, adjust if needed
            // parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(timestampString)

            val outputFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            // outputFormatter.timeZone = TimeZone.getDefault() // Format to local time
            outputFormatter.format(date ?: Date())
        } catch (e: Exception) {
            "agora" // Fallback
        }
    }

    actual fun formatDateForDashboard(dateString: String): String {
         return try {
            // Original format: "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy" (e.g., "Wed Jun 11 20:30:02 GMT-03:00 2025")
            // This was specific and might be error-prone if dateStr format from DB/ViewModel changes.
            // A more robust solution would be to pass Long timestamp from common logic.
            // For now, attempting to keep similar logic but this is a common source of KMM date issues.

            // Try parsing as epoch milliseconds first (if it's already a Long string)
            dateString.toLongOrNull()?.let {
                val date = Date(it)
                val outputFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                return outputFormatter.format(date)
            }

            // Fallback to trying the complex "EEE MMM dd..." pattern if it's not a simple Long
            // This part is highly dependent on the exact string format and Locale.
            // Consider standardizing on ISO 8601 or epoch millis in common code.
            val inputPattern = "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy"
            val inputFormatter = SimpleDateFormat(inputPattern, Locale.US) // Using US for month/day names
            val date = inputFormatter.parse(dateString)

            val outputFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            outputFormatter.format(date ?: Date())
        } catch (e: Exception) {
            dateString // Fallback to original string if parsing fails
        }
    }

    actual fun formatDateForBudgetViewModel(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "Data inválida"
        }
    }
}
