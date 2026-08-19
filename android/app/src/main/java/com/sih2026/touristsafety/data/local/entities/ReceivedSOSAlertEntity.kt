package com.sih2026.touristsafety.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "received_sos_alerts")
data class ReceivedSOSAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val victimUserIdHash: String,
    val latitude: Double,
    val longitude: Double,
    val sosType: Int,
    val sosTimestamp: Long,
    val receivedAt: Long = System.currentTimeMillis(),
    val relayedToServer: Boolean = false,
    val rssi: Int = 0
)
