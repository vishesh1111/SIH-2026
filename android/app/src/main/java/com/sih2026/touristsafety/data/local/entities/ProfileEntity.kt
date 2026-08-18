package com.sih2026.touristsafety.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val nationality: String,
    val gender: String,
    val phone: String,
    val email: String,
    val passportNumber: String?,
    val visaNumber: String?,
    val profilePhotoUrl: String?,
    val createdAt: Long
)
