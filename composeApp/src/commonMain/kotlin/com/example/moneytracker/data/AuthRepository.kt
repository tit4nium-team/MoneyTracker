package com.example.moneytracker.data

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signUp(email: String, password: String): Result<String>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUserId(): String?
} 