package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.math.abs
import com.example.moneytracker.config.ApiConfig

// TODO: Implement proper logging for iOS
// import platform.Foundation.NSLog

internal class IosGeminiService {
    private var model: GenerativeModel? = null

    // TODO: Check if model list needs to be different for iOS
    private val availableModels = listOf(
        "gemini-1.5-flash"
    )

    private var currentModelIndex = 0

    private suspend fun tryNextModel(): GenerativeModel {
        if (currentModelIndex >= availableModels.size) {
            throw IllegalStateException("Tried all available models without success")
        }

        val modelName = availableModels[currentModelIndex]
        currentModelIndex++

        // NSLog("GeminiService: Trying model: $modelName") TODO: Use platform specific logging

        return GenerativeModel(
            modelName = modelName,
            apiKey = ApiConfig.GEMINI_API_KEY
        )
    }

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

        return withContext(Dispatchers.Default) { // Use Dispatchers.Default for iOS
            try {
                if (model == null) {
                    model = tryNextModel()
                }

                val prompt = buildFinancialPrompt(transactions)
                // TODO: Ensure generateContent is available and works the same way on iOS
                val response = model!!.generateContent(prompt)
                val text = response.text ?: throw IllegalStateException("Resposta vazia do modelo")

                parseInsights(text)
            } catch (e: Exception) {
                // NSLog("GeminiService: Error with current model: ${e.message}") TODO: Use platform specific logging

                try {
                    model = tryNextModel()
                    val prompt = buildFinancialPrompt(transactions)
                    val response = model!!.generateContent(prompt)
                    val text = response.text ?: throw IllegalStateException("Resposta vazia do modelo")
                    parseInsights(text)
                } catch (e: Exception) {
                    // NSLog("GeminiService: Error generating insights: ${e.message}") TODO: Use platform specific logging
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
                "${dateTime.month.name} ${dateTime.year}"
            }
            .toList()
            .sortedByDescending { (monthYear, _) ->
                val (month, year) = monthYear.split(" ")
                val monthNumber = Month.valueOf(month).ordinal // Ensure Month.valueOf works as expected
                year.toInt() * 100 + monthNumber
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

    // Helper to format Double to String with 2 decimal places
    private fun Double.format(digits: Int): String = "%.${digits}f".format(this)


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
            // NSLog("GeminiService: Error parsing insights: ${e.message}") TODO: Use platform specific logging
            listOf(
                Insight(
                    title = "Análise Financeira",
                    description = text.take(150),
                    recommendation = "Continue monitorando suas finanças regularmente."
                )
            )
        }
    }

    // TODO: This date parsing might need adjustment for iOS or could be moved to common code if compatible
    private fun parseDate(dateStr: String): LocalDateTime {
    try {
        // Standard ISO format (YYYY-MM-DDTHH:MM:SSZ or YYYY-MM-DDTHH:MM:SS.sssZ)
        // This is a common format from servers or JavaScript Date.toISOString()
        if (dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z"))) {
            return Instant.parse(dateStr).toLocalDateTime(TimeZone.currentSystemDefault())
        }

        // Format: "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy" (e.g., "Mon May 27 10:30:00 GMT+00:00 2024")
        // This format was in the Android implementation. We need a KMP compatible way or an iOS specific one.
        // For now, let's assume a simpler, more standard format might be coming from the source.
        // If not, this part will need a robust KMP date parsing solution or platform-specific parsing.
        // Consider kotlinx-datetime capabilities for more complex parsing if needed.
        // As a fallback, or if the format is fixed and simple:
        // e.g. if dateStr is "YYYY-MM-DD HH:MM:SS"
        // val components = dateStr.split(" ", "T", ":", "-").map { it.toInt() }
        // return LocalDateTime(components[0], components[1], components[2], components[3], components[4], components[5])

    } catch (e: Exception) {
        // NSLog("Error parsing date: $dateStr, error: ${e.message}") // TODO: Platform specific logging
    }
    // Fallback to current time if parsing fails
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
}

internal actual fun initializeGeminiService() {
    val iosService = IosGeminiService()
    GeminiServiceFactory.setInstance(object : InsightGenerator() {
        override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
            return iosService.generateFinancialInsights(transactions)
        }
    })
}
