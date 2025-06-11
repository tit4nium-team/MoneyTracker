package com.example.moneytracker

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.ui.screens.AuthScreen
import com.example.moneytracker.ui.screens.EditExpenseScreen
import com.example.moneytracker.ui.screens.MainScreen
import com.example.moneytracker.ui.screens.SplashScreen
import com.example.moneytracker.ui.theme.MoneyTrackerTheme
import com.example.moneytracker.viewmodel.AuthViewModel
import com.example.moneytracker.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    object Main : Screen()
    object EditExpense : Screen()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    val repository = remember { RepositoryProvider.provideTransactionRepository() }
    val authRepository = remember { RepositoryProvider.provideAuthRepository() }
    val authViewModel = remember { AuthViewModel(authRepository) }
    val transactionViewModel = remember { TransactionViewModel(repository) }
    val scope = rememberCoroutineScope()
    
    MoneyTrackerTheme {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn() with fadeOut()
            }
        ) { screen ->
            when (screen) {
                Screen.Splash -> {
                    LaunchedEffect(Unit) {
                        val userId = authRepository.getCurrentUserId()
                        currentScreen = if (userId != null) {
                            transactionViewModel.setUserId(userId)
                            Screen.Main
                        } else {
                            Screen.Auth
                        }
                    }
                    SplashScreen(
                        onSplashComplete = { /* Handled by LaunchedEffect */ }
                    )
                }
                Screen.Auth -> {
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            scope.launch {
                                authRepository.getCurrentUserId()?.let { userId ->
                                    transactionViewModel.setUserId(userId)
                                }
                                currentScreen = Screen.Main
                            }
                        }
                    )
                }
                Screen.Main -> {
                    MainScreen(
                        viewModel = transactionViewModel,
                        onAddTransaction = { currentScreen = Screen.EditExpense }
                    )
                }
                Screen.EditExpense -> {
                    EditExpenseScreen(
                        onNavigateBack = { currentScreen = Screen.Main },
                        viewModel = transactionViewModel
                    )
                }
            }
        }
    }
}