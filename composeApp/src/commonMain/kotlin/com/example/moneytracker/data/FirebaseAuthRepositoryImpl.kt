package com.example.moneytracker.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
// import dev.gitlive.firebase.auth.FirebaseUser // Não usado diretamente no código abaixo, mas pode ser útil
import dev.gitlive.firebase.auth.FirebaseAuthException // Específico para a lib dev.gitlive

class FirebaseAuthRepositoryImpl : AuthRepository {

    // Acessa a instância de autenticação fornecida pela biblioteca KMP
    // A inicialização do Firebase (com GoogleService-Info.plist no iOS e plugin google-services no Android)
    // deve ser tratada pela biblioteca ou pela configuração do projeto.
    private val firebaseAuth = Firebase.auth

    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val userCredential = firebaseAuth.signInWithEmailAndPassword(email, password)
            userCredential.user?.uid?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Firebase KMP signIn did not return a user or UID."))
        } catch (e: FirebaseAuthException) {
            // A lib dev.gitlive pode ter seus próprios tipos de exceção.
            // É bom capturá-los especificamente se você quiser tratar códigos de erro.
            Result.failure(Exception("Firebase KMP signIn failed: ${e.message} (Code: ${e.cause})", e))
        } catch (e: Exception) {
            // Captura geral para outros erros inesperados.
            Result.failure(Exception("An unexpected error occurred during KMP signIn: ${e.message}", e))
        }
    }

    override suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val userCredential = firebaseAuth.createUserWithEmailAndPassword(email, password)
            userCredential.user?.uid?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Firebase KMP signUp did not return a user or UID."))
        } catch (e: FirebaseAuthException) {
            Result.failure(Exception("Firebase KMP signUp failed: ${e.message} (Code: ${e.cause})", e))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred during KMP signUp: ${e.message}", e))
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) { // FirebaseAuthException também é um Exception
            Result.failure(Exception("Firebase KMP signOut failed: ${e.message}", e))
        }
    }

    override suspend fun getCurrentUserId(): String? {
        // Para dev.gitlive.firebase.auth.FirebaseAuth, currentUser é uma propriedade síncrona.
        // Não precisa ser suspend, mas a interface AuthRepository define como suspend.
        // Podemos simplesmente retornar o valor.
        return firebaseAuth.currentUser?.uid
    }
}
