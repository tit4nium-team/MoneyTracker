package com.example.moneytracker.viewmodel

import com.example.moneytracker.data.TransactionRepository
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.moneytracker.util.getCurrentMonthStart
import com.example.moneytracker.util.formatDate

data class TransactionState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val categoryViewModel: CategoryViewModel,
    private val budgetViewModel: BudgetViewModel,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    val categories = categoryViewModel.categories
    val budgets = budgetViewModel.state

    private var transactionsJob: Job? = null
    private var userId: String? = null

    fun setUserId(id: String) {
        userId = id
        categoryViewModel.setUserId(id)
        budgetViewModel.setUserId(id)
        loadTransactions()
    }

    private suspend fun checkBudgetExceeded(
        category: TransactionCategory,
        amount: Double
    ): Boolean {
        val budget = budgets.value.budgets.find { it.category.id == category.id } ?: return false
        val monthStart = getCurrentMonthStart()
        
        val monthlySpent = state.value.transactions
            .filter { transaction ->
                transaction.category.id == category.id &&
                transaction.type == TransactionType.EXPENSE &&
                transaction.date.startsWith(formatDate(monthStart).substring(0, 7))
            }
            .sumOf { it.amount }

        return monthlySpent + amount > budget.amount
    }

    fun addTransaction(
        type: TransactionType,
        amount: Double,
        category: TransactionCategory,
        description: String
    ): Flow<Result<Unit>> = callbackFlow {
        userId?.let { uid ->
            if (type == TransactionType.EXPENSE) {
                val budgetExceeded = checkBudgetExceeded(category, amount)
                if (budgetExceeded) {
                    _state.update { it.copy(error = "Atenção: Esta transação excederá o orçamento da categoria ${category.name}") }
                }
            }

            val transaction = Transaction(
                id = "transaction_${Clock.System.now().toEpochMilliseconds()}",
                amount = amount,
                description = description,
                category = category,
                type = type,
                date = getCurrentDate(),
                userId = uid
            )
            repository.addTransaction(transaction).collect { result ->
                trySend(result)
                if (result.isSuccess) {
                    loadTransactions()
                }
            }
        } ?: trySend(Result.failure(IllegalStateException("User not logged in")))
        
        awaitClose()
    }

    fun deleteTransaction(transactionId: String) {
        scope.launch {
            repository.deleteTransaction(transactionId)
            loadTransactions()
        }
    }

    private fun loadTransactions() {
        transactionsJob?.cancel()
        userId?.let { uid ->
            transactionsJob = scope.launch {
                repository.getTransactionsFlow(uid).collect { transactions ->
                    _state.update { currentState ->
                        val income = transactions
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount }
                        val expenses = transactions
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                        
                        currentState.copy(
                            transactions = transactions,
                            totalIncome = income,
                            totalExpenses = expenses,
                            balance = income - expenses
                        )
                    }
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    }
} 