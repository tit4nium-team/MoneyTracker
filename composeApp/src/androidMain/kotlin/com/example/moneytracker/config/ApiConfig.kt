package com.example.moneytracker.config

import com.example.moneytracker.BuildConfig

internal actual object ApiConfig {
    // GEMINI_API_KEY será injetado via BuildConfig a partir do local.properties
    actual val GEMINI_API_KEY: String = BuildConfig.GEMINI_API_KEY

    // Para Android, GEMINI_API_KEY_IOS não é usado diretamente, mas precisa ser declarado.
    // Poderia ser igual ao GEMINI_API_KEY ou um valor placeholder se a chave for diferente por plataforma.
    actual val GEMINI_API_KEY_IOS: String = BuildConfig.GEMINI_API_KEY
}
