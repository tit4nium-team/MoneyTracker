package com.example.moneytracker.viewmodel

import com.example.moneytracker.data.AuthRepository
import com.example.moneytracker.data.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository = RepositoryProvider.provideAuthRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow<AuthState>(AuthState.Initial)
    val state: StateFlow<AuthState> = _state

    fun signIn(email: String, password: String) {
        _state.value = AuthState.Loading
        scope.launch {
            repository.signIn(email, password)
                .onSuccess { userId ->
                    _state.value = AuthState.Success(userId)
                }
                .onFailure { error ->
                    _state.value = AuthState.Error(error.message ?: "Sign in failed")
                }
        }
    }

    fun signUp(email: String, password: String) {
        _state.value = AuthState.Loading
        scope.launch {
            repository.signUp(email, password)
                .onSuccess { userId ->
                    _state.value = AuthState.Success(userId)
                }
                .onFailure { error ->
                    _state.value = AuthState.Error(error.message ?: "Sign up failed")
                }
        }
    }

    fun signOut() {
        scope.launch {
            repository.signOut()
                .onSuccess {
                    _state.value = AuthState.Initial
                }
                .onFailure { error ->
                    _state.value = AuthState.Error(error.message ?: "Sign out failed")
                }
        }
    }
} 