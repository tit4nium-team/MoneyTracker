package com.example.moneytracker.data

import com.example.moneytracker.model.Budget
import com.example.moneytracker.model.SavingsGoal
import com.example.moneytracker.model.TransactionCategory // Necessário se Budget tem TransactionCategory
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import dev.gitlive.firebase.firestore.orderBy // Se necessário para queries
import dev.gitlive.firebase.firestore.Direction // Se necessário para queries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseBudgetRepositoryImpl : BudgetRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private fun budgetsCollection() = db.collection("budgets")
    private fun savingsGoalsCollection() = db.collection("savings_goals")

    // --- Flows para observação de listas ---
    override fun getBudgetsFlow(userId: String): Flow<List<Budget>> {
        return budgetsCollection()
            .where("userId", isEqualTo = userId)
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.map { it.data<Budget>() }
            }
    }

    override fun getSavingsGoalsFlow(userId: String): Flow<List<SavingsGoal>> {
        return savingsGoalsCollection()
            .where("userId", isEqualTo = userId)
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.map { it.data<SavingsGoal>() }
            }
    }

    override fun getMonthlyBudgetsFlow(userId: String, month: Int, year: Int): Flow<List<Budget>> {
        return budgetsCollection()
            .where("userId", isEqualTo = userId)
            .where("month", isEqualTo = month)
            .where("year", isEqualTo = year)
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.map { it.data<Budget>() }
            }
    }

    // --- Operações CRUD para Orçamentos (Budget) ---
    override suspend fun addBudget(budget: Budget): Result<String?> {
        return try {
            // Assegura que o ID não seja enviado para o Firestore se for para ser gerado automaticamente
            // A biblioteca KMP pode lidar com isso se o campo 'id' no modelo for nullable e anotado corretamente.
            // Se o 'id' no modelo Budget for non-null e @DocumentId, a biblioteca o preencherá na leitura.
            // Para escrita, se 'id' for vazio, o Firestore gera um. Se preenchido, tenta usar esse ID.
            val docRef = budgetsCollection().add(budget)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add budget: ${e.message}", e))
        }
    }

    override suspend fun getBudget(budgetId: String): Result<Budget?> {
        return try {
            val documentSnapshot = budgetsCollection().document(budgetId).get()
            if (documentSnapshot.exists) {
                Result.success(documentSnapshot.data<Budget>())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get budget $budgetId: ${e.message}", e))
        }
    }

    override suspend fun updateBudget(budget: Budget): Result<Unit> {
        return try {
            if (budget.id.isBlank()) return Result.failure(IllegalArgumentException("Budget ID is required for update."))
            budgetsCollection().document(budget.id).set(budget, merge = true) // merge = true para update parcial
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update budget ${budget.id}: ${e.message}", e))
        }
    }

    override suspend fun deleteBudget(budgetId: String, userId: String): Result<Unit> {
        return try {
            if (budgetId.isBlank()) return Result.failure(IllegalArgumentException("Budget ID is required for delete."))

            // Opcional: Verificar propriedade antes de deletar, se a regra de segurança do Firestore não for suficiente.
            val budgetToDelete = getBudget(budgetId).getOrNull()
            if (budgetToDelete?.userId != userId) {
                return Result.failure(SecurityException("User not authorized to delete budget $budgetId or budget not found."))
            }

            budgetsCollection().document(budgetId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete budget $budgetId: ${e.message}", e))
        }
    }

    // --- Operações CRUD para Metas de Economia (SavingsGoal) ---
    override suspend fun addSavingsGoal(goal: SavingsGoal): Result<String?> {
        return try {
            val docRef = savingsGoalsCollection().add(goal)
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add savings goal: ${e.message}", e))
        }
    }

    override suspend fun getSavingsGoal(goalId: String): Result<SavingsGoal?> {
        return try {
            val documentSnapshot = savingsGoalsCollection().document(goalId).get()
            if (documentSnapshot.exists) {
                Result.success(documentSnapshot.data<SavingsGoal>())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get savings goal $goalId: ${e.message}", e))
        }
    }

    override suspend fun updateSavingsGoal(goal: SavingsGoal): Result<Unit> {
        return try {
            if (goal.id.isBlank()) return Result.failure(IllegalArgumentException("SavingsGoal ID is required for update."))
            savingsGoalsCollection().document(goal.id).set(goal, merge = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update savings goal ${goal.id}: ${e.message}", e))
        }
    }

    override suspend fun deleteSavingsGoal(goalId: String, userId: String): Result<Unit> {
        return try {
            if (goalId.isBlank()) return Result.failure(IllegalArgumentException("SavingsGoal ID is required for delete."))

            val goalToDelete = getSavingsGoal(goalId).getOrNull()
            if (goalToDelete?.userId != userId) {
                 return Result.failure(SecurityException("User not authorized to delete savings goal $goalId or goal not found."))
            }

            savingsGoalsCollection().document(goalId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete savings goal $goalId: ${e.message}", e))
        }
    }

    // --- Operações Específicas ---
    override suspend fun updateBudgetSpentAmount(budgetId: String, newSpentAmount: Double): Result<Unit> {
        return try {
            if (budgetId.isBlank()) return Result.failure(IllegalArgumentException("Budget ID is required."))
            budgetsCollection().document(budgetId).update("spent" to newSpentAmount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update spent amount for budget $budgetId: ${e.message}", e))
        }
    }
}
