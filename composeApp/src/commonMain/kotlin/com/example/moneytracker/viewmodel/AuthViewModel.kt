package com.example.moneytracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.moneytracker.data.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow<AuthState>(AuthState.Initial)
    val state: StateFlow<AuthState> = _state

    var isAuthenticated by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String) {
        _state.value = AuthState.Loading
    println("MY_DEBUG_LOG: AuthViewModel login - Tentando login para email: $email")
        scope.launch {
        try {
            println("MY_DEBUG_LOG: AuthViewModel login - Dentro da coroutine, antes de repository.signIn")
            repository.signIn(email, password)
                .onSuccess { userId ->
                    error = null
                    isAuthenticated = true
                    _state.value = AuthState.Success(userId)
                    println("MY_DEBUG_LOG: AuthViewModel login - Sucesso, userId: $userId")
                }
                .onFailure { e ->
                    error = e.message ?: "Erro ao fazer login"
                    isAuthenticated = false
                    _state.value = AuthState.Error(e.message ?: "Erro ao fazer login")
                    println("MY_DEBUG_LOG: AuthViewModel login - Falha: ${e.message}")
                }
        } catch (e: Exception) {
            println("MY_DEBUG_LOG: AuthViewModel login - Exceção capturada: ${e::class.simpleName} - ${e.message}")
            _state.value = AuthState.Error(e.message ?: "Exceção inesperada no login")
        }
        }
    }

    fun register(name: String, email: String, password: String) {
        _state.value = AuthState.Loading
        scope.launch {
            repository.signUp(email, password)
                .onSuccess { userId ->
                    error = null
                    isAuthenticated = true
                    _state.value = AuthState.Success(userId)
                }
                .onFailure { e ->
                    error = e.message ?: "Erro ao criar conta"
                    isAuthenticated = false
                    _state.value = AuthState.Error(e.message ?: "Erro ao criar conta")
                }
        }
    }

    fun logout() {
        scope.launch {
            repository.signOut()
            isAuthenticated = false
            error = null
            _state.value = AuthState.Initial
        }
    }

    fun signOut() {
        scope.launch {
            repository.signOut()
            isAuthenticated = false
            error = null
            _state.value = AuthState.Initial
        }
    }
} 