package com.example.moneytracker

import com.example.moneytracker.service.GeminiServiceFactory
import com.example.moneytracker.service.GilServiceFactory
import com.example.moneytracker.service.GoogleAIGeminiInsightGenerator
import com.example.moneytracker.service.GoogleAIGilChatService

object AIServiceInitializer {
    fun initialize() {
        // Initialize and set the concrete InsightGenerator implementation
        val insightGenerator = GoogleAIGeminiInsightGenerator()
        GeminiServiceFactory.setInstance(insightGenerator)

        // Initialize and set the concrete GilService implementation
        val gilService = GoogleAIGilChatService()
        GilServiceFactory.setInstance(gilService)
    }
}
