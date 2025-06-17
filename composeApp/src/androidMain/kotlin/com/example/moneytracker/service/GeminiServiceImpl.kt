package com.example.moneytracker.service

import com.example.moneytracker.BuildConfig
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.math.abs
import android.util.Log
import com.example.moneytracker.config.ApiConfig


internal class AndroidGeminiService {
    private var model: GenerativeModel? = null
    
    private val availableModels = listOf(
        "gemini-2.0-flash-lite"
    )
    
    private var currentModelIndex = 0

    private suspend fun tryNextModel(): GenerativeModel {
        if (currentModelIndex >= availableModels.size) {
            throw IllegalStateException("Tried all available models without success")
        }
        
        val modelName = availableModels[currentModelIndex]
        currentModelIndex++
        
        Log.i("GeminiService", "Trying model: $modelName")
        
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

        return withContext(Dispatchers.IO) {
            try {
                if (model == null) {
                    model = tryNextModel()
                }
                
                val prompt = buildFinancialPrompt(transactions)
                val response = model!!.generateContent(prompt)
                val text = response.text ?: throw IllegalStateException("Resposta vazia do modelo")
                
                // Converte a resposta em insights
                parseInsights(text)
            } catch (e: Exception) {
                Log.e("GeminiService", "Error with current model: ${e.message}")
                
                try {
                    model = tryNextModel()
                    val prompt = buildFinancialPrompt(transactions)
                    val response = model!!.generateContent(prompt)
                    val text = response.text ?: throw IllegalStateException("Resposta vazia do modelo")
                    parseInsights(text)
                } catch (e: Exception) {
                    Log.e("GeminiService", "Error generating insights", e)
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
        // Ordena as transações por data, da mais recente para a mais antiga
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
                // Extrai o mês e ano da string e converte para data para ordenação
                val (month, year) = monthYear.split(" ")
                val monthNumber = Month.valueOf(month).ordinal
                year.toInt() * 100 + monthNumber // Formato YYYYMM para ordenação correta
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
            // Remove qualquer texto antes e depois do JSON
            val jsonStr = text.substringAfter("[").substringBeforeLast("]")
            
            // Divide em objetos individuais
            jsonStr.split("},{")
                .map { str -> 
                    val cleanStr = str.trim()
                        .removeSurrounding("{", "}")
                        .trim()
                    
                    // Parse manual dos campos
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
            Log.e("GeminiService", "Error parsing insights", e)
            listOf(
                Insight(
                    title = "Análise Financeira",
                    description = text.take(150),
                    recommendation = "Continue monitorando suas finanças regularmente."
                )
            )
        }
    }

    private fun parseDate(dateStr: String): LocalDateTime {
        return try {
            val pattern = "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy"
            val formatter = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
            val parsedDate = formatter.parse(dateStr)
            val instant = Instant.fromEpochMilliseconds(parsedDate.time)
            instant.toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}

internal fun initializeGeminiService() {
    val androidService = AndroidGeminiService()
    GeminiServiceFactory.setInstance(object : InsightGenerator() {
        override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
            return androidService.generateFinancialInsights(transactions)
        }
    })
}