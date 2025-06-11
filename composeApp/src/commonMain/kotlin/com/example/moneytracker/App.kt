package com.example.moneytracker

import MonthlyHistoryScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.ui.screens.*
import com.example.moneytracker.ui.theme.MoneyTrackerTheme
import com.example.moneytracker.viewmodel.AuthViewModel
import com.example.moneytracker.viewmodel.CategoryViewModel
import com.example.moneytracker.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    object Dashboard : Screen()
    object EditExpense : Screen()
    object MonthlyHistory : Screen()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    val repository = remember { RepositoryProvider.provideTransactionRepository() }
    val authRepository = remember { RepositoryProvider.provideAuthRepository() }
    val categoryRepository = remember { RepositoryProvider.provideCategoryRepository() }
    val authViewModel = remember { AuthViewModel(authRepository) }
    val categoryViewModel = remember { CategoryViewModel(categoryRepository) }
    val transactionViewModel = remember { TransactionViewModel(repository, categoryViewModel) }
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
                            Screen.Dashboard
                        } else {
                            Screen.Auth
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        SplashScreen()
                    }
                }
                Screen.Auth -> {
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            scope.launch {
                                authRepository.getCurrentUserId()?.let { userId ->
                                    transactionViewModel.setUserId(userId)
                                    currentScreen = Screen.Dashboard
                                }
                            }
                        }
                    )
                }
                Screen.Dashboard -> {
                    DashboardScreen(
                        viewModel = transactionViewModel,
                        categoryViewModel = categoryViewModel,
                        onAddTransaction = { currentScreen = Screen.EditExpense },
                        onNavigateToHistory = { currentScreen = Screen.MonthlyHistory },
                        onSignOut = {
                            scope.launch {
                                authRepository.signOut()
                                currentScreen = Screen.Auth
                            }
                        }
                    )
                }
                Screen.EditExpense -> {
                    EditExpenseScreen(
                        onNavigateBack = { currentScreen = Screen.Dashboard },
                        viewModel = transactionViewModel,
                        categoryViewModel = categoryViewModel
                    )
                }
                Screen.MonthlyHistory -> {
                    MonthlyHistoryScreen(
                        viewModel = transactionViewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
            }
        }
    }
}