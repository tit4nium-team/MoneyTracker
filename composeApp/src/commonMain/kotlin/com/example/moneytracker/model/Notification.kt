package com.example.moneytracker.model

import kotlinx.datetime.Clock

data class Notification(
    val id: String = "",
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val isRead: Boolean = false,
    val actionRoute: String? = null
)

enum class NotificationType {
    BUDGET_ALERT,
    SPENDING_INSIGHT,
    SAVING_GOAL,
    BILL_REMINDER,
    ACHIEVEMENT,
    TIP
}