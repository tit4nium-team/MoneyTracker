package com.example.moneytracker.viewmodel

import com.example.moneytracker.model.Insight
import com.example.moneytracker.model.InsightType
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.util.formatDecimalPlaces
import com.example.moneytracker.util.toCurrencyString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    // A função parseDate foi movida para FirebaseVertexAiService e não é mais necessária aqui.

    fun generateInsights(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            // Pode-se retornar um insight padrão ou simplesmente lista vazia.
            // O FirebaseVertexAiService já tem uma lógica para transações vazias.
            _state.value = InsightsState(
                insights = listOf(
                    Insight(
                        title = "Sem Transações",
                        description = "Adicione transações para obter insights financeiros.",
                        recommendation = "Comece registrando suas despesas e receitas."
                    )
                ),
                isLoading = false
            )
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        scope.launch {
            try {
                // Chama o novo serviço de IA para gerar insights
                val insights = com.example.moneytracker.service.FirebaseVertexAiService.generateFinancialInsights(transactions)

                // Verifica se os insights retornados contêm o título de erro do serviço
                if (insights.any { it.title == "Erro ao Gerar Insights" }) {
                     _state.value = InsightsState(
                        insights = insights,
                        isLoading = false,
                        error = insights.firstOrNull { it.title == "Erro ao Gerar Insights" }?.description ?: "Erro desconhecido ao gerar insights."
                    )
                } else {
                    _state.value = InsightsState(insights = insights, isLoading = false)
                }
            } catch (e: Exception) {
                // Este catch pode não ser tão necessário se o FirebaseVertexAiService já lida com exceções
                // e retorna um Insight de erro, mas é uma boa prática ter.
                _state.value = InsightsState(
                    isLoading = false,
                    error = "Falha ao buscar insights: ${e.message}",
                    insights = listOf(
                        Insight(
                            title = "Erro na Aplicação",
                            description = "Não foi possível conectar ao serviço de insights.",
                            recommendation = "Verifique sua conexão e tente novamente."
                        )
                    )
                )
            }
        }
    }

    // A função analyzeTransactions (lógica local de insights) foi removida.
    // Os insights agora são totalmente gerados pela IA.
} 