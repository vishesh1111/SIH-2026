package com.sih2026.touristsafety.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disaster_alerts")
data class DisasterAlertEntity(
    @PrimaryKey val id: String,
    val capIdentifier: String,
    val hazardType: String,
    val severity: String,
    val urgency: String,
    val headline: String,
    val description: String,
    val instructions: String?,
    val affectedStates: String,
    val source: String,
    val expiresAt: Long,
    val createdAt: Long
)
