package com.sih2026.touristsafety.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val metadata: String?,
    val createdAt: Long
)
