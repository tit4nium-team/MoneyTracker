package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight
import com.google.firebase.vertexai.FirebaseVertexAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.math.abs
import android.util.Log
import com.example.moneytracker.config.ApiConfig

internal class AndroidFirebaseGeminiService {
    // TODO: Configure model name as needed, ensure it's available in Firebase Vertex AI
    private val modelName = "gemini-1.5-flash-latest" // Example model name

    // Access FirebaseVertexAI instance
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

        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildFinancialPrompt(transactions)
                // Use generateContentStream for potentially faster responses or if you need to process parts of the response as they arrive.
                // For simplicity, using generateContent which waits for the full response.
                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: throw IllegalStateException("Response text is null")
                
                parseInsights(text)
            } catch (e: Exception) {
                Log.e("FirebaseGeminiService", "Error generating insights", e)
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
        // Sort transactions by date, from most recent to oldest
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
                "${dateTime.month.name} ${dateTime.year}"
            }
            .toList()
            .sortedByDescending { (monthYear, _) ->
                val (month, year) = monthYear.split(" ")
                val monthNumber = Month.valueOf(month).ordinal
                year.toInt() * 100 + monthNumber
            }

        return """
            Atue como um consultor financeiro profissional e analise os seguintes dados financeiros:
            
            Resumo Financeiro:
            - Renda Total: R$ ${String.format("%.2f", totalIncome)}
            - Despesas Totais: R$ ${String.format("%.2f", totalExpenses)}
            - Saldo: R$ ${String.format("%.2f", balance)}
            
            Despesas por Categoria:
            ${categoryExpenses.joinToString("\n") { "- ${it.first}: R$ ${String.format("%.2f", it.second)}" }}
            
            Dados Mensais (do mais recente ao mais antigo):
            ${monthlyData.map { (monthYear, transactions) ->
                val monthlyExpenses = transactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                "- ${monthYear}: R$ ${String.format("%.2f", monthlyExpenses)}"
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
            Log.e("FirebaseGeminiService", "Error parsing insights", e)
            listOf(
                Insight(
                    title = "Análise Financeira",
                    description = text.take(150), // Take first 150 chars as a fallback
                    recommendation = "Continue monitorando suas finanças regularmente."
                )
            )
        }
    }

    private fun parseDate(dateStr: String): LocalDateTime {
        // Consider using a more robust date parsing library if needed, or ensure dateStr format is consistent
        return try {
            // Example: "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy"
            // This pattern needs to match the actual date string format from Transaction.date
            val pattern = "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy"
            val formatter = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
            val parsedDate = formatter.parse(dateStr)
            val instant = Instant.fromEpochMilliseconds(parsedDate.time)
            instant.toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            // Fallback or error handling for date parsing
            Log.e("FirebaseGeminiService", "Error parsing date: $dateStr", e)
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}

// Updated initialization function to use the new Firebase service
internal fun initializeGeminiService() {
    val firebaseService = AndroidFirebaseGeminiService()
    GeminiServiceFactory.setInstance(object : InsightGenerator() {
        override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
            return firebaseService.generateFinancialInsights(transactions)
        }
    })
}
