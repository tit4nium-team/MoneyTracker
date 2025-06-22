package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight

import com.example.moneytracker.model.Insight
import com.example.moneytracker.model.Transaction // Ainda necessário para o método legado na factory e interface

// Interface que as implementações de plataforma (Android/iOS) fornecerão.
// Ela lidará diretamente com a chamada ao SDK do Firebase AI.
internal expect class PlatformGeminiManager() {
    suspend fun generateContent(prompt: String): String? // Retorna o JSON da resposta ou null em caso de erro
}

// InsightGenerator agora usará PlatformGeminiManager para obter o conteúdo JSON
// e então (idealmente) parseá-lo.
abstract class InsightGenerator {
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException

// ... (outros imports e expect class PlatformGeminiManager)

abstract class InsightGenerator {
    abstract suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight>

    // Configuração do parser JSON para ser um pouco mais tolerante
    private val jsonParser = Json {
        ignoreUnknownKeys = true // Ignora chaves no JSON que não estão no data class Insight
        isLenient = true // Permite algumas malformações leves no JSON (usar com cautela)
        prettyPrint = false // Não necessário para parsing, mas útil para debugging de output
    }

    protected fun parseInsightsFromJson(jsonString: String?): List<Insight> {
        if (jsonString.isNullOrBlank()) {
            // Log.w("InsightGenerator", "JSON string nula ou vazia recebida para parsing.")
            return listOf(Insight("Erro de Geração", "Resposta da IA vazia ou nula.", "Verifique a conexão ou o prompt."))
        }
        return try {
            // Log.d("InsightGenerator", "Tentando parsear JSON: $jsonString")
            jsonParser.decodeFromString<List<Insight>>(jsonString)
        } catch (e: SerializationException) {
            // Log.e("InsightGenerator", "Erro de serialização ao parsear JSON: ${e.message}", e)
            listOf(Insight("Erro de Parsing", "Falha ao decodificar resposta da IA: ${e.message}", jsonString.take(200)))
        } catch (e: Exception) { // Captura outras exceções inesperadas durante o parsing
            // Log.e("InsightGenerator", "Erro inesperado ao parsear JSON: ${e.message}", e)
            listOf(Insight("Erro Inesperado", "Ocorreu um erro inesperado ao processar a resposta da IA.", jsonString.take(200)))
        }
    }
}

// Esta função será implementada em androidMain e iosMain para fornecer a instância específica da plataforma.
internal expect fun initializePlatformManager(): PlatformGeminiManager

internal object GeminiServiceFactory {
    private var platformManager: PlatformGeminiManager? = null
    private var insightGenerator: InsightGenerator? = null

    private fun getPlatformManagerInstance(): PlatformGeminiManager {
        if (platformManager == null) {
            platformManager = initializePlatformManager()
        }
        return platformManager!!
    }

    fun getInstance(): InsightGenerator {
        if (insightGenerator == null) {
            val currentManager = getPlatformManagerInstance()
            insightGenerator = object : InsightGenerator() {
                override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
                    if (transactions.isEmpty()) {
                        return listOf(
                            Insight(
                                title = "Bem-vindo ao Money Tracker",
                                description = "Adicione suas primeiras transações para receber insights personalizados.",
                                recommendation = "Comece registrando suas despesas diárias."
                            )
                        )
                    }
                    val prompt = buildFinancialPromptCommon(transactions) // Usando a função de prompt comum
                    val jsonResponse = currentManager.generateContent(prompt)
                    return parseInsightsFromJson(jsonResponse)
                }
            }
        }
        return insightGenerator!!
    }
}

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs


// Adaptação da função parseDate para commonMain.
// CRÍTICO: Esta implementação assume que 'dateStr' está no formato ISO 8601 (ex: "2023-10-26T10:15:30Z").
// Se o formato for diferente (ex: "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy" ou outros),
// esta função falhará ou retornará resultados incorretos.
// RECOMENDAÇÃO: Implemente expect/actual para parseDate se precisar de formatos complexos não-ISO,
// ou garanta que as datas de entrada estejam sempre no formato ISO 8601.
private fun commonParseDate(dateStr: String): LocalDateTime {
    return try {
        Instant.parse(dateStr).toLocalDateTime(TimeZone.currentSystemDefault())
    } catch (e: Exception) {
        // TODO: Adicionar logging de erro mais robusto aqui ou lançar uma exceção customizada.
        println("WARN: Falha ao parsear data '$dateStr' como ISO 8601. Usando data/hora atual como fallback. Isso pode levar a insights incorretos.")
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
}

// Função comum para construir o prompt, agora com a lógica mais detalhada.
internal fun buildFinancialPromptCommon(transactions: List<Transaction>): String {
    if (transactions.isEmpty()) {
        // Não deveria chegar aqui se a factory já trata isso, mas como defesa.
        return "Sem transações para analisar. Por favor, adicione transações."
    }

    val sortedTransactions = transactions.sortedByDescending {
        commonParseDate(it.date.toString()).toInstant(TimeZone.currentSystemDefault())
    }

    val totalIncome = sortedTransactions.filter { it.amount > 0 }.sumOf { it.amount }
    val totalExpenses = sortedTransactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
    val balance = totalIncome - totalExpenses

    val categoryExpenses = sortedTransactions
        .filter { it.amount < 0 }
        .groupBy { it.category.name } // Usar o nome da categoria
        .mapValues { entry -> entry.value.sumOf { transaction -> abs(transaction.amount) } }
        .toList()
        .sortedByDescending { it.second }
        .take(5) // Limitar às top 5 categorias para brevidade no prompt

    val monthlyData = sortedTransactions
        .groupBy { transaction ->
            val dateTime = commonParseDate(transaction.date.toString())
            // Formato MM-YYYY para consistência e facilidade de ordenação
            "${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.year}"
        }
        .mapValues { entry ->
            // Dentro de cada mês/ano, calcular despesas
            entry.value.filter { it.amount < 0 }.sumOf { abs(it.amount) }
        }
        .toList()
        .sortedByDescending { (monthYear, _) ->
            val parts = monthYear.split("-")
            val month = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val year = parts.getOrNull(1)?.toIntOrNull() ?: 0
            year * 100 + month // Ordenar por YYYYMM
        }
        .take(3) // Limitar aos últimos 3 meses para brevidade

    // Helper para formatar Double para String com 2 casas decimais (KMP safe)
    fun Double.formatAmount(): String {
        val rounded = (this * 100).toLong() / 100.0
        val s = rounded.toString()
        val parts = s.split('.')
        val intPart = parts[0]
        val decPart = parts.getOrNull(1)?.padEnd(2, '0') ?: "00"
        return "$intPart.${decPart.take(2)}"
    }

    val promptBuilder = StringBuilder()
    promptBuilder.appendLine("Atue como um consultor financeiro profissional e analise os seguintes dados financeiros de um usuário:")
    promptBuilder.appendLine()
    promptBuilder.appendLine("Resumo Financeiro:")
    promptBuilder.appendLine("- Renda Total: R$ ${totalIncome.formatAmount()}")
    promptBuilder.appendLine("- Despesas Totais: R$ ${totalExpenses.formatAmount()}")
    promptBuilder.appendLine("- Saldo: R$ ${balance.formatAmount()}")
    promptBuilder.appendLine()

    if (categoryExpenses.isNotEmpty()) {
        promptBuilder.appendLine("Principais Despesas por Categoria (Top 5):")
        categoryExpenses.forEach { (categoryName, amount) ->
            promptBuilder.appendLine("- ${categoryName}: R$ ${amount.formatAmount()}")
        }
        promptBuilder.appendLine()
    }

    if (monthlyData.isNotEmpty()) {
        promptBuilder.appendLine("Despesas Mensais Recentes (Últimos 3 meses):")
        monthlyData.forEach { (monthYear, amount) ->
            // Tentar reformatar monthYear para nome do mês se desejado. Ex: "05-2024" -> "MAIO 2024"
            val parts = monthYear.split("-")
            val monthName = try { Month(parts[0].toInt()).name } catch (e:Exception) { monthYear }
            val year = parts.getOrNull(1) ?: ""
            promptBuilder.appendLine("- $monthName $year: R$ ${amount.formatAmount()}")
        }
        promptBuilder.appendLine()
    }

    promptBuilder.appendLine("Com base nesses dados, gere EXATAMENTE 3 insights financeiros distintos e práticos para o usuário.")
    promptBuilder.appendLine("Siga ESTRITAMENTE o seguinte formato JSON para a resposta (um array de 3 objetos):")
    promptBuilder.appendLine("""
    [
      {
        "title": "Título Curto e Impactante do Insight (max 50 chars)",
        "description": "Descrição clara e concisa do insight, explicando o que foi observado (max 150 chars).",
        "recommendation": "Recomendação prática e acionável que o usuário pode seguir (max 100 chars)."
      },
      {
        "title": "Outro Título de Insight (max 50 chars)",
        "description": "Outra observação importante sobre as finanças (max 150 chars).",
        "recommendation": "Outra sugestão ou dica útil (max 100 chars)."
      },
      {
        "title": "Terceiro Insight (max 50 chars)",
        "description": "Terceira análise relevante dos dados (max 150 chars).",
        "recommendation": "Terceira recomendação para melhoria ou atenção (max 100 chars)."
      }
    ]
    """.trimIndent())
    promptBuilder.appendLine("IMPORTANTE: Responda APENAS com o array JSON. Não inclua nenhum texto, introdução, despedida ou qualquer outra coisa fora do array JSON.")
    promptBuilder.appendLine("Se os dados forem insuficientes para 3 insights significativos, forneça quantos forem possíveis, mantendo o formato de array JSON. Se nenhum insight puder ser gerado, retorne um array JSON vazio: [].")

    return promptBuilder.toString()
}