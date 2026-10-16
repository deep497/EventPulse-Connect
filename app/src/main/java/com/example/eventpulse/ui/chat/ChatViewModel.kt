package com.example.eventpulse.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpulse.data.model.ChatMessage
import com.example.eventpulse.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        // Add user message
        val userMessage = ChatMessage(
            content = content,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _chatState.value = _chatState.value.copy(
            messages = _chatState.value.messages + userMessage
        )

        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isLoading = true, error = null)
            
            val result = chatRepository.sendMessage(content)
            result.fold(
                onSuccess = { reply ->
                    val aiMessage = ChatMessage(
                        content = reply,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _chatState.value = _chatState.value.copy(
                        isLoading = false,
                        messages = _chatState.value.messages + aiMessage
                    )
                },
                onFailure = { exception ->
                    _chatState.value = _chatState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to send message"
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _chatState.value = ChatState()
    }

    fun clearError() {
        _chatState.value = _chatState.value.copy(error = null)
    }
}
