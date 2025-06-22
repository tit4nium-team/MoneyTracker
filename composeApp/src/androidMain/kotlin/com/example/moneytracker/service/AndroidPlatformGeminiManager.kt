package com.example.moneytracker.service

import com.example.moneytracker.model.Transaction // Manter por enquanto
import com.example.moneytracker.model.Insight // Manter por enquanto
// Removido: import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.* // Manter para parseDate e manipulação de datas
import kotlin.math.abs
import android.util.Log
// Removido: import com.example.moneytracker.config.ApiConfig // ApiConfig foi deletado
// Removido: import com.example.moneytracker.BuildConfig // Se ApiConfig usava, pode não ser mais necessário aqui diretamente

// Imports do Firebase Vertex AI SDK serão adicionados aqui quando implementarmos
// import com.google.firebase.vertexai.FirebaseVertexAI
// import com.google.firebase.vertexai.type.GenerateContentResponse ---- (ou o tipo de resposta correto)


// Imports para o SDK base do Google AI
import com.google.ai.client.generativeai.GenerativeModel // Classe principal do SDK
import com.google.ai.client.generativeai.type.BlockThreshold // Para safetySettings
import com.google.ai.client.generativeai.type.HarmCategory // Para safetySettings
import com.google.ai.client.generativeai.type.SafetySetting // Para safetySettings
import com.google.ai.client.generativeai.type.generationConfig // Função para GenerationConfig
// import com.google.ai.client.generativeai.type.GenerateContentResponse // Se precisar do tipo de resposta explícito

import com.example.moneytracker.config.ApiConfig // Para a chave de API
import com.example.moneytracker.model.Transaction // Ainda usado por parseDate se ele permanecer aqui
import com.example.moneytracker.model.Insight // Embora o parsing seja common, pode ser útil para referência
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.math.abs
import android.util.Log

// Implementação 'actual' para a 'expect class PlatformGeminiManager' definida em commonMain
internal actual class PlatformGeminiManager {

    private var generativeModel: GenerativeModel? = null
    private val modelName = "gemini-1.5-flash" // Ou outro modelo base compatível, ex: "gemini-pro"

    init {
        initializeModel()
    }

    private fun initializeModel() {
        try {
            Log.d("AndroidPlaManager", "Inicializando GenerativeModel com Google AI SDK...")
            // Exemplo de configuração de segurança. Ajuste conforme necessário.
            val safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.BLOCK_MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.BLOCK_MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.BLOCK_MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
            )
            // Exemplo de configuration de geração
            val config = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            }

            generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = ApiConfig.GEMINI_API_KEY, // Usa a chave do ApiConfig
                safetySettings = safetySettings,
                generationConfig = config
            )
            Log.d("AndroidPlaManager", "GenerativeModel inicializado com sucesso.")
        } catch (e: Exception) {
            Log.e("AndroidPlaManager", "Erro ao inicializar GenerativeModel: ${e.message}", e)
            // generativeModel permanecerá nulo, as chamadas subsequentes falharão ou retornarão null.
        }
    }

    actual suspend fun generateContent(prompt: String): String? {
        val currentModel = generativeModel
        if (currentModel == null) {
            Log.e("AndroidPlaManager", "GenerativeModel não foi inicializado. Verifique a chave de API e a configuração.")
            // Tentar reinicializar pode ser uma opção, ou falhar diretamente.
            // initializeModel() // Cuidado com chamadas repetidas se a chave estiver errada.
            // currentModel = generativeModel
            // if (currentModel == null) return null
            return null
        }

        return try {
            withContext(Dispatchers.IO) {
                Log.i("AndroidPlaManager", "Enviando prompt para Google AI SDK...")
                val response = currentModel.generateContent(prompt)
                Log.i("AndroidPlaManager", "Resposta recebida do Google AI SDK.")
                response.text
            }
        } catch (e: Exception) {
            Log.e("AndroidPlaManager", "Erro ao chamar generateContent no Google AI SDK: ${e.message}", e)
            null
        }
    }

    // As funções buildFinancialPrompt e parseInsights foram movidas para commonMain (InsightGenerator e buildFinancialPromptCommon)
    // A função parseDate permanece aqui por enquanto, pois usa SimpleDateFormat (específico do Android).
    // Se for movida para commonMain, precisará de uma implementação KMP.
    private fun parseDate(dateStr: String): LocalDateTime {
        return try {
            val pattern = "EEE MMM dd HH:mm:ss 'GMT'XXX yyyy"
            val formatter = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
            val parsedDate = formatter.parse(dateStr)
            Instant.fromEpochMilliseconds(parsedDate.time)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            Log.w("AndroidGeminiManager", "Erro ao parsear data '$dateStr', usando data atual: ${e.message}")
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}

// Implementação 'actual' para a 'expect fun initializePlatformManager' definida em commonMain
internal actual fun initializePlatformManager(): PlatformGeminiManager {
    Log.d("AndroidGeminiManager", "initializePlatformManager() chamado.")
    return PlatformGeminiManager()
}