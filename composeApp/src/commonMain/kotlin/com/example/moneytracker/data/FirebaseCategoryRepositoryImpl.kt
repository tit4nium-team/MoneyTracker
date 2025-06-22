package com.example.moneytracker.data

import com.example.moneytracker.model.TransactionCategory
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

// Estrutura para salvar/carregar categorias customizadas no Firestore
@Serializable
private data class FirestoreCustomCategory(
    val name: String,
    val icon: String,
    val userId: String,
    val id: String? = null
    // 'isCustom' é implícito porque apenas categorias customizadas são salvas.
)

class FirebaseCategoryRepositoryImpl : CategoryRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private fun categoriesCollection() = db.collection("categories")

    override fun getCategoriesFlow(userId: String): Flow<List<TransactionCategory>> {
        return categoriesCollection()
            .where("userId", equalTo = userId)
            .snapshots
            .map { querySnapshot ->
                val customCategories = querySnapshot.documents.mapNotNull { documentSnapshot ->
                    try {
                        val firestoreCategory = documentSnapshot.data<FirestoreCustomCategory>()
                        TransactionCategory(
                            id = documentSnapshot.id,
                            name = firestoreCategory.name,
                            icon = firestoreCategory.icon,
                            isCustom = true // Todas as categorias do Firestore são customizadas
                        )
                    } catch (e: Exception) {
                        println("Error deserializing category: ${documentSnapshot.id}, error: ${e.message}")
                        null
                    }
                }
                // Adiciona as categorias padrão à lista de categorias customizadas
                customCategories + TransactionCategory.DEFAULT_CATEGORIES
            }
    }

    override suspend fun addCategory(userId: String, category: TransactionCategory): Result<Unit> {
        return try {
            // Salva apenas categorias que são marcadas como customizadas ou se a lógica de negócio permitir
            // A implementação Android original não verificava 'isCustom' antes de salvar,
            // mas apenas categorias novas (presumivelmente customizadas) eram adicionadas.
            // Vamos assumir que apenas 'isCustom = true' devem ser salvas.
            // Se a categoria recebida não for customizada (ex: uma das DEFAULT_CATEGORIES), não a salvamos.
            if (!category.isCustom && TransactionCategory.DEFAULT_CATEGORIES.any { it.id == category.id }) {
                 return Result.failure(IllegalArgumentException("Cannot add a default category as custom."))
            }

            val firestoreCategory = FirestoreCustomCategory(
                name = category.name,
                icon = category.icon,
                userId = userId
            )
            // Se a categoria já tem um ID (ex: de uma tentativa anterior falha ou lógica de upsert),
            // poderíamos usar .document(category.id).set(firestoreCategory)
            // Mas como o ID original é gerado pelo ViewModel como "temp_...", é melhor usar add para gerar novo ID.
            categoriesCollection().add(firestoreCategory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add category: ${e.message}", e))
        }
    }

    override suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> {
        return try {
            if (TransactionCategory.DEFAULT_CATEGORIES.any { it.id == categoryId }) {
                return Result.failure(IllegalArgumentException("Cannot delete a default category."))
            }
            if (categoryId.isBlank()) {
                return Result.failure(IllegalArgumentException("Category ID cannot be blank for delete."))
            }

            val docRef = categoriesCollection().document(categoryId)
            val snapshot = docRef.get()

            if (snapshot.exists) {
                val firestoreCategory = snapshot.data<FirestoreCustomCategory>()
                if (firestoreCategory.userId == userId) {
                    docRef.delete()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Unauthorized to delete category $categoryId. Belongs to another user."))
                }
            } else {
                Result.failure(NoSuchElementException("Category $categoryId not found."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete category $categoryId: ${e.message}", e))
        }
    }
}
