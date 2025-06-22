package com.example.moneytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
// import com.example.moneytracker.service.initializeGeminiService // Removido
// import com.example.moneytracker.service.initializeGilService // Removido
import com.example.moneytracker.ui.theme.MoneyTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize services
        // initializeGeminiService() // Removido
        // initializeGilService() // Removido, pois GilService foi migrado para FirebaseChatService em commonMain
        
        setContent {
            MoneyTrackerTheme {
                App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}