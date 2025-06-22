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

import com.google.firebase.vertexai.type.generationConfig // Para configurações de geração
import com.google.firebase.vertexai.type.safetySetting // Para configurações de segurança
import com.google.firebase.vertexai.type.HarmCategory
import com.google.firebase.vertexai.type.BlockThreshold

// Assumindo que UserFinancialContext, GilServiceFactory e GilService (classe base/interface) ainda existem.
// Se UserFinancialContext ou GilService vêm de commonMain, eles devem estar lá.

internal class AndroidGilService {
    
    private val generativeModel: GenerativeModel by lazy {
        try {
            // Você pode querer configurações diferentes para o Gil
            val safetySettings = listOf(
                safetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
                // Adicione outras configurações de segurança conforme necessário
            )
            val generationConfig = generationConfig {
                temperature = 0.8f // Um pouco mais criativo para chat?
                // Outras configs
            }

            Log.d("GilService", "Inicializando modelo Firebase AI para GilService...")
            Firebase.vertexAI.generativeModel(
                modelName = "gemini-1.5-flash", // Ou um modelo específico para chat se disponível/preferido
                safetySettings = safetySettings,
                generationConfig = generationConfig
            ).also {
                Log.d("GilService", "Modelo Firebase AI para GilService inicializado.")
            }
        } catch (e: Exception) {
            Log.e("GilService", "CRITICAL: Erro ao inicializar GenerativeModel para GilService: ${e.message}", e)
            throw IllegalStateException("Falha ao inicializar o modelo de IA para GilService", e)
        }
    }

    suspend fun chat(message: String, context: UserFinancialContext? = null): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(message, context)
                Log.i("GilService", "Enviando prompt para o Gil (Firebase AI)...")
                val response = generativeModel.generateContent(prompt)
                response.text ?: "Desculpe, não consegui processar sua pergunta no momento."
            } catch (e: Exception) {
                Log.e("GilService", "Erro ao chamar generateContent para GilService: ${e.message}", e)
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