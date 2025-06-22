package com.example.moneytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.data.BudgetRepository
import com.example.moneytracker.data.CategoryRepository
import com.example.moneytracker.data.TransactionRepository
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
// Removed java.text.SimpleDateFormat and java.util.Locale
// DateTimeUtil will be used for platform-specific formatting if needed for parsing,
// but kotlinx-datetime is preferred for internal logic.
import com.example.moneytracker.util.DateTimeUtil // Added if direct formatting is still needed

class BudgetViewModel(
    private val repository: BudgetRepository = RepositoryProvider.provideBudgetRepository(),
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    private val _categories = MutableStateFlow<List<TransactionCategory>>(emptyList())
    val categories: StateFlow<List<TransactionCategory>> = _categories.asStateFlow()

    private var userId: String = ""

    fun setUserId(id: String) {
        userId = id
        loadCategories()
        loadBudgets(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month.ordinal,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        )
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategoriesFlow(userId).collect { categories ->
                _categories.value = categories
            }
        }
    }

    private fun parseTransactionDate(dateString: String): LocalDateTime? {
        return try {
            // Primeiro tenta o formato ISO
            LocalDateTime.parse(dateString)
        } catch (e1: Exception) {
            try {
                // Tenta usar o DateTimeUtil para o formato legado "EEE MMM dd..."
                // Esta parte é mais complexa porque DateTimeUtil.formatDateForDashboard retorna String,
                // e precisamos de LocalDateTime. O ideal é que a data já venha como Long ou ISO.
                // Por agora, se não for ISO, vamos considerar como não parseável aqui
                // e a lógica de filtragem precisará ser robusta.
                // Uma solução temporária, se o formato legado for comum e precisar ser parseado para LocalDateTime:
                // 1. DateTimeUtil teria que expor um parseToEpochMillis (actual/expect)
                // 2. Then Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
                // Para simplificar agora, se não for ISO, retornamos null.
                // A conversão de "EEE MMM dd..." para LocalDateTime em commonMain é não trivial sem expect/actual para parsing.
                null // Simplificado: se não for ISO, não conseguimos parsear diretamente aqui.
            } catch (e2: Exception) {
                null
            }
        }
    }

    fun loadBudgets(month: Int, year: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            combine(
                repository.getBudgets(userId, month, year),
                transactionRepository.getTransactionsFlow(userId)
            ) { budgets, transactions ->
                budgets.map { budget ->
                    val spent = transactions
                        .filter { transaction ->
                            val transactionDate = parseTransactionDate(transaction.date)
                            transactionDate?.let { date ->
                                transaction.category.id == budget.category.id &&
                                transaction.type == TransactionType.EXPENSE &&
                                date.month.ordinal == month &&
                                date.year == year
                            } ?: false
                        }
                        .sumOf { it.amount }
                    
                    budget.copy(spent = spent)
                }
            }.collect { budgetsWithSpent ->
                _state.value = _state.value.copy(
                    budgets = budgetsWithSpent,
                    isLoading = false
                )
            }
        }
    }

    fun createBudget(
        category: TransactionCategory,
        amount: Double,
        month: Int,
        year: Int,
        replicateForAllMonths: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                if (replicateForAllMonths) {
                    // Criar orçamentos para todos os meses do ano
                    (0..11).forEach { monthIndex ->
                        val budget = Budget(
                            userId = userId,
                            category = category,
                            amount = amount,
                            month = monthIndex,
                            year = year
                        )
                        repository.createBudget(budget) // Pode lançar no iOS
                    }
                } else {
                    // Criar orçamento apenas para o mês selecionado
                    val budget = Budget(
                        userId = userId,
                        category = category,
                        amount = amount,
                        month = month,
                        year = year
                    )
                    repository.createBudget(budget) // Pode lançar no iOS
                }
                // Recarrega os orçamentos do mês atual após criar (se não houve exceção)
                loadBudgets(month, year)
            } catch (e: Exception) {
                println("Error in BudgetViewModel.createBudget: ${e.message}")
                _state.update { it.copy(isLoading = false, error = e.message ?: "Erro ao criar orçamento") }
            }
        }
    }

    fun updateBudget(budget: Budget, newAmount: Double) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                repository.updateBudget(budget.copy(amount = newAmount)) // Pode lançar no iOS
                 // Recarregar orçamentos se necessário, e se a operação for bem-sucedida
                loadBudgets(budget.month, budget.year)
            } catch (e: Exception) {
                println("Error in BudgetViewModel.updateBudget: ${e.message}")
                _state.update { it.copy(isLoading = false, error = e.message ?: "Erro ao atualizar orçamento") }
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        // Precisamos saber o mês/ano para recarregar corretamente, ou recarregar uma visão geral.
        // Por simplicidade, vamos assumir que a UI pode lidar com a lista sendo atualizada por loadBudgets.
        // Se o budgetId não for suficiente para saber qual mês/ano recarregar,
        // pode ser necessário buscar o budget primeiro ou ter mais info.
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                repository.deleteBudget(budgetId) // Pode lançar no iOS
                // Recarregar orçamentos. Idealmente, saber qual mês/ano.
                // Usando o mês/ano atual do estado como fallback.
                val currentMonth = _state.value.budgets.firstOrNull()?.month
                                   ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month.ordinal
                val currentYear = _state.value.budgets.firstOrNull()?.year
                                  ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
                loadBudgets(currentMonth, currentYear)
            } catch (e: Exception) {
                println("Error in BudgetViewModel.deleteBudget: ${e.message}")
                _state.update { it.copy(isLoading = false, error = e.message ?: "Erro ao deletar orçamento") }
            }
        }
    }

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals

    private val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun loadSavingsGoals() {
        userId?.let { uid ->
            scope.launch {
                try {
                    _state.update { it.copy(error = null) } // Limpa erro anterior se houver
                    // No iOS, a dummy retornará emptyFlow(), então _savingsGoals será uma lista vazia.
                    repository.getSavingsGoalsFlow(uid).collect { goals ->
                        _savingsGoals.value = goals
                    }
                } catch (e: Exception) { // Improvável para emptyFlow, mas bom ter.
                    println("Error in BudgetViewModel.loadSavingsGoals: ${e.message}")
                    _state.update { it.copy(error = e.message ?: "Erro ao carregar metas de economia") }
                }
            }
        }
    }

    fun addBudget(category: TransactionCategory, amount: Double): Flow<Result<Unit>> {
        val budget = Budget(category = category, amount = amount, month = currentDate.monthNumber, year = currentDate.year)
        // A dummy já retorna Flow<Result.failure>
        if (this.userId.isBlank()) return MutableStateFlow(Result.failure(IllegalStateException("User not logged in")))
        return repository.addBudget(this.userId, budget)
    }

    fun addSavingsGoal(name: String, targetAmount: Double): Flow<Result<Unit>> {
        val goal = SavingsGoal(name = name, targetAmount = targetAmount, currentAmount = 0.0)
        // A dummy já retorna Flow<Result.failure>
        if (this.userId.isBlank()) return MutableStateFlow(Result.failure(IllegalStateException("User not logged in")))
        return repository.addSavingsGoal(this.userId, goal)
    }

    fun updateSavingsGoal(name: String, targetAmount: Double, currentAmount: Double) {
        userId?.let { uid ->
            scope.launch {
                try {
                    _state.update { it.copy(isLoading = true, error = null) }
                    val goal = SavingsGoal(name = name, targetAmount = targetAmount, currentAmount = currentAmount)
                    repository.updateSavingsGoal(uid, goal) // Pode lançar no iOS
                    loadSavingsGoals() // Recarrega se bem-sucedido
                } catch (e: Exception) {
                    println("Error in BudgetViewModel.updateSavingsGoal: ${e.message}")
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Erro ao atualizar meta de economia") }
                }
            }
        }
    }

    fun deleteSavingsGoal(goalName: String) {
        userId?.let { uid ->
            scope.launch {
                try {
                    _state.update { it.copy(isLoading = true, error = null) }
                    repository.deleteSavingsGoal(uid, goalName) // Pode lançar no iOS
                    loadSavingsGoals() // Recarrega se bem-sucedido
                } catch (e: Exception) {
                    println("Error in BudgetViewModel.deleteSavingsGoal: ${e.message}")
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Erro ao deletar meta de economia") }
                }
            }
        }
    }
}

data class BudgetState(
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) 