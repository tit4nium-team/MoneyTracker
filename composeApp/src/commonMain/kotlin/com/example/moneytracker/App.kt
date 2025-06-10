package com.example.moneytracker

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.example.moneytracker.data.RepositoryProvider
import com.example.moneytracker.ui.screens.MainScreen
import com.example.moneytracker.ui.screens.SplashScreen
import com.example.moneytracker.ui.theme.MoneyTrackerTheme
import com.example.moneytracker.viewmodel.TransactionViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {
    var showSplash by remember { mutableStateOf(true) }
    val repository = remember { RepositoryProvider.provideTransactionRepository() }
    val viewModel = remember { TransactionViewModel(repository) }
    
    MoneyTrackerTheme {
        AnimatedContent(
            targetState = showSplash,
            transitionSpec = {
                fadeIn() with fadeOut()
            }
        ) { isSplashScreen ->
            if (isSplashScreen) {
                SplashScreen(
                    onSplashComplete = { showSplash = false }
                )
            } else {
                MainScreen(viewModel)
            }
        }
    }
}