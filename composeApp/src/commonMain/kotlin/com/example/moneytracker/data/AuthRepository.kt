package com.example.moneytracker.data

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun signOut()
    suspend fun getCurrentUserId(): String?
} 