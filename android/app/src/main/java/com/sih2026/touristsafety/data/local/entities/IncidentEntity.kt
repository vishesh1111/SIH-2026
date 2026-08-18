package com.sih2026.touristsafety.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val incidentType: String,
    val description: String,
    val aiStructuredFir: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val state: String,
    val district: String,
    val status: String,
    val createdAt: Long
)
