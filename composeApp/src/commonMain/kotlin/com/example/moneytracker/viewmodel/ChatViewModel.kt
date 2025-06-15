package com.example.moneytracker.viewmodel

import com.example.moneytracker.model.ChatMessage
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.service.GilService
import com.example.moneytracker.service.GilServiceFactory
import com.example.moneytracker.service.UserFinancialContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class ChatState(
    val messages: List<ChatMessage> = listOf(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFirstInteraction: Boolean = true
)

class ChatViewModel(
    private val transactionViewModel: TransactionViewModel,
    private val budgetViewModel: BudgetViewModel,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        startNewChat()
    }

    fun startNewChat() {
        // Mantém o histórico mas marca como nova interação
        _state.update { currentState ->
            currentState.copy(
                isFirstInteraction = true,
                error = null
            )
        }

        // Adiciona mensagem de boas-vindas apenas se não houver mensagens
        if (_state.value.messages.isEmpty()) {
            addMessage(
                ChatMessage(
                    id = "welcome",
                    content = "Olá! Eu sou o Gil, seu assistente financeiro. Como posso ajudar você hoje?",
                    isFromUser = false,
                    timestamp = getCurrentDateTime()
                )
            )
        }
    }

    private fun buildFinancialContext(): UserFinancialContext {
        val transactions = transactionViewModel.state.value.transactions
        val budgets = budgetViewModel.state.value.budgets

        // Calcula totais
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        // Calcula orçamento mensal (soma de todos os orçamentos)
        val monthlyBudget = budgets.sumOf { it.amount }

        // Calcula top categorias de despesa
        val expensesByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { (_, total) -> total }
            .take(5)

        return UserFinancialContext(
            transactions = transactions,
            budgets = budgets,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            monthlyBudget = monthlyBudget,
            topExpenseCategories = expensesByCategory,
            currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val isFirstInteraction = _state.value.isFirstInteraction

        // Adiciona mensagem do usuário
        addMessage(
            ChatMessage(
                id = "user_${Clock.System.now().toEpochMilliseconds()}",
                content = content,
                isFromUser = true,
                timestamp = getCurrentDateTime()
            )
        )

        // Inicia processamento da resposta
        _state.update { it.copy(isLoading = true) }

        scope.launch {
            try {
                // Modifica o prompt baseado se é primeira interação ou não
                val prompt = if (isFirstInteraction) {
                    content
                } else {
                    "Continue a conversa normalmente, sem se apresentar novamente. Pergunta do usuário: $content"
                }

                val context = buildFinancialContext()
                val response = GilServiceFactory.getInstance().chat(prompt, context)
                
                addMessage(
                    ChatMessage(
                        id = "gil_${Clock.System.now().toEpochMilliseconds()}",
                        content = response,
                        isFromUser = false,
                        timestamp = getCurrentDateTime()
                    )
                )
                _state.update { it.copy(
                    isLoading = false, 
                    error = null,
                    isFirstInteraction = false
                ) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Não foi possível processar sua mensagem. Tente novamente."
                    )
                }
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _state.update { currentState ->
            currentState.copy(
                messages = currentState.messages + message
            )
        }
    }

    private fun getCurrentDateTime(): String {
        return Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
    }
} 