package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight
import com.google.firebase.vertexai.FirebaseVertexAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.math.abs
import platform.Foundation.NSLog

// iOS-specific implementation for Firebase Gemini Service
internal class IOSFirebaseGeminiService {
    // Ensure this model name is available and configured in your Firebase project for Vertex AI
    private val modelName = "gemini-1.5-flash-latest"
    private val generativeModel = FirebaseVertexAI.getInstance().generativeModel(modelName)

    suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
        if (transactions.isEmpty()) {
            return listOf(
                Insight(
                    title = "Bem-vindo ao Money Tracker",
                    description = "Adicione suas primeiras transações para receber insights personalizados",
                    recommendation = "Comece registrando suas despesas diárias para obter uma análise detalhada."
                )
            )
        }

        return withContext(Dispatchers.IO) { // Dispatchers.IO might not be directly applicable in Kotlin/Native for iOS in the same way as JVM. Consider platform-specific dispatchers if needed.
            try {
                val prompt = buildFinancialPrompt(transactions)
                val response = generativeModel.generateContent(prompt) // Ensure this API is consistent for iOS Kotlin Multiplatform
                val text = response.text ?: throw IllegalStateException("Response text is null from Firebase Vertex AI")

                parseInsights(text)
            } catch (e: Exception) {
                NSLog("IOSFirebaseGeminiService: Error generating insights: ${e.message}")
                listOf(
                    Insight(
                        title = "Erro ao Gerar Insights",
                        description = "Não foi possível analisar suas transações no momento.",
                        recommendation = "Por favor, tente novamente mais tarde."
                    )
                )
            }
        }
    }

    private fun buildFinancialPrompt(transactions: List<Transaction>): String {
        val sortedTransactions = transactions.sortedByDescending {
            parseDate(it.date.toString()).toInstant(TimeZone.currentSystemDefault())
        }

        val totalIncome = sortedTransactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpenses = sortedTransactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
        val balance = totalIncome - totalExpenses

        val categoryExpenses = sortedTransactions
            .filter { it.amount < 0 }
            .groupBy { it.category.name }
            .mapValues { it.value.sumOf { transaction -> abs(transaction.amount) } }
            .toList()
            .sortedByDescending { it.second }

        val monthlyData = sortedTransactions
            .groupBy { transaction ->
                val dateTime = parseDate(transaction.date.toString())
                // Ensure month name localization or use numerical month if issues arise
                "${dateTime.month.name} ${dateTime.year}"
            }
            .toList()
            .sortedByDescending { (monthYear, _) ->
                val parts = monthYear.split(" ")
                if (parts.size == 2) {
                    val month = Month.valueOf(parts[0]) // This might need adjustment based on actual month name format/localization
                    val year = parts[1].toInt()
                    year * 100 + month.ordinal
                } else {
                    0 // Fallback for sorting if format is unexpected
                }
            }

        return """
            Atue como um consultor financeiro profissional e analise os seguintes dados financeiros:

            Resumo Financeiro:
            - Renda Total: R$ ${totalIncome.format(2)}
            - Despesas Totais: R$ ${totalExpenses.format(2)}
            - Saldo: R$ ${balance.format(2)}

            Despesas por Categoria:
            ${categoryExpenses.joinToString("\n") { "- ${it.first}: R$ ${it.second.format(2)}" }}

            Dados Mensais (do mais recente ao mais antigo):
            ${monthlyData.map { (monthYear, transactions) ->
                val monthlyExpenses = transactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                "- ${monthYear}: R$ ${monthlyExpenses.format(2)}"
            }.joinToString("\n")}

            Gere 3 insights diferentes no seguinte formato JSON:
            [
              {
                "title": "Título do Insight (máximo 50 caracteres)",
                "description": "Descrição detalhada do insight (máximo 150 caracteres)",
                "recommendation": "Recomendação prática (máximo 100 caracteres)"
              }
            ]

            Cada insight deve focar em um aspecto diferente:
            1. Visão geral da saúde financeira
            2. Análise de gastos por categoria
            3. Tendências mensais

            Mantenha as respostas concisas e diretas, respeitando os limites de caracteres.
            IMPORTANTE: Responda APENAS com o JSON, sem texto adicional.
        """.trimIndent()
    }

    // Helper to format double to string with 2 decimal places, useful for currency.
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)


    private fun parseInsights(text: String): List<Insight> {
        return try {
            val jsonStr = text.substringAfter("[").substringBeforeLast("]")

            jsonStr.split("},{")
                .map { str ->
                    val cleanStr = str.trim()
                        .removeSurrounding("{", "}")
                        .trim()

                    val title = cleanStr.substringAfter("\"title\": \"").substringBefore("\"")
                    val description = cleanStr.substringAfter("\"description\": \"").substringBefore("\"")
                    val recommendation = cleanStr.substringAfter("\"recommendation\": \"").substringBefore("\"")

                    Insight(
                        title = title,
                        description = description,
                        recommendation = recommendation
                    )
                }
        } catch (e: Exception) {
            NSLog("IOSFirebaseGeminiService: Error parsing insights: ${e.message}")
            listOf(
                Insight(
                    title = "Análise Financeira",
                    description = text.take(150),
                    recommendation = "Continue monitorando suas finanças regularmente."
                )
            )
        }
    }

    // Date parsing needs to be compatible with iOS Kotlin/Native.
    // The java.text.SimpleDateFormat is not available. Consider kotlinx-datetime or platform-specific date formatting.
    private fun parseDate(dateStr: String): LocalDateTime {
         // This is a placeholder. kotlinx-datetime should be used for robust date parsing.
         // Example: Assuming dateStr is in ISO 8601 format "YYYY-MM-DDTHH:MM:SSZ"
        return try {
            // Attempt to parse with a common format, adjust as necessary
            // This is a simplified example. Real-world parsing might need more robust error handling or specific formatters.
            Instant.parse(dateStr).toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            NSLog("IOSFirebaseGeminiService: Error parsing date '$dateStr': ${e.message}")
            // Fallback to current time if parsing fails. Consider how critical accurate date parsing is.
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}

// Initialization function for iOS, to be called from Swift code (e.g., AppDelegate or SwiftUI App struct)
fun initializeIOSGeminiService() {
    val iosService = IOSFirebaseGeminiService()
    // Set this instance in your shared factory/service locator
    GeminiServiceFactory.setInstance(object : InsightGenerator() {
        override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
            return iosService.generateFinancialInsights(transactions)
        }
    })
}
