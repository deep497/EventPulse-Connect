package com.example.eventpulse.data.repository

import com.example.eventpulse.data.model.ChatHistoryItem
import com.example.eventpulse.data.model.ChatMessage
import com.example.eventpulse.data.model.ChatRequest
import com.example.eventpulse.data.remote.EventPulseApi

class ChatRepository(
    private val api: EventPulseApi
) {
    
    suspend fun sendMessage(
        message: String,
        role: String = "USER",
        history: List<ChatMessage> = emptyList(),
        draftContext: Map<String, Any>? = null
    ): Result<String> {
        return try {
            val historyItems = history.map { 
                ChatHistoryItem(content = it.content, isFromUser = it.isFromUser)
            }
            val request = ChatRequest(
                message = message,
                role = role,
                history = historyItems,
                draftContext = draftContext
            )
            val response = api.sendChatMessage(request)
            if (response.isSuccessful && response.body() != null) {
                val reply = response.body()!!.reply
                Result.success(reply.ifBlank { "No response received" })
            } else {
                Result.failure(Exception("Failed to send message: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
