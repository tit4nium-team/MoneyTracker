package com.example.moneytracker.util

import kotlinx.datetime.LocalDate

expect object DateTimeUtil {
    fun formatChatTimestamp(timestampString: String): String
    fun formatDateForDashboard(dateString: String): String
    fun formatDateForBudgetViewModel(timestamp: Long): String
    fun stringToDate(dateString: String): LocalDate
}
