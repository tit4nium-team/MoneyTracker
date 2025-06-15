package com.example.moneytracker.model

data class ChatMessage(
    val id: String = "",
    val content: String,
    val isFromUser: Boolean,
    val timestamp: String
) 