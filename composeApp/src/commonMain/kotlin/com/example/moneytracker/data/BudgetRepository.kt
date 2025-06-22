package com.example.moneytracker.data

import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    // Flows para observação de listas
    fun getBudgetsFlow(userId: String): Flow<List<Budget>> // Orçamentos gerais do usuário
    fun getSavingsGoalsFlow(userId: String): Flow<List<SavingsGoal>> // Metas de economia do usuário
    fun getMonthlyBudgetsFlow(userId: String, month: Int, year: Int): Flow<List<Budget>> // Orçamentos para um mês específico

    // Operações CRUD para Orçamentos (Budget)
    suspend fun addBudget(budget: Budget): Result<String?> // Retorna ID do novo orçamento ou null em falha
    suspend fun getBudget(budgetId: String): Result<Budget?> // Busca um orçamento específico
    suspend fun updateBudget(budget: Budget): Result<Unit> // Atualiza um orçamento existente (requer budget.id)
    suspend fun deleteBudget(budgetId: String, userId: String): Result<Unit> // Deleta um orçamento (requer verificação de userId)

    // Operações CRUD para Metas de Economia (SavingsGoal)
    suspend fun addSavingsGoal(goal: SavingsGoal): Result<String?> // Retorna ID da nova meta ou null em falha
    suspend fun getSavingsGoal(goalId: String): Result<SavingsGoal?> // Busca uma meta específica
    suspend fun updateSavingsGoal(goal: SavingsGoal): Result<Unit> // Atualiza meta (requer goal.id)
    suspend fun deleteSavingsGoal(goalId: String, userId: String): Result<Unit> // Deleta meta (requer verificação de userId)

    // Operações específicas
    suspend fun updateBudgetSpentAmount(budgetId: String, newSpentAmount: Double): Result<Unit>
    // Se updateSpentAmount for mais complexo e precisar de userId, category, month, year para identificar o orçamento:
    // suspend fun updateBudgetSpentAmount(userId: String, categoryId: String, month: Int, year: Int, newSpentAmount: Double): Result<Unit>
}