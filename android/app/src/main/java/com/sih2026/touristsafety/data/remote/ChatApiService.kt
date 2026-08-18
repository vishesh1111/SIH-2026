package com.sih2026.touristsafety.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(
    val message: String,
    val location: String?,
    val language: String
)

data class ActionButton(
    val label: String,
    val action: String
)

data class ChatResponse(
    val text: String,
    val image_urls: List<String>,
    val action_buttons: List<ActionButton>
)

interface ChatApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>
}
