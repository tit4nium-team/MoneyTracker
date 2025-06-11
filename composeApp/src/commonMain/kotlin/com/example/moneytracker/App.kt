package com.example.moneytracker

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.ui.screens.AuthScreen
import com.example.moneytracker.ui.screens.DashboardScreen
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
    object Dashboard : Screen()
    object EditExpense : Screen()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {
    MoneyTrackerTheme {
        MainScreen()
    }
}