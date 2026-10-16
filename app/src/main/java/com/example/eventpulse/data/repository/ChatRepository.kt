package com.example.eventpulse.data.repository

import com.example.eventpulse.data.remote.EventPulseApi

class ChatRepository(
    private val api: EventPulseApi
) {
    
    suspend fun sendMessage(message: String): Result<String> {
        return try {
            val body = mapOf("message" to message)
            val response = api.sendChatMessage(body)
            if (response.isSuccessful && response.body() != null) {
                val reply = response.body()!!["reply"] ?: "No response"
                Result.success(reply)
            } else {
                Result.failure(Exception("Failed to send message: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
