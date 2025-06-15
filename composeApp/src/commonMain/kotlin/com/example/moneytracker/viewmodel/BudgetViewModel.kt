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
import java.text.SimpleDateFormat
import java.util.Locale

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
        } catch (e: Exception) {
            try {
                // Se falhar, tenta converter do formato do Firebase
                val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
                val date = formatter.parse(dateString)
                val instant = Instant.fromEpochMilliseconds(date.time)
                instant.toLocalDateTime(TimeZone.currentSystemDefault())
            } catch (e: Exception) {
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

    fun createBudget(category: TransactionCategory, amount: Double, month: Int, year: Int) {
        viewModelScope.launch {
            val budget = Budget(
                userId = userId,
                category = category,
                amount = amount,
                month = month,
                year = year
            )
            repository.createBudget(budget)
        }
    }

    fun updateBudget(budget: Budget, newAmount: Double) {
        viewModelScope.launch {
            repository.updateBudget(budget.copy(amount = newAmount))
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            repository.deleteBudget(budgetId)
        }
    }

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals

    private val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun loadSavingsGoals() {
        userId?.let { uid ->
            scope.launch {
                repository.getSavingsGoalsFlow(uid).collect { goals ->
                    _savingsGoals.value = goals
                }
            }
        }
    }

    fun addBudget(category: TransactionCategory, amount: Double): Flow<Result<Unit>> {
        val budget = Budget(category = category, amount = amount, month = currentDate.monthNumber, year = currentDate.year)
        return repository.addBudget(userId ?: return MutableStateFlow(Result.failure(IllegalStateException("User not logged in"))), budget)
    }

    fun addSavingsGoal(name: String, targetAmount: Double): Flow<Result<Unit>> {
        val goal = SavingsGoal(name = name, targetAmount = targetAmount, currentAmount = 0.0)
        return repository.addSavingsGoal(userId ?: return MutableStateFlow(Result.failure(IllegalStateException("User not logged in"))), goal)
    }

    fun updateSavingsGoal(name: String, targetAmount: Double, currentAmount: Double) {
        userId?.let { uid ->
            scope.launch {
                val goal = SavingsGoal(name = name, targetAmount = targetAmount, currentAmount = currentAmount)
                repository.updateSavingsGoal(uid, goal)
            }
        }
    }

    fun deleteSavingsGoal(goalName: String) {
        userId?.let { uid ->
            scope.launch {
                repository.deleteSavingsGoal(uid, goalName)
            }
        }
    }
}

data class BudgetState(
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) 