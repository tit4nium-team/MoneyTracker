package com.example.moneytracker.data

import kotlinx.coroutines.flow.flowOf

class IosAuthRepositoryDummy : AuthRepository {
    private val errorMessage = "Funcionalidade de autenticação não implementada no iOS."

    override suspend fun signIn(email: String, password: String): Result<String> {
        println("WARN: IosAuthRepositoryDummy.signIn chamado")
        return Result.failure(UnsupportedOperationException(errorMessage))
    }

    override suspend fun signUp(email: String, password: String): Result<String> {
        println("WARN: IosAuthRepositoryDummy.signUp chamado")
        return Result.failure(UnsupportedOperationException(errorMessage))
    }

    override suspend fun signOut(): Result<Unit> {
        println("WARN: IosAuthRepositoryDummy.signOut chamado")
        return Result.success(Unit) // Retorna sucesso para não causar erro no logout
    }

    override suspend fun getCurrentUserId(): String? {
        println("WARN: IosAuthRepositoryDummy.getCurrentUserId chamado")
        return null // Simula nenhum usuário logado
    }
}
