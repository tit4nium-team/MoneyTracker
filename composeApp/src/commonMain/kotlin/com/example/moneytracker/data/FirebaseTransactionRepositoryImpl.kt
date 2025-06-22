package com.example.moneytracker.data

import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionCategory
import com.example.moneytracker.model.TransactionType
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

// Classe para representar a estrutura de dados no Firestore,
// especialmente para lidar com categoryId e o campo de data.
@kotlinx.serialization.Serializable
private data class FirestoreTransaction(
    val type: TransactionType,
    val amount: Double,
    val categoryId: String, // Armazena apenas o ID da categoria
    val description: String,
    val date: String, // Mantendo como String por enquanto, conforme modelo original
    val userId: String,
    val id: String? = null, // Para obter o ID do documento
     // Para ordenação, se a string 'date' não for suficiente, um timestamp real seria melhor
    val timestamp: dev.gitlive.firebase.firestore.Timestamp? = null
)

class FirebaseTransactionRepositoryImpl : TransactionRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    // Define o caminho da coleção de transações.
    // Usando uma coleção raiz "transactions" conforme a implementação Android original.
    private fun transactionsCollection() = db.collection("transactions")

    override fun getTransactionsFlow(userId: String): Flow<List<Transaction>> {
        return transactionsCollection()
            .where("userId", equalTo = userId)
            // A ordenação por 'date' (String) pode não ser cronologicamente precisa
            // a menos que o formato da string seja YYYY-MM-DD...
            // Idealmente, ordenar por um campo Timestamp real. Se 'timestamp' for adicionado:
            // .orderBy("timestamp", Direction.DESCENDING)
            .orderBy("date", Direction.DESCENDING) // Mantendo a ordenação por 'date' string por agora
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { documentSnapshot ->
                    val firestoreTransaction = try {
                        documentSnapshot.data<FirestoreTransaction>()
                    } catch (e: Exception) {
                        println("Error deserializing transaction: ${documentSnapshot.id}, error: ${e.message}")
                        return@mapNotNull null
                    }

                    Transaction(
                        id = documentSnapshot.id,
                        type = firestoreTransaction.type,
                        amount = firestoreTransaction.amount,
                        category = TransactionCategory.DEFAULT_CATEGORIES.find { it.id == firestoreTransaction.categoryId }
                            ?: TransactionCategory.OTHER, // Lógica de fallback para categoria
                        description = firestoreTransaction.description,
                        date = firestoreTransaction.date, // Ou formatar a partir de firestoreTransaction.timestamp se usado
                        userId = firestoreTransaction.userId
                    )
                }
            }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val firestoreTransaction = FirestoreTransaction(
                type = transaction.type,
                amount = transaction.amount,
                categoryId = transaction.category.id,
                description = transaction.description,
                // Usar a data da transação se fornecida, senão a data atual.
                // A implementação Android original usava Timestamp.now().
                // Para consistência e melhor ordenação, um timestamp real seria melhor.
                date = if (transaction.date.isNotBlank()) transaction.date else Clock.System.now().toString(), // Ou formato ISO específico
                userId = transaction.userId,
                timestamp = dev.gitlive.firebase.firestore.Timestamp.now() // Adicionando um timestamp real para ordenação
            )
            transactionsCollection().add(firestoreTransaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add transaction: ${e.message}", e))
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            if (transaction.id.isBlank()) {
                return Result.failure(IllegalArgumentException("Transaction ID cannot be blank for update."))
            }
            val firestoreTransaction = FirestoreTransaction(
                type = transaction.type,
                amount = transaction.amount,
                categoryId = transaction.category.id,
                description = transaction.description,
                date = transaction.date,
                userId = transaction.userId,
                // Ao atualizar, podemos querer atualizar o timestamp também, ou não, dependendo da lógica de negócios.
                // Se 'date' for a fonte da verdade e puder ser editada, 'timestamp' também deve refletir isso.
                timestamp = dev.gitlive.firebase.firestore.Timestamp.now() // Atualiza o timestamp na modificação
            )
            transactionsCollection().document(transaction.id).set(firestoreTransaction, merge = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update transaction ${transaction.id}: ${e.message}", e))
        }
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            if (transactionId.isBlank()) {
                return Result.failure(IllegalArgumentException("Transaction ID cannot be blank for delete."))
            }
            transactionsCollection().document(transactionId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete transaction ${transactionId}: ${e.message}", e))
        }
    }

    override suspend fun getTransactionsByCategory(userId: String, category: TransactionCategory): Result<List<Transaction>> {
        return try {
            val querySnapshot = transactionsCollection()
                .where("userId", equalTo = userId)
                .where("categoryId", equalTo = category.id)
                .orderBy("date", Direction.DESCENDING)
                .get()

            val transactions = querySnapshot.documents.mapNotNull { documentSnapshot ->
                val firestoreTransaction = documentSnapshot.data<FirestoreTransaction>()
                Transaction(
                    id = documentSnapshot.id,
                    type = firestoreTransaction.type,
                    amount = firestoreTransaction.amount,
                    category = TransactionCategory.DEFAULT_CATEGORIES.find { it.id == firestoreTransaction.categoryId } ?: TransactionCategory.OTHER,
                    description = firestoreTransaction.description,
                    date = firestoreTransaction.date,
                    userId = firestoreTransaction.userId
                )
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get transactions by category: ${e.message}", e))
        }
    }

    override suspend fun getTransactionsByDateRange(userId: String, startDate: String, endDate: String): Result<List<Transaction>> {
        return try {
            // Queries de intervalo em campos de string são problemáticas se o formato não for YYYY-MM-DD...
            // Esta query pode não funcionar como esperado. Um campo Timestamp real é necessário para queries de data robustas.
            val querySnapshot = transactionsCollection()
                .where("userId", equalTo = userId)
                .where("date", equalTo = startDate)
                .where("date", equalTo = endDate)
                .orderBy("date", Direction.DESCENDING)
                .get()

            val transactions = querySnapshot.documents.mapNotNull { documentSnapshot ->
                val firestoreTransaction = documentSnapshot.data<FirestoreTransaction>()
                Transaction(
                    id = documentSnapshot.id,
                    type = firestoreTransaction.type,
                    amount = firestoreTransaction.amount,
                    category = TransactionCategory.DEFAULT_CATEGORIES.find { it.id == firestoreTransaction.categoryId } ?: TransactionCategory.OTHER,
                    description = firestoreTransaction.description,
                    date = firestoreTransaction.date,
                    userId = firestoreTransaction.userId
                )
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get transactions by date range: ${e.message}", e))
        }
    }
}
