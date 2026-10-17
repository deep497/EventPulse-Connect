package com.example.eventpulse.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpulse.data.model.ChatMessage
import com.example.eventpulse.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val role: String = "USER", // "USER" or "ORGANIZER"
    val quickSuggestions: List<String> = emptyList(),
    val draftContext: Map<String, Any>? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    fun setRole(role: String, draftContext: Map<String, Any>? = null) {
        val suggestions = if (role == "ORGANIZER") {
            listOf(
                "Suggest a catchy title & price for a coding bootcamp",
                "Write a 2-paragraph concert description",
                "What is a competitive price for a weekend comedy show?",
                "Suggest an event schedule for a full-day tech conference"
            )
        } else {
            listOf(
                "Weekend music events",
                "Free tech workshops",
                "Outdoor sports and fitness activities",
                "Popular events under ₹500"
            )
        }
        _chatUiState.update { 
            it.copy(
                role = role, 
                quickSuggestions = suggestions,
                draftContext = draftContext
            ) 
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val userMessage = ChatMessage(
            content = content.trim(),
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        
        val currentHistory = _chatUiState.value.messages
        val currentRole = _chatUiState.value.role
        val currentDraft = _chatUiState.value.draftContext

        _chatUiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                message = content.trim(),
                role = currentRole,
                history = currentHistory,
                draftContext = currentDraft
            )
            result.fold(
                onSuccess = { reply ->
                    val aiMessage = ChatMessage(
                        content = reply,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _chatUiState.update {
                        it.copy(
                            isLoading = false,
                            messages = it.messages + aiMessage
                        )
                    }
                },
                onFailure = { exception ->
                    _chatUiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to get response"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _chatUiState.update { it.copy(messages = emptyList(), error = null) }
    }

    fun clearError() {
        _chatUiState.update { it.copy(error = null) }
    }
}
