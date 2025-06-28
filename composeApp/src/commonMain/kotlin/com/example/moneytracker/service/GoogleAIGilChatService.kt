package com.example.moneytracker.service

import com.example.moneytracker.config.getGeminiApiKey
import dev.shreyaspatil.generativeai.GenerativeModel
import dev.shreyaspatil.generativeai.type.generationConfig
import dev.shreyaspatil.generativeai.type.safetySetting

internal class GoogleAIGilChatService : GilService() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro", // Or another suitable model for chat
        apiKey = getGeminiApiKey(),
        // Optional: Configure safety settings and generation parameters if needed
        // safetySettings = listOf(safetySetting(...)),
        // generationConfig = generationConfig { temperature = 0.7f }
    )

    override suspend fun chat(message: String, context: UserFinancialContext?): String {
        val fullPrompt = buildPrompt(message, context) // Uses the helper from the base class

        return try {
            val response = generativeModel.generateContent(fullPrompt)
            response.text ?: "Desculpe, não consegui processar sua pergunta no momento."
        } catch (e: Exception) {
            println("Error in GilService chat: ${e.message}")
            "Desculpe, ocorreu um erro ao tentar falar com o Gil. Por favor, tente novamente mais tarde. (Detalhe: ${e.message})"
        }
    }
}
