package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import platform.Foundation.NSLog
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

// Importa o wrapper Swift. O nome exato pode depender da configuração do seu projeto e do nome do módulo.
// Se o seu módulo de app iOS for "iosApp", e o arquivo Swift estiver nesse módulo,
// o Kotlin/Native geralmente o expõe sob o nome do módulo.
// Se esta importação direta não funcionar, pode ser necessário ajustar como o Kotlin/Native "vê" o Swift.
// Para o Cocoapods, seria `cocoapods.FirebaseAISwiftWrapper`. Para SPM, geralmente é o nome do target/módulo.
// Frequentemente, para código Swift dentro do seu próprio app target (como `iosApp`),
// as classes Swift são acessíveis sem um prefixo de módulo explícito se o framework KMP
// for corretamente vinculado e os nomes não conflitarem.
// Vamos tentar sem prefixo de módulo por enquanto, e ajustar se necessário.
// import FirebaseAISwiftWrapper // Esta linha pode não ser necessária ou pode precisar ser ajustada.

actual class InsightGenerator actual constructor() {
    // Instancia o wrapper Swift.
    // A classe FirebaseAISwiftWrapper precisa estar acessível aqui.
    // Se houver problemas de visibilidade, pode ser necessário verificar a configuração do build KMP
    // e como o framework gerado expõe/importa símbolos do projeto iOS principal.
    private val swiftWrapper: FirebaseAISwiftWrapper by lazy { FirebaseAISwiftWrapper() }

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

        val prompt = buildFinancialPrompt(transactions)

        // Usar Dispatchers.Default para operações de CPU-bound ou chamadas que bloqueiam no contexto nativo.
        // Para chamadas de rede/IO via interop que são assíncronas (como nosso wrapper Swift),
        // o dispatcher principal da coroutine que chama esta função pode ser suficiente,
        // mas withContext(Dispatchers.Default) é uma escolha segura para garantir que não bloqueie o chamador.
        return withContext(Dispatchers.Default) {
            suspendCancellableCoroutine { continuation ->
                swiftWrapper.generateInsights(prompt) { result, error ->
                    if (error != null) {
                        NSLog("IOSFirebaseGeminiService Error from Swift: ${error.localizedDescription}")
                        continuation.resumeWithException(Exception("Error from Swift: ${error.localizedDescription} (Code: ${error.code})"))
                    } else if (result != null) {
                        try {
                            val insights = parseInsights(result)
                            continuation.resume(insights)
                        } catch (e: Exception) {
                            NSLog("IOSFirebaseGeminiService Parsing Error: ${e.message}")
                            continuation.resumeWithException(e)
                        }
                    } else {
                        // Este caso não deveria acontecer se o callback Swift sempre retornar resultado ou erro.
                        continuation.resumeWithException(Exception("Unknown error or null result from FirebaseAISwiftWrapper"))
                    }
                }
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
                "${dateTime.month.name.uppercase()} ${dateTime.year}" // Use uppercase for month name consistency if needed
            }
            .toList()
            .sortedByDescending { (monthYear, _) ->
                val parts: List<String> = monthYear.split(" ")
                if (parts.size == 2) {
                    try {
                        // Ensure month name matches enum value (e.g. JANUARY, FEBRUARY)
                        val month = Month.valueOf(parts[0].uppercase())
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

        fun formatCurrency(value: Double): String {
            val roundedValue = (kotlin.math.round(value * 100) / 100.0)
            // Basic formatting, ensure it matches R$ xxx.xx or similar as expected by the prompt
            val s = roundedValue.toString()
            val parts = s.split('.')
            val integerPart = parts[0]
            val decimalPart = if (parts.size > 1) parts[1].padEnd(2, '0') else "00"
            return "R$ $integerPart.$decimalPart"
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
            ${monthlyData.map { (monthYear, monthTransactions) ->
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

    private fun parseInsights(text: String): List<Insight> {
         return try {
            // Tentativa de limpar um pouco mais o JSON, caso haja prefixos/sufixos inesperados
            val actualJsonText = if (text.trimStart().startsWith("[")) text.trim() else text.substringAfter("[").substringBeforeLast("]") + "]"

            // Split a string of JSON objects into individual JSON object strings
            // This assumes objects are separated by "},{"
            // More robust parsing would involve a JSON library that can handle an array of objects directly
            val objectsStr = actualJsonText.removePrefix("[").removeSuffix("]")

            if (objectsStr.isBlank()) return emptyList()

            objectsStr.split("},{").map { objStr ->
                val singleJson = if (objectsStr.contains("},{")) "{${objStr}}" else objStr // Re-add braces if split

                // Parse each field manually. Consider using kotlinx.serialization for robust parsing.
                val title = singleJson.substringAfter("\"title\": \"", "").substringBefore("\"", "")
                val description = singleJson.substringAfter("\"description\": \"", "").substringBefore("\"", "")
                val recommendation = singleJson.substringAfter("\"recommendation\": \"", "").substringBefore("\"", "")

                if (title.isEmpty() && description.isEmpty() && recommendation.isEmpty()) {
                    throw Exception("Failed to parse insight object: $singleJson")
                }
                Insight(title, description, recommendation)
            }
        } catch (e: Exception) {
            NSLog("IOSFirebaseGeminiService: Error parsing insights from text: '$text' - Error: ${e.message}")
            // Fallback if parsing fails
             listOf(
                Insight(
                    title = "Erro ao Processar Insights",
                    description = "Não foi possível processar a resposta do servidor. Conteúdo: ${text.take(100)}...",
                    recommendation = "Tente novamente mais tarde ou verifique o formato da resposta."
                )
            )
        }
    }

    private fun parseDate(dateStr: String): LocalDateTime {
        return try {
            Instant.parse(dateStr).toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            NSLog("IOSFirebaseGeminiService: Error parsing date '$dateStr': ${e.message}")
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}

internal actual object GeminiServiceFactory {
    private val instance: InsightGenerator by lazy { InsightGenerator() }
    actual fun getInstance(): InsightGenerator = instance
}

actual fun initializeGeminiService() {
    // A inicialização do wrapper Swift e do Firebase (via FirebaseApp.configure)
    // deve ocorrer no lado Swift. Esta função Kotlin pode ser usada para
    // inicializar ansiosamente a instância Kotlin, se necessário, ou apenas para log.
    GeminiServiceFactory.getInstance() // Isso irá inicializar o swiftWrapper na primeira chamada.
    NSLog("IOSFirebaseGeminiService (Kotlin actual): Initialized and ready to use Swift wrapper.")
}
