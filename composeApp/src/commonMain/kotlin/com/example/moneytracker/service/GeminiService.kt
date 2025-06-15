package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight

abstract class InsightGenerator {
    abstract suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight>
}

internal object GeminiServiceFactory {
    private var instance: InsightGenerator? = null

    fun setInstance(service: InsightGenerator) {
        instance = service
    }

    fun getInstance(): InsightGenerator {
        return instance ?: object : InsightGenerator() {
            override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
                return listOf(
                    Insight(
                        title = "Serviço Indisponível",
                        description = "Insights não disponíveis nesta plataforma",
                        recommendation = "Tente acessar em um dispositivo Android."
                    )
                )
            }
        }.also { instance = it }
    }
} 