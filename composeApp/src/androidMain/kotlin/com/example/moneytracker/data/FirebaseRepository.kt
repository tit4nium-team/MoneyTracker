package com.example.moneytracker.data

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Instant

class FirebaseRepository : TransactionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val transactionsCollection = db.collection("transactions")

    override fun getTransactionsFlow(userId: String): Flow<List<Transaction>> = callbackFlow {
        val subscription = transactionsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Transaction(
                            id = doc.id,
                            type = TransactionType.valueOf(doc.getString("type") ?: return@mapNotNull null),
                            amount = doc.getDouble("amount") ?: return@mapNotNull null,
                            category = TransactionCategory.valueOf(doc.getString("category") ?: return@mapNotNull null),
                            description = doc.getString("description") ?: "",
                            date = doc.getTimestamp("date")?.toDate()?.toString() ?: "",
                            userId = doc.getString("userId") ?: return@mapNotNull null
                        )
                    } catch (e: Exception) {
                        null
                    }
                }?.sortedByDescending { it.date } ?: emptyList()

                trySend(transactions)
            }

        awaitClose { subscription.remove() }
    }

    override fun addTransaction(transaction: Transaction): Flow<Result<Unit>> = callbackFlow {
        try {
            val data = hashMapOf(
                "type" to transaction.type.name,
                "amount" to transaction.amount,
                "category" to transaction.category.name,
                "description" to transaction.description,
                "date" to com.google.firebase.Timestamp.now(),
                "userId" to transaction.userId
            )
            
            val addOperation = transactionsCollection.add(data)
            
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

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> = try {
        val data = hashMapOf(
            "type" to transaction.type.name,
            "amount" to transaction.amount,
            "category" to transaction.category.name,
            "description" to transaction.description,
            "date" to transaction.date,
            "userId" to transaction.userId
        )
        transactionsCollection.document(transaction.id).set(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> = try {
        transactionsCollection.document(transactionId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTransactionsByCategory(
        userId: String,
        category: TransactionCategory
    ): Result<List<Transaction>> = try {
        val snapshot = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("category", category.name)
            .get()
            .await()

        val transactions = snapshot.documents.mapNotNull { doc ->
            try {
                Transaction(
                    id = doc.id,
                    type = TransactionType.valueOf(doc.getString("type") ?: return@mapNotNull null),
                    amount = doc.getDouble("amount") ?: return@mapNotNull null,
                    category = TransactionCategory.valueOf(doc.getString("category") ?: return@mapNotNull null),
                    description = doc.getString("description") ?: "",
                    date = doc.getTimestamp("date").toString(),
                    userId = doc.getString("userId") ?: return@mapNotNull null
                )
            } catch (e: Exception) {
                null
            }
        }
        Result.success(transactions)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTransactionsByDateRange(
        userId: String,
        startDate: String,
        endDate: String
    ): Result<List<Transaction>> = try {
        val snapshot = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .await()

        val transactions = snapshot.documents.mapNotNull { doc ->
            try {
                Transaction(
                    id = doc.id,
                    type = TransactionType.valueOf(doc.getString("type") ?: return@mapNotNull null),
                    amount = doc.getDouble("amount") ?: return@mapNotNull null,
                    category = TransactionCategory.valueOf(doc.getString("category") ?: return@mapNotNull null),
                    description = doc.getString("description") ?: "",
                    date = doc.getTimestamp("date").toString(),
                    userId = doc.getString("userId") ?: return@mapNotNull null
                )
            } catch (e: Exception) {
                null
            }
        }
        Result.success(transactions)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
