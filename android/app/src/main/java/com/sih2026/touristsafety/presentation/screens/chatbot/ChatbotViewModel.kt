package com.sih2026.touristsafety.presentation.screens.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sih2026.touristsafety.data.remote.ActionButton
import com.sih2026.touristsafety.data.remote.ChatApiService
import com.sih2026.touristsafety.data.remote.ChatRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val actionButtons: List<ActionButton>? = null,
    val imageUrls: List<String>? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val chatApiService: ChatApiService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    init {
        loadHistory()
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    private fun loadHistory() {
        // Dummy welcome message
        _messages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "assistant",
                content = "Hello! I'm your AI travel assistant. I can help you with:\n" +
                        "• \uD83C\uDFDB\uFE0F Monument & tourist info\n" +
                        "• \uD83D\uDEE1\uFE0F Safety tips\n" +
                        "• \uD83D\uDDFA\uFE0F Navigation help\n" +
                        "• \uD83C\uDD98 Emergency guidance\n" +
                        "Ask me anything!"
            )
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = "user",
            content = text
        )

        _messages.value = _messages.value + userMessage
        _inputText.value = ""
        _isTyping.value = true

        viewModelScope.launch {
            try {
                val response = chatApiService.sendMessage(
                    ChatRequest(
                        message = text,
                        location = "Unknown", // You can update this to get actual location
                        language = "en"
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val assistantResponse = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = "assistant",
                        content = body.text,
                        actionButtons = body.action_buttons,
                        imageUrls = body.image_urls
                    )
                    _messages.value = _messages.value + assistantResponse
                } else {
                    val errorMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = "assistant",
                        content = "Sorry, I'm having trouble connecting right now."
                    )
                    _messages.value = _messages.value + errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = "assistant",
                    content = "Error: ${e.message}"
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isTyping.value = false
            }
        }
    }
}
