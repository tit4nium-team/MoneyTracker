package com.example.moneytracker.model

data class Insight(
    val title: String,
    val description: String,
    val recommendation: String? = null,
    val type: InsightType = InsightType.GENERAL
)

enum class InsightType {
    SPENDING_PATTERN,
    SAVING_OPPORTUNITY,
    BUDGET_ALERT,
    INCOME_TREND,
    GENERAL
} 