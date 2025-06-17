package com.example.moneytracker.service

import android.util.Log
import com.example.moneytracker.config.ApiConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidGilService {
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
        
        Log.i("GilService", "Trying model: $modelName")
        
        return GenerativeModel(
            modelName = modelName,
            apiKey = ApiConfig.GEMINI_API_KEY
        )
    }

    suspend fun chat(message: String, context: UserFinancialContext? = null): String {
        return withContext(Dispatchers.IO) {
            try {
                if (model == null) {
                    model = tryNextModel()
                }
                
                val prompt = buildPrompt(message, context)
                val response = model!!.generateContent(prompt)
                response.text ?: "Desculpe, não consegui processar sua pergunta no momento."
            } catch (e: Exception) {
                Log.e("GilService", "Error with current model: ${e.message}")
                
                try {
                    model = tryNextModel()
                    val prompt = buildPrompt(message, context)
                    val response = model!!.generateContent(prompt)
                    response.text ?: "Desculpe, não consegui processar sua pergunta no momento."
                } catch (e: Exception) {
                    Log.e("GilService", "Error chatting with Gil", e)
                    "Desculpe, estou com dificuldades técnicas no momento. Tente novamente mais tarde."
                }
            }
        }
    }

    private fun buildPrompt(message: String, context: UserFinancialContext? = null): String {
        return """
            Você é o Gil, um assistente financeiro amigável e profissional. Você deve:
            
            1. Manter um tom amigável mas profissional
            2. Focar em dar conselhos práticos e diretos sobre finanças pessoais
            3. Usar linguagem simples e acessível
            4. Manter respostas concisas (máximo 3-4 frases)
            5. Sempre considerar o contexto brasileiro (moeda, práticas financeiras, etc)
            6. Evitar termos técnicos demais, preferindo explicações práticas
            7. Ser encorajador e positivo, mas realista
            8. IMPORTANTE: Não se apresente novamente se a mensagem pedir para continuar a conversa normalmente
            9. IMPORTANTE: Mantenha suas respostas diretas e objetivas, sem formalidades desnecessárias
            10. Use as informações financeiras do usuário para dar respostas mais personalizadas
            11. Sempre formate valores monetários no formato brasileiro (R$ X.XXX,XX)
            
            ${if (context != null) """
            Informações financeiras do usuário:
            - Renda total: R$ ${formatMoney(context.totalIncome)}
            - Despesas totais: R$ ${formatMoney(context.totalExpenses)}
            - Orçamento mensal: R$ ${formatMoney(context.monthlyBudget)}
            
            Principais categorias de despesa:
            ${context.topExpenseCategories.joinToString("\n") { (category, value) ->
                "- ${category.name}: R$ ${formatMoney(value)}"
            }}
            
            Orçamentos definidos:
            ${context.budgets.joinToString("\n") { budget ->
                "- ${budget.category.name}: R$ ${formatMoney(budget.amount)} (Limite mensal)"
            }}
            
            Número de transações registradas: ${context.transactions.size}
            Data atual: ${context.currentDate}
            """ else ""}
            
            Pergunta do usuário: $message
            
            Lembre-se: Mantenha a resposta curta, prática e amigável, e use as informações financeiras do usuário quando relevante.
        """.trimIndent()
    }

    private fun formatMoney(value: Double): String {
        return "%.2f".format(value).replace(".", ",")
    }
}

internal fun initializeGilService() {
    val androidService = AndroidGilService()
    GilServiceFactory.setInstance(object : GilService() {
        override suspend fun chat(message: String, context: UserFinancialContext?): String {
            return androidService.chat(message, context)
        }
    })
} 