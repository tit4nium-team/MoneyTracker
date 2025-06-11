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
    private var userId: String = "default_user",
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    private var transactionsJob: Job? = null

    init {
        observeTransactions()
    }

    fun setUserId(newUserId: String) {
        userId = newUserId
        observeTransactions()
    }

    private fun observeTransactions() {
        transactionsJob?.cancel()
        transactionsJob = scope.launch {
            try {
                repository.getTransactionsFlow(userId).collect { transactions ->
                    updateState(transactions)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun addTransaction(
        type: TransactionType,
        amount: Double,
        category: TransactionCategory,
        description: String = ""
    ) {
        val transaction = Transaction(
            type = type,
            amount = amount,
            category = category,
            description = description,
            date = "",
            userId = userId
        )
        
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.addTransaction(transaction)
                .collect { result ->
                    result
                        .onSuccess {
                            // The observeTransactions Flow will automatically update the list
                            // since we're already listening to changes
                            _state.update { it.copy(isLoading = false) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(error = error.message, isLoading = false) }
                        }
                }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.updateTransaction(transaction)
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun deleteTransaction(transactionId: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.deleteTransaction(transactionId)
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun updateState(transactions: List<Transaction>) {
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
            
        val totalExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
            
        _state.value = TransactionState(
            transactions = transactions,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            balance = totalIncome - totalExpenses
        )
    }

    fun filterByCategory(category: TransactionCategory) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getTransactionsByCategory(userId, category)
                .onSuccess { transactions: List<Transaction> ->
                    updateState(transactions)
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun filterByDateRange(startDate: String, endDate: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getTransactionsByDateRange(userId, startDate, endDate)
                .onSuccess { transactions: List<Transaction> ->
                    updateState(transactions)
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
} 