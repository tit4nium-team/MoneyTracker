package com.example.moneytracker

import LoginScreen
import RegisterScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.ui.screens.*
import com.example.moneytracker.ui.theme.MoneyTrackerTheme
import com.example.moneytracker.viewmodel.AuthState
import com.example.moneytracker.viewmodel.AuthViewModel
import com.example.moneytracker.viewmodel.CategoryViewModel
import com.example.moneytracker.viewmodel.TransactionViewModel
import com.example.moneytracker.viewmodel.InsightsViewModel
import com.example.moneytracker.viewmodel.BudgetViewModel
import com.example.moneytracker.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object EditExpense : Screen("edit_expense")
    object MonthlyHistory : Screen("monthly_history")
    object Insights : Screen("insights")
    object Settings : Screen("settings")
    object Budget : Screen("budget")
    object Chat : Screen("chat")
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
    val insightsViewModel = remember { InsightsViewModel() }
    val budgetViewModel = remember { 
        BudgetViewModel(
            repository = RepositoryProvider.provideBudgetRepository(),
            categoryRepository = RepositoryProvider.provideCategoryRepository(),
            transactionRepository = RepositoryProvider.provideTransactionRepository()
        ) 
    }
    val transactionViewModel = remember { TransactionViewModel(repository, categoryViewModel, budgetViewModel) }
    val chatViewModel = remember { ChatViewModel(transactionViewModel, budgetViewModel) }
    val scope = rememberCoroutineScope()
    val authState by authViewModel.state.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Initial -> {
                currentScreen = Screen.Auth
            }
            is AuthState.Success -> {
                val userId = (authState as AuthState.Success).userId
                transactionViewModel.setUserId(userId)
                if (currentScreen == Screen.Auth) {
                    currentScreen = Screen.Dashboard
                }
            }
            else -> {}
        }
    }

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
        println("MY_DEBUG_LOG: SplashScreen LaunchedEffect - Verificando usuário atual...")
                        val userId = authRepository.getCurrentUserId()
        println("MY_DEBUG_LOG: SplashScreen LaunchedEffect - userId: $userId")
                        currentScreen = if (userId != null) {
                            transactionViewModel.setUserId(userId)
                            Screen.Dashboard
                        } else {
                            Screen.Auth
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        SplashScreen(
                            onTimeout = {
                                scope.launch {
                    println("MY_DEBUG_LOG: SplashScreen onTimeout - Verificando usuário atual...")
                                    val userId = authRepository.getCurrentUserId()
                     println("MY_DEBUG_LOG: SplashScreen onTimeout - userId: $userId")
                                    currentScreen = if (userId != null) {
                                        transactionViewModel.setUserId(userId)
                                        Screen.Dashboard
                                    } else {
                                        Screen.Auth
                                    }
                                }
                            }
                        )
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
                        },
                        onNavigateToLogin = { currentScreen = Screen.Login },
                        onNavigateToRegister = { currentScreen = Screen.Register }
                    )
                }
                Screen.Login -> {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onNavigateToRegister = { currentScreen = Screen.Register },
                        onLoginSuccess = {
                            scope.launch {
                                authRepository.getCurrentUserId()?.let { userId ->
                                    transactionViewModel.setUserId(userId)
                                    currentScreen = Screen.Dashboard
                                }
                            }
                        }
                    )
                }
                Screen.Register -> {
                    RegisterScreen(
                        authViewModel = authViewModel,
                        onNavigateToLogin = { currentScreen = Screen.Login },
                        onRegisterSuccess = {
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
                    MainScreen(
                        viewModel = transactionViewModel,
                        authViewModel = authViewModel,
                        categoryViewModel = categoryViewModel,
                        budgetViewModel = budgetViewModel,
                        onNavigate = { route ->
                            when (route) {
                                Screen.EditExpense.route -> currentScreen = Screen.EditExpense
                                Screen.MonthlyHistory.route -> currentScreen = Screen.MonthlyHistory
                                Screen.Insights.route -> currentScreen = Screen.Insights
                                Screen.Settings.route -> {} // Implementar tela de configurações
                                Screen.Budget.route -> currentScreen = Screen.Budget
                                Screen.Chat.route -> currentScreen = Screen.Chat
                            }
                        }
                    )
                }
                Screen.EditExpense -> {
                    EditExpenseScreen(
                        viewModel = transactionViewModel,
                        categoryViewModel = categoryViewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
                Screen.MonthlyHistory -> {
                    MonthlyHistoryScreen(
                        state = transactionViewModel.state.collectAsState().value,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
                Screen.Insights -> {
                    InsightsScreen(
                        state = transactionViewModel.state.collectAsState().value,
                        insightsViewModel = insightsViewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
                Screen.Budget -> {
                    BudgetScreen(
                        viewModel = budgetViewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
                Screen.Chat -> {
                    ChatScreen(
                        viewModel = chatViewModel,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                }
                Screen.Settings -> {} // Implementar tela de configurações
            }
        }
    }
}