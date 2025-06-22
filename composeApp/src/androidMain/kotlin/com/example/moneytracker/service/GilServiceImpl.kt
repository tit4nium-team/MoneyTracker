package com.example.moneytracker.service

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.vertexai.GenerativeModel // Import do Firebase AI
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.generativeModel // Função de extensão
// Potencialmente importar configs de safety/generation se for usar diferente do default
// import com.google.firebase.vertexai.type.generationConfig
// import com.google.firebase.vertexai.type.safetySetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Assumindo que UserFinancialContext, GilServiceFactory e GilService (classe base/interface) ainda existem.
// Se UserFinancialContext ou GilService vêm de commonMain, eles devem estar lá.

// Imports para o SDK base do Google AI
// com.google.ai.client.generativeai.GenerativeModel já está importado
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.example.moneytracker.config.ApiConfig // Para a chave de API

// Assumindo que UserFinancialContext, GilServiceFactory e GilService (classe base/interface) ainda existem.

internal class AndroidGilService {
    
    private var generativeModel: GenerativeModel? = null
    private val modelName = "gemini-1.5-flash" // Ou um modelo específico para chat, ex: "gemini-pro"

    init {
        initializeModel()
    }

    private fun initializeModel() {
        try {
            Log.d("GilService", "Inicializando GenerativeModel para Gil com Google AI SDK...")
            // Configurações específicas para o Gil, se necessário
            val safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.BLOCK_ONLY_HIGH),
                // Adicione outras conforme a personalidade/função do Gil
            )
            val config = generationConfig {
                temperature = 0.8f // Mais criativo para um chatbot
                // Outras configurações
            }
            generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = ApiConfig.GEMINI_API_KEY, // Usando a mesma chave, pode ser diferente se necessário
                safetySettings = safetySettings,
                generationConfig = config
            )
            Log.d("GilService", "GenerativeModel para Gil inicializado com sucesso.")
        } catch (e: Exception) {
            Log.e("GilService", "Erro ao inicializar GenerativeModel para Gil: ${e.message}", e)
        }
    }

    suspend fun chat(message: String, context: UserFinancialContext? = null): String {
        val currentModel = generativeModel
        if (currentModel == null) {
            Log.e("GilService", "GenerativeModel para Gil não inicializado.")
            return "Desculpe, estou com um problema interno e não consigo conversar agora."
        }

        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(message, context)
                Log.i("GilService", "Enviando prompt para o Gil (Google AI SDK)...")
                val response = currentModel.generateContent(prompt)
                response.text ?: "Desculpe, não consegui processar sua pergunta no momento."
            } catch (e: Exception) {
                Log.e("GilService", "Erro ao chamar generateContent para Gil (Google AI SDK): ${e.message}", e)
                "Desculpe, estou com dificuldades técnicas no momento. Tente novamente mais tarde."
            }
        }
    }

    // buildPrompt e formatMoney podem permanecer os mesmos se a lógica de formatação do prompt for a mesma.
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