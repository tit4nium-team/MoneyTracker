package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction // Manter por enquanto
import com.example.moneytracker.model.Insight // Manter por enquanto
// Removido: import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.* // Manter para parseDate e manipulação de datas
import kotlin.math.abs
import android.util.Log
// Removido: import com.example.moneytracker.config.ApiConfig // ApiConfig foi deletado
// Removido: import com.example.moneytracker.BuildConfig // Se ApiConfig usava, pode não ser mais necessário aqui diretamente

// Imports do Firebase Vertex AI SDK serão adicionados aqui quando implementarmos
// import com.google.firebase.vertexai.FirebaseVertexAI
// import com.google.firebase.vertexai.type.GenerateContentResponse ---- (ou o tipo de resposta correto)


// Esta classe será refatorada para ser o 'actual class AndroidGeminiManager'
// e usará o SDK Firebase AI para Android (FirebaseVertexAI).
internal class AndroidGeminiManagerPlaceholder { // Renomeado temporariamente de AndroidGeminiService

    // private var generativeModel: com.google.firebase.vertexai.GenerativeModel? = null // Exemplo de como seria com Firebase AI

    // A lógica de 'availableModels' e 'tryNextModel' pode mudar ou ser simplificada
    // dependendo de como o Firebase AI SDK lida com a seleção de modelos e retries.

    suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
        if (transactions.isEmpty()) {
            // Esta lógica de boas-vindas pode ser mantida ou movida para commonMain se for genérica
            return listOf(
                Insight(
                    title = "Bem-vindo ao Money Tracker",
                    description = "Adicione suas primeiras transações para receber insights personalizados.",
                    recommendation = "Comece registrando suas despesas diárias."
                )
            )
        }

        return withContext(Dispatchers.IO) { // Ou Dispatchers.Default, dependendo da natureza da chamada do Firebase AI
            // TODO: Implementar lógica com Firebase AI SDK
            // 1. Inicializar FirebaseVertexAI (pode ser feito uma vez e reutilizado)
            //    val vertexAI = Firebase.vertexAI
            //    if (generativeModel == null) {
            //        generativeModel = vertexAI.generativeModel(modelName = "gemini-1.5-flash") // ou outro modelo
            //    }
            // 2. Construir o prompt (a função buildFinancialPrompt pode ser reutilizada ou adaptada)
            //    val promptText = buildFinancialPrompt(transactions)
            // 3. Chamar o modelo:
            //    try {
            //        val response = generativeModel!!.generateContent(promptText)
            //        val insightsJson = response.text ?: ""
            //        parseInsights(insightsJson) // A função parseInsights pode ser reutilizada
            //    } catch (e: Exception) {
            //        Log.e("AndroidGeminiManager", "Erro ao gerar insights com Firebase AI: ${e.message}", e)
            //        // Retornar um insight de erro padrão
            //        listOf(
            //            Insight(
            //                title = "Erro ao Gerar Insights",
            //                description = "Não foi possível conectar ao serviço de IA.",
            //                recommendation = "Verifique sua conexão ou tente mais tarde."
            //            )
            //        )
            //    }

            // Placeholder enquanto a implementação real não está pronta:
            Log.d("AndroidGeminiManager", "generateFinancialInsights chamado, implementação pendente.")
            listOf(
                Insight(
                    title = "Android Insights (Placeholder)",
                    description = "Implementação pendente com Firebase AI SDK.",
                    recommendation = "Verifique a configuração do Firebase para Android."
                )
            )
        }
    }

    // A função buildFinancialPrompt pode ser movida para commonMain ou para uma classe utilitária
    // se a lógica de construção do prompt for a mesma para ambas as plataformas.
    // Por enquanto, ela pode permanecer aqui e ser adaptada.
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
                // Usar o número do mês para evitar problemas com nomes de meses em diferentes locales no prompt
                // Formato MM-YYYY para consistência
                "${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.year}"
            }
            .toList()
            .sortedByDescending { (monthYear, _) ->
                val (month, year) = monthYear.split("-")
                year.toInt() * 100 + month.toInt()
            }
            .map { (monthYear, trans) -> // Remapear para nome do mês se desejado para o prompt final
                 val (monthNum, year) = monthYear.split("-")
                 val monthName = Month(monthNum.toInt()).name // Requer que Month.name seja o que você espera
                 val monthlyExpenses = trans.filter { it.amount < 0 }.sumOf { abs(it.amount) }
                "- $monthName $year: R$ ${String.format("%.2f", monthlyExpenses)}"
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
            ${monthlyData.joinToString("\n")}
            
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

    // A função parseInsights também pode ser movida para commonMain ou utilitário.
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
            Log.e("AndroidGeminiManager", "Erro ao parsear insights: ${e.message}", e)
            listOf(
                Insight(
                    title = "Erro de Análise",
                    description = "Não foi possível processar a resposta do serviço de IA.",
                    recommendation = "Tente novamente mais tarde."
                )
            )
        }
    }

    // A função parseDate provavelmente precisará de uma solução KMP ou específica da plataforma
    // se o formato "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy" for estritamente necessário e vier de uma fonte externa.
    // Se `transaction.date` for um timestamp Long ou um formato ISO, a conversão é mais fácil em KMP.
    // Por agora, mantendo a implementação Android original que usa SimpleDateFormat.
    private fun parseDate(dateStr: String): LocalDateTime {
        return try {
            // Esta implementação é específica do Android (java.text.SimpleDateFormat)
            // Precisará de uma alternativa KMP se esta função for para commonMain.
            val pattern = "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy"
            val formatter = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
            val parsedDate = formatter.parse(dateStr)
            val instant = Instant.fromEpochMilliseconds(parsedDate.time)
            instant.toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            Log.w("AndroidGeminiManager", "Erro ao parsear data '$dateStr', usando data atual como fallback: ${e.message}")
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}

// Esta função será refatorada para instanciar o novo AndroidGeminiManager
// e se adequar à nova estrutura com PlatformGeminiManager.
internal actual fun initializeGeminiService() {
    val androidManager = AndroidGeminiManagerPlaceholder() // Será o novo manager
    GeminiServiceFactory.setInstance(object : InsightGenerator() {
        override suspend fun generateFinancialInsights(transactions: List<Transaction>): List<Insight> {
            // Esta chamada será para o novo AndroidGeminiManager
            return androidManager.generateFinancialInsights(transactions)
        }
    })
}