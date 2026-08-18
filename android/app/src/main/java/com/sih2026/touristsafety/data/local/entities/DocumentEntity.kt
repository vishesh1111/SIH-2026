package com.sih2026.touristsafety.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val docType: String,
    val docNumber: String,
    val fileUrl: String,
    val expiryDate: Long?,
    val verificationStatus: String,
    val metadata: String?,
    val createdAt: Long
)
