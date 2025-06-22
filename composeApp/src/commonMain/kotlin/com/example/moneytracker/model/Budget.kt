package com.example.moneytracker.model

import kotlinx.serialization.Serializable

package com.example.moneytracker.model

import kotlinx.serialization.Serializable
import dev.gitlive.firebase.firestore.DocumentId // Importar DocumentId

@Serializable
data class Budget(
    @DocumentId val id: String = "", // Usar @DocumentId para mapear o ID do documento Firestore
    val userId: String = "",
    val category: TransactionCategory = TransactionCategory.OTHER, // Assegure que TransactionCategory é @Serializable
    val amount: Double = 0.0,
    val month: Int = 1,
    val year: Int = 2024,
    val spent: Double = 0.0
) {
    val remaining: Double
        get() = amount - spent
    
    val progress: Float
        get() = (spent / amount).toFloat().coerceIn(0f, 1f)
}

import dev.gitlive.firebase.firestore.DocumentId // Já importado acima ou adicionar se necessário

@Serializable
data class SavingsGoal(
    @DocumentId val id: String = "", // Usar @DocumentId
    val userId: String = "",
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double
)