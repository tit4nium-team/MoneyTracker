package com.example.moneytracker.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Imports específicos do Firebase iOS SDK (precisarão ser verificados/ajustados com base no nome real do módulo no Xcode)
// Estes são exemplos e podem não ser os nomes exatos ou podem precisar de um prefixo do módulo.
// import FirebaseCore // Para FirebaseApp.configure() - geralmente feito no AppDelegate
// import FirebaseVertexAI // O nome real do módulo para Vertex AI / Gemini no iOS

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// É CRUCIAL que os imports para o Firebase iOS SDK sejam os corretos.
// Eles dependerão de como o Swift Package Manager expõe os módulos ao Kotlin/Native.
// Frequentemente, o nome do módulo SPM é usado como prefixo.
// Exemplo: Se o produto do SPM for FirebaseVertexAI, os imports podem ser:
// import FirebaseVertexAI.FIRVertexAI (ou apenas VertexAI se for um tipo Swift puro)
// import FirebaseVertexAI.FIRGenerativeModel (ou apenas GenerativeModel)
// import FirebaseCore.FIRApp (ou apenas FirebaseApp)

// Estes são placeholders e precisarão ser verificados/ajustados no seu ambiente Xcode.
// import cocoapods.FirebaseVertexAI.* // Se estiver usando CocoaPods e quer acesso total
import com.example.moneytracker.config.ApiConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Estruturas de dados para a API REST do Google AI Gemini
@Serializable
private data class GeminiRequestPart(val text: String)

@Serializable
private data class GeminiRequestContent(val parts: List<GeminiRequestPart>)

@Serializable
private data class GeminiSafetySetting(
    val category: String, // e.g., "HARM_CATEGORY_HARASSMENT"
    val threshold: String // e.g., "BLOCK_MEDIUM_AND_ABOVE"
)

@Serializable
private data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val topK: Int? = null,
    val topP: Float? = null,
    val maxOutputTokens: Int? = null,
    val stopSequences: List<String>? = null
)

@Serializable
private data class GeminiRequestBody(
    val contents: List<GeminiRequestContent>,
    val safetySettings: List<GeminiSafetySetting>? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
private data class GeminiResponsePart(val text: String)

@Serializable
private data class GeminiResponseCandidate(
    val content: GeminiRequestContent? = null, // Reutilizando GeminiRequestContent para a estrutura de 'parts'
    val finishReason: String? = null,
    val safetyRatings: List<GeminiSafetySetting>? = null // Reutilizando GeminiSafetySetting
)

@Serializable
private data class GeminiResponsePromptFeedback(
    val blockReason: String? = null,
    val safetyRatings: List<GeminiSafetySetting>? = null
)

@Serializable
private data class GeminiResponseBody(
    val candidates: List<GeminiResponseCandidate>? = null,
    val promptFeedback: GeminiResponsePromptFeedback? = null
)
// Fim das estruturas de dados da API REST

@OptIn(ExperimentalForeignApi::class) // Mantido por causa de NSError, mas não usado ativamente
internal actual class PlatformGeminiManager {

    // Cliente Ktor para chamadas de rede. Configurado para iOS.
    // TODO: Adicionar dependências do Ktor no build.gradle.kts (ktor-client-darwin, ktor-client-content-negotiation, ktor-serialization-kotlinx-json)
    private val httpClient by lazy {
        HttpClient {
            // install(ContentNegotiation) {
            //     json(Json {
            //         prettyPrint = true
            //         isLenient = true
            //         ignoreUnknownKeys = true
            //     })
            // }
            // TODO: Configurar engine Darwin específico se necessário, ou usar o default.
        }
    }

    private val modelName = "gemini-1.5-flash" // Ou "gemini-pro"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    init {
        println("IosGeminiManager: init. Usará chamadas REST para Google AI Gemini API.")
    }

    actual suspend fun generateContent(prompt: String): String? {
        val apiKey = ApiConfig.GEMINI_API_KEY_IOS
        if (apiKey.startsWith("YOUR_")) {
            println("IosGeminiManager: Chave de API para iOS não configurada em ApiConfig.kt.")
            return """
            [{"title":"Erro de Configuração (iOS)","description":"Chave de API não configurada.","recommendation":"Configure GEMINI_API_KEY_IOS em ApiConfig.kt (iosMain)."}]
            """.trimIndent()
        }

        val requestBody = GeminiRequestBody(
            contents = listOf(GeminiRequestContent(parts = listOf(GeminiRequestPart(text = prompt)))),
            // TODO: Adicionar safetySettings e generationConfig se desejar, conforme a API REST
            // safetySettings = listOf(GeminiSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE")),
            // generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 1024)
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        return try {
            withContext(Dispatchers.Default) { // Usar Dispatchers.Default para Ktor/Native
                println("IosGeminiManager: Enviando prompt para Google AI API (REST)...")
                val response: HttpResponse = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(GeminiRequestBody.serializer(), requestBody))
                }

                if (response.status == HttpStatusCode.OK) {
                    val responseText = response.bodyAsText(Charsets.UTF_8)
                    println("IosGeminiManager: Resposta recebida (status OK).")
                    // Log.v("IosGeminiManager", "Raw response: $responseText")

                    // A API do Gemini retorna um JSON que contém o texto gerado dentro de uma estrutura.
                    // Precisamos extrair o texto útil.
                    val geminiResponseBody = json.decodeFromString(GeminiResponseBody.serializer(), responseText)

                    // Concatenar o texto de todas as partes de todos os candidatos (geralmente haverá um)
                    val fullTextResponse = geminiResponseBody.candidates?.joinToString(separator = "\n") { candidate ->
                        candidate.content?.parts?.joinToString(separator = "") { part -> part.text } ?: ""
                    } ?: ""

                    if (fullTextResponse.isBlank() && geminiResponseBody.promptFeedback?.blockReason != null) {
                        println("IosGeminiManager: Prompt bloqueado. Razão: ${geminiResponseBody.promptFeedback.blockReason}")
                        // Retornar um JSON indicando o bloqueio
                        """
                        [{"title":"Prompt Bloqueado","description":"Seu pedido foi bloqueado por políticas de segurança: ${geminiResponseBody.promptFeedback.blockReason}.","recommendation":"Tente reformular seu pedido."}]
                        """.trimIndent()
                    } else if (fullTextResponse.isNotBlank()) {
                        fullTextResponse
                    } else {
                        println("IosGeminiManager: Resposta de texto vazia, mas sem bloqueio explícito.")
                        // Retornar um JSON indicando resposta vazia
                         """
                        [{"title":"Resposta Vazia","description":"A IA não forneceu uma resposta de texto.","recommendation":"Tente novamente ou reformule seu pedido."}]
                        """.trimIndent()
                    }
                } else {
                    val errorBody = response.bodyAsText(Charsets.UTF_8)
                    println("IosGeminiManager: Erro na chamada REST: ${response.status} - $errorBody")
                    // Retornar um JSON de erro
                    """
                    [{"title":"Erro de API (${response.status})","description":"Falha ao comunicar com o serviço de IA. Detalhes: ${errorBody.take(100)}","recommendation":"Verifique a conexão e a chave de API."}]
                    """.trimIndent()
                }
            }
        } catch (e: Exception) {
            println("IosGeminiManager: Exceção ao chamar Google AI API (REST): ${e.message}")
            e.printStackTrace()
            """
            [{"title":"Erro de Rede/Exceção","description":"Falha ao comunicar com o serviço de IA: ${e.message?.take(100)}","recommendation":"Verifique sua conexão de internet."}]
            """.trimIndent()
        }
    }
}

// ... (initializePlatformManager e doInitIosPlatformManager permanecem os mesmos)
            [
              {
                "title": "Insight Placeholder iOS (Manager)",
                "description": "Implementação com Firebase AI SDK para iOS pendente.",
                "recommendation": "Verifique a interoperação Kotlin/Native com Swift."
              }
            ]
        """.trimIndent()
    }
}

// Implementação de initializePlatformManager para iOS
internal actual fun initializePlatformManager(): PlatformGeminiManager {
    return PlatformGeminiManager() // Retorna a instância do iOS
}

/**
 * Função wrapper para ser chamada do Swift para inicializar o PlatformGeminiManager para iOS.
 * Isso garante que o singleton GeminiServiceFactory.getInstance() possa obter o manager.
 */
@Suppress("unused") // Usado pelo código Swift
fun doInitIosPlatformManager() {
    // Acessar GeminiServiceFactory.getInstance() força a inicialização
    // do platformManager através de initializePlatformManager() se ainda não tiver sido feito.
    GeminiServiceFactory.getInstance()
}
