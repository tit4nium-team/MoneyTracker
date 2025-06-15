package com.example.moneytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytracker.data.BudgetRepository
import com.example.moneytracker.data.CategoryRepository
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class BudgetViewModel(
    private val repository: BudgetRepository = RepositoryProvider.provideBudgetRepository(),
    private val categoryRepository: CategoryRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : ViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals

    private var userId: String = ""
    private val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    private val _categories = MutableStateFlow<List<TransactionCategory>>(emptyList())
    val categories: StateFlow<List<TransactionCategory>> = _categories.asStateFlow()

    fun setUserId(id: String) {
        userId = id
        loadBudgets()
        loadSavingsGoals()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategoriesFlow(userId).collect { categories ->
                _categories.value = categories
            }
        }
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            repository.getBudgets(
                userId = userId,
                month = currentDate.monthNumber,
                year = currentDate.year
            ).collect { budgets ->
                _state.value = _state.value.copy(
                    budgets = budgets,
                    isLoading = false
                )
            }
        }
    }

    private fun loadSavingsGoals() {
        userId?.let { uid ->
            scope.launch {
                repository.getSavingsGoalsFlow(uid).collect { goals ->
                    _savingsGoals.value = goals
                }
            }
        }
    }

    fun createBudget(category: TransactionCategory, amount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                userId = userId,
                category = category,
                amount = amount,
                month = currentDate.monthNumber,
                year = currentDate.year
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