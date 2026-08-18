package com.sih2026.touristsafety.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geofence_zones")
data class GeofenceZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val zoneType: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val description: String,
    val alertMessage: String,
    val severity: Int,
    val state: String,
    val isActive: Boolean
)
