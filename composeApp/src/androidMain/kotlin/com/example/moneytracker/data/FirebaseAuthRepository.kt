package com.example.moneytracker.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    override suspend fun signIn(email: String, password: String): Result<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.uid?.let {
            Result.success(it)
        } ?: Result.failure(IllegalStateException("Failed to get user ID after sign in"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signUp(email: String, password: String): Result<String> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.uid?.let {
            Result.success(it)
        } ?: Result.failure(IllegalStateException("Failed to get user ID after sign up"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
} 