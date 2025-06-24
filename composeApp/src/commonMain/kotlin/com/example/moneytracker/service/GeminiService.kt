package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight

// Define InsightGenerator as an expect class
expect class InsightGenerator() {
    suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight>
}

// Factory to get platform-specific instance
internal expect object GeminiServiceFactory {
    fun getInstance(): InsightGenerator
}

// Expect function to initialize the platform-specific service
expect fun initializeGeminiService()