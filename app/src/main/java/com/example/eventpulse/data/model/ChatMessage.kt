package com.example.eventpulse.data.model

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val isFromUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
