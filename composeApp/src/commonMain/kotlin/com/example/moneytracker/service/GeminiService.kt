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
    abstract suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight>

    protected fun parseInsightsFromJson(jsonString: String?): List<Insight> {
        if (jsonString.isNullOrBlank()) {
            return listOf(Insight("Erro de Geração", "Resposta da IA vazia ou nula.", "Verifique a conexão ou o prompt."))
        }
        try {
            // Validação básica de JSON Array
            if (!jsonString.trim().startsWith("[") || !jsonString.trim().endsWith("]")) {
                 return listOf(Insight("Erro de Formato", "Resposta da IA não é um array JSON válido.", jsonString.take(200)))
            }

            // Tentativa de parsing manual simples. kotlinx.serialization é recomendado para produção.
            val objectsStr = jsonString.trim().removePrefix("[").removeSuffix("]").trim()
            if (objectsStr.isEmpty()) return emptyList()

            return objectsStr.split("},{").mapNotNull { objStr ->
                val completeObjStr = when {
                    objectsStr.startsWith("{") && objectsStr.endsWith("}") -> objStr // Array de um único objeto
                    objStr.startsWith("{") && objStr.endsWith("}") -> objStr
                    objStr.startsWith("{") -> "$objStr}"
                    objStr.endsWith("}") -> "{$objStr"
                    else -> "{$objStr}"
                }

                try {
                    val title = completeObjStr.substringAfter("\"title\": \"", "").substringBefore("\"", "")
                    val description = completeObjStr.substringAfter("\"description\": \"", "").substringBefore("\"", "")
                    val recommendation = completeObjStr.substringAfter("\"recommendation\": \"", "").substringBefore("\"", "")

                    if (title.isNotBlank() || description.isNotBlank() || recommendation.isNotBlank()) { // Permite campos parcialmente vazios se outros estiverem ok
                        Insight(title, description, recommendation)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    // Logar erro de parsing de objeto individual se necessário
                    null
                }
            }.ifEmpty {
                 listOf(Insight("Sem Insights", "Nenhum insight utilizável pode ser parseado da resposta.", jsonString.take(200)))
            }
        } catch (e: Exception) {
            return listOf(Insight("Erro de Parsing", "Exceção ao processar resposta da IA: ${e.message}", jsonString.take(200)))
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

// Função comum para construir o prompt, pode ser usada por Android e iOS (via `actual` implementations se necessário)
// ou diretamente se a lógica for 100% compartilhada.
// Movida para fora da factory para melhor organização.
internal fun buildFinancialPromptCommon(transactions: List<Transaction>): String {
    // Lógica de ordenação e agrupamento de transações (exemplo simplificado)
    // Idealmente, usar kotlinx-datetime para manipulação de datas de forma KMP.
    // A lógica de parseDate() precisará ser KMP compatível ou platform-specific.
    // Esta é uma simplificação e pode precisar de adaptação.
    val transactionDetails = transactions.take(10).joinToString("\n") { t ->
        "Data: ${t.date}, Desc: ${t.description}, Valor: ${t.amount}, Cat: ${t.category.name}"
    }

    return """
        Analise as seguintes transações financeiras (máximo de 10 listadas para brevidade):
        $transactionDetails

        Gere 3 insights financeiros concisos e práticos no seguinte formato JSON (array de objetos):
        [
          {
            "title": "Título Curto do Insight (até 50 chars)",
            "description": "Descrição um pouco mais detalhada do insight (até 150 chars).",
            "recommendation": "Recomendação ou ação sugerida (até 100 chars)."
          }
        ]
        Responda APENAS com o JSON. Não inclua nenhum texto ou explicação adicional fora do JSON.
        Se não houver transações suficientes ou insights claros, retorne um array JSON vazio: [].
    """.trimIndent()
}

// A antiga `initializeGeminiService()` é efetivamente substituída pela `initializePlatformManager()`.
// Não precisamos mais de uma `expect fun initializeGeminiService()`.