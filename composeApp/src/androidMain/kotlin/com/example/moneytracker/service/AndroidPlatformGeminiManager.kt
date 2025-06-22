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


// Imports corretos para Firebase Vertex AI
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI // Import para a extensão vertexAI
import com.google.firebase.vertexai.generativeModel // Import para a função de extensão generativeModel
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.type.safetySetting
import com.google.firebase.vertexai.type.HarmCategory
import com.google.firebase.vertexai.type.BlockThreshold
import com.google.firebase.vertexai.VertexAI

// Demais imports que já estavam (alguns podem ser removidos se não usados pela nova impl.)
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.Insight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlin.math.abs
import android.util.Log

// Implementação 'actual' para a 'expect class PlatformGeminiManager' definida em commonMain
internal actual class PlatformGeminiManager {

    // Tentativa simplificada para isolar o problema de resolução do vertexAI
    private val generativeModel by lazy {
        try {
            Log.d("AndroidGeminiManager", "Attempting to initialize Firebase Vertex AI generative model...")
            // A forma mais direta de chamar, conforme a documentação KTX.
            // Se Firebase.vertexAI não resolver, o problema é na configuração da dependência/plugin.
            val model = Firebase.vertexAI.generativeModel("gemini-1.5-flash")
            Log.d("AndroidGeminiManager", "Firebase Vertex AI generative model initialized successfully.")
            model
        } catch (e: Exception) {
            Log.e("AndroidGeminiManager", "CRITICAL: Error initializing Firebase Vertex AI generative model: ${e.message}", e)
            null
        }
    }

    actual suspend fun generateContent(prompt: String): String? {
        val currentModel = generativeModel
        if (currentModel == null) {
            Log.e("AndroidGeminiManager", "GenerativeModel não foi inicializado.")
            return null
        }

        return try {
            withContext(Dispatchers.IO) {
                Log.i("AndroidGeminiManager", "Enviando prompt para Firebase AI...")
                // Log do prompt pode ser muito grande, considere truncar ou logar apenas o início/fim
                // Log.d("AndroidGeminiManager", "Prompt: ${prompt.take(500)}...")
                val response = currentModel.generateContent(prompt)
                Log.i("AndroidGeminiManager", "Resposta recebida do Firebase AI.")
                response.text
            }
        } catch (e: Exception) {
            Log.e("AndroidGeminiManager", "Erro ao chamar generateContent no Firebase AI: ${e.message}", e)
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