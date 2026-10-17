package com.example.eventpulse.data.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String = "",
    val isFromUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatHistoryItem(
    val content: String,
    val isFromUser: Boolean
)

data class ChatRequest(
    val message: String,
    val role: String,
    val history: List<ChatHistoryItem> = emptyList(),
    val draftContext: Map<String, Any>? = null
)

data class ChatResponse(
    val reply: String = ""
)
