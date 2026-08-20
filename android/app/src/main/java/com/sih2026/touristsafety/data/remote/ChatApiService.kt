package com.sih2026.touristsafety.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val message: String,
    val location: String?,
    val language: String
)

@JsonClass(generateAdapter = true)
data class ActionButton(
    val label: String,
    val action: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val text: String,
    val image_urls: List<String>,
    val action_buttons: List<ActionButton>
)


