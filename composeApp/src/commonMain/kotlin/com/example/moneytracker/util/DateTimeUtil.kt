package com.example.moneytracker.util

expect object DateTimeUtil {
    fun formatChatTimestamp(timestampString: String): String
    fun formatDateForDashboard(dateString: String): String
    fun formatDateForBudgetViewModel(timestamp: Long): String
}
