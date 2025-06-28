package com.example.moneytracker.service

import com.example.moneytracker.config.getGeminiApiKey
import com.example.moneytracker.model.Insight
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionType
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

// Data class for parsing structured JSON response from AI
@Serializable
private data class AIInsight(
    val title: String,
    val description: String,
    val recommendation: String? = null
)

internal class GoogleAIGeminiInsightGenerator : InsightGenerator() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro", // Or another suitable model
        apiKey = getGeminiApiKey()
    )

    override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
        if (transactions.isEmpty()) {
            return emptyList()
        }

        val prompt = buildPromptForInsights(transactions)

        try {
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text
            return parseResponseToInsights(responseText)
        } catch (e: Exception) {
            // Log error or handle it appropriately
            println("Error generating insights: ${e.message}")
            return listOf(
                Insight(
                    title = "Erro ao Gerar Análise",
                    description = "Não foi possível conectar ao serviço de IA para gerar análises financeiras.",
                    recommendation = "Verifique sua conexão com a internet ou tente novamente mais tarde. Detalhe: ${e.message}"
                )
            )
        }
    }

    private fun buildPromptForInsights(transactions: List<Transaction>): String {
        val transactionSummary = transactions.joinToString("\n") {
            val type = if (it.type == TransactionType.INCOME) "Receita" else "Despesa"
            "- ${it.description}: R$${it.amount} (${type}) em ${it.date} (Categoria: ${it.category.name})"
        }

        // Instruct the AI to return a JSON array of objects
        return """
            Analise as seguintes transações financeiras e forneça insights.
            Seja conciso e prático. Para cada insight, forneça um título, uma descrição e uma recomendação opcional.
            Retorne os resultados como um array JSON de objetos, onde cada objeto tem os campos "title", "description" e "recommendation".
            Exemplo de formato de um objeto no array:
            {
              "title": "Título do Insight",
              "description": "Descrição detalhada do insight.",
              "recommendation": "Recomendação para o usuário."
            }
            Se não houver recomendação, omita o campo "recommendation" ou deixe-o como null.

            Transações:
            $transactionSummary

            Por favor, forneça de 3 a 5 insights principais baseados nestas transações.
            Array JSON de Insights:
        """.trimIndent()
    }

    private fun parseResponseToInsights(responseText: String?): List<Insight> {
        if (responseText.isNullOrBlank()) {
            return listOf(Insight("Nenhuma Análise", "O serviço de IA não retornou dados.", null))
        }

        return try {
            // Attempt to find the JSON array part of the response
            val jsonStartIndex = responseText.indexOf("[")
            val jsonEndIndex = responseText.lastIndexOf("]")
            if (jsonStartIndex == -1 || jsonEndIndex == -1 || jsonStartIndex > jsonEndIndex) {
                throw IllegalArgumentException("Resposta JSON não encontrada ou malformada.")
            }
            val jsonArrayString = responseText.substring(jsonStartIndex, jsonEndIndex + 1)

            val aiInsights = Json {
                ignoreUnknownKeys = true
                isLenient = true // Allow for some minor formatting issues
            }.decodeFromString<List<AIInsight>>(jsonArrayString)

            aiInsights.map { aiInsight ->
                Insight(
                    title = aiInsight.title,
                    description = aiInsight.description,
                    recommendation = aiInsight.recommendation
                )
            }
        } catch (e: Exception) {
            println("Error parsing insights response: ${e.message}\nResponse was: $responseText")
            listOf(
                Insight(
                    title = "Erro ao Processar Análise",
                    description = "A resposta do serviço de IA não pôde ser processada.",
                    recommendation = "Isto pode ser um problema temporário. Detalhe: ${e.message}"
                )
            )
        }
    }
}
