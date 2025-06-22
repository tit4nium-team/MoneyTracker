package com.example.moneytracker.model

import kotlinx.serialization.Serializable

@Serializable
data class Insight(
    val title: String,
    val description: String,
    val recommendation: String? = null, // O JSON da IA sempre terá, mas é bom manter opcional no modelo
    val type: InsightType = InsightType.GENERAL // Não vem do JSON da IA, usará o default
)

@Serializable
enum class InsightType {
    SPENDING_PATTERN,
    SAVING_OPPORTUNITY,
    BUDGET_ALERT,
    INCOME_TREND,
    GENERAL
}