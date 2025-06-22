package com.example.moneytracker.service

import com.example.moneytracker.model.Insight
import com.example.moneytracker.model.Transaction
import com.google.firebase.vertexai.FirebaseVertexAI
import com.google.firebase.vertexai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.abs

// Data class para deserializar a resposta JSON do modelo
@Serializable
private data class InsightResponse(
    val title: String,
    val description: String,
    val recommendation: String
)

object FirebaseVertexAiService {

    // Configuração do modelo generativo
    // Usar "gemini-1.5-flash" como um modelo padrão. Pode ser necessário ajustar.
    // A API Key não é mais necessária aqui, pois o SDK do Firebase lida com a autenticação.
    private val generativeModel = FirebaseVertexAI.getInstance()
        .generativeModel(
            modelName = "gemini-1.5-flash", // Modelo recomendado para velocidade e custo-benefício
            // Para forçar saída JSON, podemos usar responseMimeType, mas exige prompt específico
            // generationConfig = generationConfig {
            //     responseMimeType = "application/json"
            // }
        )

    private val json = Json { isLenient = true; ignoreUnknownKeys = true }

    suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
        if (transactions.isEmpty()) {
            return listOf(
                Insight(
                    title = "Bem-vindo ao Money Tracker",
                    description = "Adicione suas primeiras transações para receber insights personalizados.",
                    recommendation = "Comece registrando suas despesas diárias para obter uma análise detalhada."
                )
            )
        }

        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildFinancialPrompt(transactions)
                // O SDK KMP do Firebase AI não tem um log explícito como o Log.i do Android.
                // Se for necessário logging, pode-se usar uma biblioteca KMP de logging ou println para debug.
                println("FirebaseVertexAiService: Gerando insights com prompt: $prompt")

                val response = generativeModel.generateContent(prompt)

                val responseText = response.text
                if (responseText == null) {
                    println("FirebaseVertexAiService: Resposta vazia do modelo.")
                    throw IllegalStateException("Resposta vazia do modelo")
                }

                println("FirebaseVertexAiService: Resposta recebida: $responseText")
                parseInsights(responseText)

            } catch (e: Exception) {
                // Log de erro (usando println para KMP, ou uma lib de log KMP)
                println("FirebaseVertexAiService: Erro ao gerar insights: ${e.message}")
                e.printStackTrace() // Para debug, pode ser útil
                listOf(
                    Insight(
                        title = "Erro ao Gerar Insights",
                        description = "Não foi possível analisar suas transações no momento.",
                        recommendation = "Por favor, tente novamente mais tarde. Detalhe: ${e.message}"
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
                "${dateTime.month.name.toLowerCase().capitalize()} ${dateTime.year}" // Formatado para melhor leitura
            }
            .toList()
            .sortedByDescending { (monthYear, _) ->
                val parts = monthYear.split(" ")
                if (parts.size == 2) {
                    val monthName = parts[0].toUpperCase() // Month.valueOf espera uppercase
                    val year = parts[1].toIntOrNull()
                    if (year != null && Month.entries.any { it.name == monthName }) {
                        val monthNumber = Month.valueOf(monthName).ordinal
                        year * 100 + monthNumber
                    } else {
                        0 // Fallback para ordenação
                    }
                } else {
                    0 // Fallback para ordenação
                }
            }

        // Instrução para o modelo retornar JSON.
        // É importante que o modelo Gemini que você está usando suporte bem a geração de JSON.
        // "gemini-1.5-flash" geralmente lida bem com isso.
        return """
            Atue como um consultor financeiro profissional e analise os seguintes dados financeiros:

            Resumo Financeiro:
            - Renda Total: R$ ${String.format("%.2f", totalIncome)}
            - Despesas Totais: R$ ${String.format("%.2f", totalExpenses)}
            - Saldo: R$ ${String.format("%.2f", balance)}

            Despesas por Categoria (Top 5):
            ${categoryExpenses.take(5).joinToString("\n") { "- ${it.first}: R$ ${String.format("%.2f", it.second)}" }}

            Dados Mensais (Despesas dos últimos 3 meses, do mais recente ao mais antigo):
            ${monthlyData.take(3).map { (monthYear, monthTransactions) ->
                val monthlyExpenses = monthTransactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                "- ${monthYear}: R$ ${String.format("%.2f", monthlyExpenses)}"
            }.joinToString("\n")}

            Gere EXATAMENTE 3 insights financeiros distintos no seguinte formato JSON (uma lista de objetos JSON).
            Responda APENAS com o JSON, sem nenhum texto ou formatação adicional antes ou depois do array JSON.
            [
              {
                "title": "Título do Insight (máximo 50 caracteres)",
                "description": "Descrição detalhada do insight (máximo 150 caracteres)",
                "recommendation": "Recomendação prática (máximo 100 caracteres)"
              },
              {
                "title": "Outro Título de Insight (máximo 50 caracteres)",
                "description": "Outra descrição detalhada (máximo 150 caracteres)",
                "recommendation": "Outra recomendação (máximo 100 caracteres)"
              },
              {
                "title": "Mais um Título (máximo 50 caracteres)",
                "description": "Mais uma descrição (máximo 150 caracteres)",
                "recommendation": "Mais uma recomendação (máximo 100 caracteres)"
              }
            ]

            Cada insight deve focar em um aspecto diferente, por exemplo:
            1. Visão geral da saúde financeira ou fluxo de caixa.
            2. Análise de gastos por categoria (destacar maiores gastos ou oportunidades de economia).
            3. Tendências mensais ou comportamento de gastos ao longo do tempo.

            Mantenha as respostas concisas e diretas, respeitando os limites de caracteres.
            A resposta DEVE ser um array JSON válido. Não inclua a palavra "json" ou ```json no início ou fim.
        """.trimIndent()
    }

    private fun parseInsights(jsonText: String): List<Insight> {
        return try {
            // Tentativa de limpar o texto, caso o modelo adicione ```json ou similar
            val cleanedJsonText = jsonText
                .replaceFirst("```json", "")
                .replaceFirst("```", "")
                .trim()

            val insightResponses = json.decodeFromString<List<InsightResponse>>(cleanedJsonText)
            insightResponses.map { response ->
                Insight(
                    title = response.title,
                    description = response.description,
                    recommendation = response.recommendation
                )
            }
        } catch (e: Exception) {
            println("FirebaseVertexAiService: Erro ao parsear JSON de insights: ${e.message}")
            e.printStackTrace()
            // Fallback se o parsing falhar, tenta pegar o texto bruto como descrição.
            listOf(
                Insight(
                    title = "Análise Financeira Recebida",
                    description = jsonText.take(150), // Pega os primeiros 150 chars da resposta
                    recommendation = "Houve um problema ao formatar os insights. Verifique a resposta completa se necessário."
                )
            )
        }
    }

    // TODO: Esta função de parse de data é específica da JVM (SimpleDateFormat).
    // Precisamos de uma implementação KMP para parse de datas se `transaction.date` for uma String complexa.
    // Se `transaction.date` já for um tipo KMP (kotlinx-datetime), esta função pode ser simplificada ou removida.
    // Por agora, vou manter uma implementação simples assumindo um formato que kotlinx-datetime possa parsear
    // ou que precise de uma lógica mais robusta. A implementação original usava SimpleDateFormat.
    private fun parseDate(dateStr: String): LocalDateTime {
        // Handle the format "Wed Jun 11 20:30:02 GMT-03:00 2025"
        // This logic is copied from InsightsViewModel for consistency.
        val regex = """(\w{3}) (\w{3}) (\d{1,2}) (\d{2}):(\d{2}):(\d{2}) GMT([+-]\d{2}):(\d{2}) (\d{4})""".toRegex()
        val match = regex.find(dateStr)

        return if (match != null) {
            val groups = match.groupValues
            // val dayOfWeek = groups[1] // Ex: "Wed"
            val monthAbbr = groups[2] // Ex: "Jun"
            val day = groups[3].toInt()
            val hour = groups[4].toInt()
            val minute = groups[5].toInt()
            val second = groups[6].toInt()
            // val gmtOffset = groups[7] // Ex: "-03"
            // val gmtOffsetMinutes = groups[8] // Ex: "00"
            val year = groups[9].toInt()

            val monthNumber = when (monthAbbr) {
                "Jan" -> 1; "Feb" -> 2; "Mar" -> 3; "Apr" -> 4
                "May" -> 5; "Jun" -> 6; "Jul" -> 7; "Aug" -> 8
                "Sep" -> 9; "Oct" -> 10; "Nov" -> 11; "Dec" -> 12
                else -> throw IllegalArgumentException("Invalid month abbreviation: $monthAbbr")
            }

            // kotlinx.datetime.LocalDateTime para representar a data e hora lidas
            // Nota: O offset GMT original é perdido aqui, mas para ordenação e agrupamento por mês/ano,
            // a data/hora local (ou UTC se fosse o caso) é geralmente o que se usa.
            // Se o fuso horário exato for crucial para a lógica do prompt, precisaria ser tratado.
            LocalDateTime(year, monthNumber, day, hour, minute, second)
        } else {
            // Fallback se o regex não encontrar o padrão.
            // Tentar como timestamp Long, depois como ISO string, e por último, tempo atual.
            try {
                Instant.fromEpochMilliseconds(dateStr.toLong()).toLocalDateTime(TimeZone.currentSystemDefault())
            } catch (e: NumberFormatException) {
                try {
                    Instant.parse(dateStr).toLocalDateTime(TimeZone.currentSystemDefault())
                } catch (e2: Exception) {
                    kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                }
            }
        }
    }
}
