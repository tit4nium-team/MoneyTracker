package com.example.moneytracker.viewmodel

import com.example.moneytracker.model.Insight
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.service.GeminiServiceFactory // Added import for factory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
// Removed unused imports: InsightType, TransactionType, formatDecimalPlaces, toCurrencyString, abs, Month, LocalDateTime, Instant, Clock, TimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.math.abs

data class InsightsState(
    val insights: List<Insight> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class InsightsViewModel(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    // Removed parseDate function as it was part of the old local analysis

    fun generateInsights(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            _state.value = InsightsState(insights = emptyList(), isLoading = false, error = null)
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        scope.launch {
            try {
                // Call the AI service via the factory
                val insights = GeminiServiceFactory.getInstance().generateFinancialInsights(transactions)
                _state.value = InsightsState(insights = insights, isLoading = false, error = null)
            } catch (e: Exception) {
                // The service itself might return an error Insight,
                // but this catch block handles exceptions during the call itself (e.g., network issues not caught by the service)
                _state.value = InsightsState(
                    insights = emptyList(),
                    isLoading = false,
                    error = "Falha ao gerar análises: ${e.message}"
                )
            }
        }
    }

    // Removed analyzeTransactions function as it's now handled by GoogleAIGeminiInsightGenerator
} 