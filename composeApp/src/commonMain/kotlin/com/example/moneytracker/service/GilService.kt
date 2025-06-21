package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.util.toCurrencyString
import kotlinx.datetime.LocalDate

data class UserFinancialContext(
    val transactions: List<Transaction>,
    val budgets: List<Budget>,
    val totalIncome: Double,
    val totalExpenses: Double,
    val monthlyBudget: Double,
    val topExpenseCategories: List<Pair<TransactionCategory, Double>>,
    val currentDate: LocalDate
)

abstract class GilService {
    abstract suspend fun chat(message: String, context: UserFinancialContext? = null): String

    protected fun buildPrompt(message: String, context: UserFinancialContext? = null): String {
        val basePrompt = """
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
        """.trimIndent()

        val contextInfo = if (context != null) {
            """
            
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
            """.trimIndent()
        } else {
            ""
        }

        return """
            $basePrompt
            
            $contextInfo
            
            Pergunta do usuário: $message
            
            Lembre-se: Mantenha a resposta curta, prática e amigável, e use as informações financeiras do usuário quando relevante.
        """.trimIndent()
    }

    private fun formatMoney(value: Double): String {
        return value.toCurrencyString()
    }
}

internal object GilServiceFactory {
    private var instance: GilService? = null

    fun setInstance(service: GilService) {
        instance = service
    }

    fun getInstance(): GilService {
        return instance ?: object : GilService() {
            override suspend fun chat(message: String, context: UserFinancialContext?): String {
                return "Desculpe, o Gil não está disponível nesta plataforma."
            }
        }.also { instance = it }
    }
} 