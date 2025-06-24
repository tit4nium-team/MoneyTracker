package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight
import com.google.firebase.vertexai.FirebaseVertexAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.math.abs
import platform.Foundation.NSLog

// Actual class implementing the expect InsightGenerator for iOS
actual class InsightGenerator actual constructor() {
    // Ensure this model name is available and configured in your Firebase project for Vertex AI
    private val modelName = "gemini-1.5-flash-latest" // Example model name for Firebase Vertex AI

    // GenerativeModel might need to be initialized after FirebaseApp.configure() is called.
    // Consider lazy initialization or initializing it in an init block if direct init causes issues.
    private val generativeModel by lazy {
        FirebaseVertexAI.getInstance().generativeModel(modelName)
    }

    actual suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
        if (transactions.isEmpty()) {
            return listOf(
                Insight(
                    title = "Bem-vindo ao Money Tracker",
                    description = "Adicione suas primeiras transações para receber insights personalizados",
                    recommendation = "Comece registrando suas despesas diárias para obter uma análise detalhada."
                )
            )
        }

        return withContext(Dispatchers.Default) { // Changed from Dispatchers.IO to Dispatchers.Default for Kotlin/Native compatibility
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
        val sortedTransactions: List<Transaction> = transactions.sortedByDescending {
            parseDate(it.date.toString()).toInstant(TimeZone.currentSystemDefault())
        }

        val totalIncome: Double = sortedTransactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpenses: Double = sortedTransactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
        val balance: Double = totalIncome - totalExpenses

        val categoryExpenses: List<Pair<String, Double>> = sortedTransactions
            .filter { it.amount < 0 }
            .groupBy { it.category.name }
            .mapValues { entry -> entry.value.sumOf { transaction -> abs(transaction.amount) } }
            .toList()
            .sortedByDescending { pair -> pair.second }

        val monthlyData: List<Pair<String, List<Transaction>>> = sortedTransactions
            .groupBy { transaction ->
                val dateTime: LocalDateTime = parseDate(transaction.date.toString())
                // Ensure month name localization or use numerical month if issues arise
                "${dateTime.month.name} ${dateTime.year}"
            }
            .toList()
            .sortedByDescending { (monthYear, _) ->
                val parts: List<String> = monthYear.split(" ")
                if (parts.size == 2) {
                    try {
                        val month = Month.valueOf(parts[0].uppercase()) // Ensure uppercase for enum matching
                        val year = parts[1].toInt()
                        year * 100 + month.ordinal
                    } catch (e: Exception) {
                        NSLog("Error parsing monthYear for sorting: $monthYear, Error: ${e.message}")
                        0 // Fallback
                    }
                } else {
                    0 // Fallback for sorting if format is unexpected
                }
            }

        // Using manual string formatting to avoid issues with Double.format extension in Kotlin/Native
        fun formatCurrency(value: Double): String {
            val roundedValue = (kotlin.math.round(value * 100) / 100.0)
            return "R$ $roundedValue" // Basic formatting, consider more robust for localization
        }

        return """
            Atue como um consultor financeiro profissional e analise os seguintes dados financeiros:

            Resumo Financeiro:
            - Renda Total: ${formatCurrency(totalIncome)}
            - Despesas Totais: ${formatCurrency(totalExpenses)}
            - Saldo: ${formatCurrency(balance)}

            Despesas por Categoria:
            ${categoryExpenses.joinToString("\n") { "- ${it.first}: ${formatCurrency(it.second)}" }}

            Dados Mensais (do mais recente ao mais antigo):
            ${monthlyData.map { (monthYear, monthTransactions) -> // Changed variable name to avoid conflict
                val monthlyExpenses = monthTransactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                "- ${monthYear}: ${formatCurrency(monthlyExpenses)}"
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

    // Helper to format double to string with 2 decimal places, useful for currency. (Removed as it was causing issues, direct formatting used in prompt)
    // private fun Double.format(digits: Int) = "%.${digits}f".format(this)


    private fun parseInsights(text: String): List<Insight> {
        return try {
            val jsonStr: String = text.substringAfter("[").substringBeforeLast("]")

            jsonStr.split("},{")
                .map { str: String ->
                    val cleanStr: String = str.trim()
                        .removeSurrounding("{", "}")
                        .trim()

                    val title: String = cleanStr.substringAfter("\"title\": \"").substringBefore("\"")
                    val description: String = cleanStr.substringAfter("\"description\": \"").substringBefore("\"")
                    val recommendation: String = cleanStr.substringAfter("\"recommendation\": \"").substringBefore("\"")

                    Insight(
                        title = title,
                        description = description,
                        recommendation = recommendation
                    )
                }
        } catch (e: Exception) {
            NSLog("IOSFirebaseGeminiService: Error parsing insights: ${e.message}")
            listOf<Insight>( // Explicit type for list
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

// Actual factory implementation for iOS
internal actual object GeminiServiceFactory {
    // Lazy initialization of the singleton instance
    private val instance: InsightGenerator by lazy { InsightGenerator() }

    actual fun getInstance(): InsightGenerator {
        return instance
    }
}

// Actual initialization function for iOS
actual fun initializeGeminiService() {
    // Service is lazily initialized by GeminiServiceFactory.getInstance()
    // This function can be called from Swift to ensure the factory object is initialized if needed,
    // though direct calls to getInstance() will also initialize it.
    NSLog("IOSFirebaseGeminiService: Initialized (or will be on first use)")
}
