package com.example.moneytracker.service

import com.example.moneytracker.BuildConfig
import com.example.moneytracker.model.Transaction
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.math.abs

open class GeminiService {
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    open suspend fun generateFinancialInsights(transactions: List<Transaction>): String {
        if (transactions.isEmpty()) {
            return "Adicione suas primeiras transações para receber insights personalizados"
        }

        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildFinancialPrompt(transactions)
                val response = model.generateContent(prompt)
                response.text ?: "Não foi possível gerar insights no momento"
            } catch (e: Exception) {
                e.printStackTrace()
                "Não foi possível gerar insights no momento"
            }
        }
    }

    private fun buildFinancialPrompt(transactions: List<Transaction>): String {
        val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
        val balance = totalIncome - totalExpenses

        val categoryExpenses = transactions
            .filter { it.amount < 0 }
            .groupBy { it.category.name }
            .mapValues { it.value.sumOf { transaction -> abs(transaction.amount) } }
            .toList()
            .sortedByDescending { it.second }

        val monthlyData = transactions.groupBy { transaction ->
            val dateTime = parseDate(transaction.date.toString())
            "${dateTime.month.name} ${dateTime.year}"
        }

        return """
            Atue como um consultor financeiro profissional e analise os seguintes dados financeiros:
            
            Resumo Financeiro:
            - Renda Total: R$ ${String.format("%.2f", totalIncome)}
            - Despesas Totais: R$ ${String.format("%.2f", totalExpenses)}
            - Saldo: R$ ${String.format("%.2f", balance)}
            
            Despesas por Categoria:
            ${categoryExpenses.joinToString("\n") { "- ${it.first}: R$ ${String.format("%.2f", it.second)}" }}
            
            Dados Mensais:
            ${monthlyData.map { entry ->
                val monthlyExpenses = entry.value.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                "- ${entry.key}: R$ ${String.format("%.2f", monthlyExpenses)}"
            }.joinToString("\n")}
            
            Por favor, forneça uma análise concisa (máximo 3 frases) que inclua:
            1. Uma visão geral da saúde financeira
            2. Identificação de padrões de gastos relevantes
            3. Uma recomendação específica para melhorar a situação financeira
            
            Mantenha a resposta informal e amigável, como se estivesse conversando com o usuário.
        """.trimIndent()
    }

    private fun parseDate(dateStr: String): LocalDateTime {
        return try {
            // Tenta converter a string da data para um formato que podemos processar
            val pattern = "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy"
            val formatter = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
            val parsedDate = formatter.parse(dateStr)
            val instant = Instant.fromEpochMilliseconds(parsedDate.time)
            instant.toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            // Se falhar, retorna a data atual como fallback
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
} 