package com.example.moneytracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.moneytracker.data.BudgetRepository
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: BudgetRepository = RepositoryProvider.provideBudgetRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : ViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<SavingsGoal>> = _savingsGoals

    private var userId: String? = null

    fun setUserId(id: String) {
        userId = id
        loadBudgets()
        loadSavingsGoals()
    }

    private fun loadBudgets() {
        userId?.let { uid ->
            scope.launch {
                repository.getBudgetsFlow(uid).collect { budgets ->
                    _budgets.value = budgets
                }
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

    fun addBudget(category: TransactionCategory, amount: Double): Flow<Result<Unit>> {
        val budget = Budget(category = category, amount = amount)
        return repository.addBudget(userId ?: return MutableStateFlow(Result.failure(IllegalStateException("User not logged in"))), budget)
    }

    fun addSavingsGoal(name: String, targetAmount: Double): Flow<Result<Unit>> {
        val goal = SavingsGoal(name = name, targetAmount = targetAmount, currentAmount = 0.0)
        return repository.addSavingsGoal(userId ?: return MutableStateFlow(Result.failure(IllegalStateException("User not logged in"))), goal)
    }

    fun updateBudget(category: TransactionCategory, amount: Double) {
        userId?.let { uid ->
            scope.launch {
                val budget = Budget(category = category, amount = amount)
                repository.updateBudget(uid, budget)
            }
        }
    }

    fun updateSavingsGoal(name: String, targetAmount: Double, currentAmount: Double) {
        userId?.let { uid ->
            scope.launch {
                val goal = SavingsGoal(name = name, targetAmount = targetAmount, currentAmount = currentAmount)
                repository.updateSavingsGoal(uid, goal)
            }
        }
    }

    fun deleteBudget(categoryId: String) {
        userId?.let { uid ->
            scope.launch {
                repository.deleteBudget(uid, categoryId)
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