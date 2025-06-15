package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction

abstract class InsightGenerator {
    abstract suspend fun generateFinancialInsights(transactions: List<Transaction>): String
}

internal object GeminiServiceFactory {
    private var instance: InsightGenerator? = null

    fun setInstance(service: InsightGenerator) {
        instance = service
    }

    fun getInstance(): InsightGenerator {
        return instance ?: object : InsightGenerator() {
            override suspend fun generateFinancialInsights(transactions: List<Transaction>): String {
                return "Insights não disponíveis nesta plataforma"
            }
        }.also { instance = it }
    }
} 