package com.example.moneytracker.config

internal actual object ApiConfig {
    // Para iOS, GEMINI_API_KEY não é usado diretamente se a chave principal for GEMINI_API_KEY_IOS.
    // Pode ser um placeholder ou o mesmo valor se a chave for única.
    actual val GEMINI_API_KEY: String = "YOUR_IOS_GEMINI_API_KEY_IF_DIFFERENT_OR_PLACEHOLDER"

    // Esta é a chave principal que a implementação iOS usaria.
    // IMPORTANTE: Substitua "YOUR_IOS_GEMINI_API_KEY" pela sua chave de API real para iOS.
    // Você pode carregá-la de um arquivo de configuração nativo (plist) ou defini-la aqui.
    actual val GEMINI_API_KEY_IOS: String = "YOUR_IOS_GEMINI_API_KEY"
}
