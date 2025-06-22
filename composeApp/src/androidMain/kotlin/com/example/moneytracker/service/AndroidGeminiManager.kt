package com.example.moneytracker.service

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.vertexai.generativeModel
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

internal actual class PlatformGeminiManager {

    private val generativeModel by lazy {
        try {
            // Configure o nome do modelo conforme necessário. Ex: "gemini-1.5-flash", "gemini-1.5-pro"
            // As configurações de safety e generationConfig podem ser adicionadas aqui.
            Firebase.vertexAI.generativeModel("gemini-1.5-flash")
        } catch (e: Exception) {
            Log.e("AndroidGeminiManager", "Erro ao inicializar o GenerativeModel do Firebase: ${e.message}", e)
            null // Retorna null se a inicialização falhar para tratamento posterior
        }
    }

    actual suspend fun generateContent(prompt: String): String? {
        val currentModel = generativeModel
        if (currentModel == null) {
            Log.e("AndroidGeminiManager", "GenerativeModel não foi inicializado. Verifique a configuração do Firebase e as dependências.")
            return null
        }

        return try {
            // Usar withContext(Dispatchers.IO) para chamadas de rede/bloqueantes
            withContext(Dispatchers.IO) {
                Log.d("AndroidGeminiManager", "Enviando prompt para o Firebase AI:\n$prompt")
                val response = currentModel.generateContent(prompt) // generateContent é suspend
                Log.d("AndroidGeminiManager", "Resposta recebida do Firebase AI.")
                response.text
            }
        } catch (e: Exception) {
            Log.e("AndroidGeminiManager", "Erro ao chamar generateContent no Firebase AI: ${e.message}", e)
            null
        }
    }
}

// Implementação de initializePlatformManager para Android
internal actual fun initializePlatformManager(): PlatformGeminiManager {
    return PlatformGeminiManager() // Retorna a instância do Android
}
