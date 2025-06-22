package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight

abstract class InsightGenerator {
    abstract suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight>
}

// This expect fun will be implemented in platform-specific code (androidMain, iosMain)
internal expect fun initializeGeminiService()

internal object GeminiServiceFactory {
    private var instance: InsightGenerator? = null

    fun initialize() {
        if (instance == null) {
            initializeGeminiService() // Calls the platform-specific initializer
        }
    }

    fun setInstance(service: InsightGenerator) {
        instance = service
    }

    fun getInstance(): InsightGenerator {
        initialize() // Ensure initialized before getting instance
        return instance ?: object : InsightGenerator() { // Fallback if not initialized
            override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
                return listOf(
                    Insight(
                        title = "Serviço Indisponível",
                        description = "Configuração da plataforma para GeminiService não encontrada.",
                        recommendation = "Verifique a inicialização específica da plataforma."
                    )
                )
            }
        }
    }
}