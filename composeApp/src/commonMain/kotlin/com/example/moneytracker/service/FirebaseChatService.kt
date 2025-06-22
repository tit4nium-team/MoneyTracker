package com.example.moneytracker.service

import com.google.firebase.vertexai.FirebaseVertexAI
import com.google.firebase.vertexai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.moneytracker.util.toCurrencyString // Importar a função de formatação
import com.example.moneytracker.model.UserFinancialContext // Import atualizado

object FirebaseChatService {

    // Configuração do modelo generativo para chat
    // Usar "gemini-1.5-flash" ou outro modelo adequado para chat.
    private val generativeModel = FirebaseVertexAI.getInstance()
        .generativeModel(
            modelName = "gemini-1.5-flash"
            // Para chat, geralmente não se força responseMimeType para JSON, a menos que a resposta seja estruturada.
        )

    suspend fun chat(message: String, context: UserFinancialContext? = null, isFirstInteraction: Boolean = true): String {
        return withContext(Dispatchers.IO) {
            try {
                val fullPrompt = buildChatPrompt(message, context, isFirstInteraction)
                println("FirebaseChatService: Enviando prompt de chat: $fullPrompt")

                val response = generativeModel.generateContent(fullPrompt)

                val responseText = response.text
                if (responseText == null) {
                    println("FirebaseChatService: Resposta vazia do modelo de chat.")
                    "Desculpe, não consegui processar sua pergunta no momento (resposta vazia)."
                } else {
                    println("FirebaseChatService: Resposta do chat recebida: $responseText")
                    responseText
                }

            } catch (e: Exception) {
                println("FirebaseChatService: Erro ao interagir com o chat: ${e.message}")
                e.printStackTrace()
                "Desculpe, estou com dificuldades técnicas no momento. Tente novamente mais tarde. (Erro: ${e.message})"
            }
        }
    }

    // Esta função de construção de prompt é baseada na lógica de GilService.kt (common) e AndroidGilService.kt (android)
    private fun buildChatPrompt(message: String, context: UserFinancialContext?, isFirstInteraction: Boolean): String {
        // Instruções base do prompt para o Gil
        val baseInstructions = """
            Você é o Gil, um assistente financeiro amigável e profissional. Você deve:

            1. Manter um tom amigável mas profissional.
            2. Focar em dar conselhos práticos e diretos sobre finanças pessoais.
            3. Usar linguagem simples e acessível.
            4. Manter respostas concisas (máximo 3-4 frases, a menos que mais detalhes sejam solicitados).
            5. Sempre considerar o contexto brasileiro (moeda Real R$, práticas financeiras comuns no Brasil).
            6. Evitar termos técnicos demais, preferindo explicações práticas.
            7. Ser encorajador e positivo, mas realista.
            8. Se não for a primeira interação, NÃO se apresente novamente. Vá direto para a resposta.
            9. Mantenha suas respostas diretas e objetivas, sem formalidades desnecessárias.
            10. Use as informações financeiras do usuário para dar respostas mais personalizadas, quando disponíveis e relevantes.
            11. Sempre formate valores monetários no formato brasileiro (Ex: R$ 1.234,56).
        """.trimIndent()

        // Adiciona o contexto financeiro do usuário se disponível
        val contextInfo = if (context != null) {
            """

            Aqui estão algumas informações financeiras sobre o usuário para sua referência (use-as para personalizar a resposta, se aplicável):
            - Renda total informada: R$ ${formatMoney(context.totalIncome)}
            - Despesas totais informadas: R$ ${formatMoney(context.totalExpenses)}
            - Orçamento mensal total (soma dos orçamentos por categoria): R$ ${formatMoney(context.monthlyBudget)}

            Principais categorias de despesa (se houver):
            ${context.topExpenseCategories.joinToString("\n") { (category, value) ->
                "- ${category.name}: R$ ${formatMoney(value)}"
            }.ifEmpty { "- Nenhuma categoria de despesa principal registrada." }}

            Orçamentos definidos por categoria (se houver):
            ${context.budgets.joinToString("\n") { budget ->
                "- ${budget.category.name}: Limite de R$ ${formatMoney(budget.amount)} por mês."
            }.ifEmpty { "- Nenhum orçamento por categoria definido." }}

            Número de transações financeiras registradas pelo usuário: ${context.transactions.size}
            Data atual para referência: ${context.currentDate}
            """.trimIndent()
        } else {
            "\nO usuário não forneceu informações financeiras contextuais neste momento.\n"
        }

        // Lógica para apresentação inicial vs. continuação
        val userQuery: String
        if (isFirstInteraction) {
            userQuery = "Pergunta do usuário: $message"
        } else {
            // Instrução para não se apresentar de novo e continuar a conversa
            userQuery = "Lembre-se de não se apresentar novamente, apenas responda à pergunta. Pergunta do usuário: $message"
        }

        return """
            $baseInstructions
            $contextInfo

            $userQuery

            Sua resposta (Gil):
        """.trimIndent()
    }

    // Função auxiliar para formatação de moeda (baseada na que estava em GilService)
    // Certifique-se que a extensão toCurrencyString() já faz o replace(".", ",") se necessário.
    // A toCurrencyString() do projeto já deve lidar com isso.
    private fun formatMoney(value: Double): String {
        return value.toCurrencyString()
    }
}
