package com.example.moneytracker.data

import com.example.moneytracker.model.TransactionCategory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseCategoryRepository : CategoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val categoriesCollection = db.collection("categories")

    override fun getCategoriesFlow(userId: String): Flow<List<TransactionCategory>> = callbackFlow {
        val subscription = categoriesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val categories = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        TransactionCategory(
                            id = doc.id,
                            name = doc.getString("name") ?: return@mapNotNull null,
                            icon = doc.getString("icon") ?: "shopping_cart",
                            isCustom = true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }?.toMutableList() ?: mutableListOf()

                // Add default categories
                categories.addAll(TransactionCategory.DEFAULT_CATEGORIES)
                
                trySend(categories)
            }

        awaitClose { subscription.remove() }
    }

    override fun addCategory(userId: String, category: TransactionCategory): Flow<Result<Unit>> = callbackFlow {
        try {
            val data = hashMapOf(
                "name" to category.name,
                "icon" to category.icon,
                "userId" to userId
            )
            
            val addOperation = categoriesCollection.add(data)
            
            addOperation.addOnSuccessListener {
                trySend(Result.success(Unit))
                close()
            }.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
                close(exception)
            }
            
            awaitClose()
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
        }
    }

    override suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> = try {
        val doc = categoriesCollection.document(categoryId)
        val snapshot = doc.get().await()
        
        if (snapshot.exists() && snapshot.getString("userId") == userId) {
            doc.delete().await()
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Category not found or unauthorized"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
} 