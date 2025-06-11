package com.example.moneytracker.viewmodel

import com.example.moneytracker.data.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateEmail(email) || !validatePassword(password)) return

        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password)
                .onSuccess {
                    _state.update { it.copy(isAuthenticated = true) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        if (!validateEmail(email) || !validatePassword(password) || !validateConfirmPassword(password, confirmPassword)) return

        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.register(email, password)
                .onSuccess {
                    _state.update { it.copy(isAuthenticated = true) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun resetPassword(email: String) {
        if (!validateEmail(email)) return

        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.resetPassword(email)
                .onSuccess {
                    _state.update { it.copy(error = "Password reset email sent") }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun clearErrors() {
        _state.update {
            it.copy(
                error = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null
            )
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email cannot be empty") }
            false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "Invalid email format") }
            false
        } else {
            _state.update { it.copy(emailError = null) }
            true
        }
    }

    private fun validatePassword(password: String): Boolean {
        return if (password.length < 6) {
            _state.update { it.copy(passwordError = "Password must be at least 6 characters") }
            false
        } else {
            _state.update { it.copy(passwordError = null) }
            true
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return if (password != confirmPassword) {
            _state.update { it.copy(confirmPasswordError = "Passwords do not match") }
            false
        } else {
            _state.update { it.copy(confirmPasswordError = null) }
            true
        }
    }
} 