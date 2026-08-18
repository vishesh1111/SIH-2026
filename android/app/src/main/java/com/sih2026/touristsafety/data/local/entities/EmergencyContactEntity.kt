package com.sih2026.touristsafety.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val countryCode: String,
    val residentAddress: String?,
    val isPrimary: Boolean
)
