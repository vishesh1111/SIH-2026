package com.sih2026.touristsafety.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sih2026.touristsafety.data.local.entities.ChatMessageEntity
import com.sih2026.touristsafety.data.local.entities.DisasterAlertEntity
import com.sih2026.touristsafety.data.local.entities.DocumentEntity
import com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity
import com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity
import com.sih2026.touristsafety.data.local.entities.IncidentEntity
import com.sih2026.touristsafety.data.local.entities.ProfileEntity
import com.sih2026.touristsafety.data.local.entities.ReceivedSOSAlertEntity

@Database(
    entities = [
        ProfileEntity::class,
        EmergencyContactEntity::class,
        GeofenceZoneEntity::class,
        IncidentEntity::class,
        DisasterAlertEntity::class,
        DocumentEntity::class,
        ChatMessageEntity::class,
        ReceivedSOSAlertEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TouristSafetyDatabase : RoomDatabase() {
    abstract fun profileDao(): com.sih2026.touristsafety.data.local.dao.ProfileDao
    abstract fun emergencyContactDao(): com.sih2026.touristsafety.data.local.dao.EmergencyContactDao
    abstract fun geofenceZoneDao(): com.sih2026.touristsafety.data.local.dao.GeofenceZoneDao
    abstract fun incidentDao(): com.sih2026.touristsafety.data.local.dao.IncidentDao
    abstract fun disasterAlertDao(): com.sih2026.touristsafety.data.local.dao.DisasterAlertDao
    abstract fun documentDao(): com.sih2026.touristsafety.data.local.dao.DocumentDao
    abstract fun chatMessageDao(): com.sih2026.touristsafety.data.local.dao.ChatMessageDao
    abstract fun receivedSOSAlertDao(): com.sih2026.touristsafety.data.local.dao.ReceivedSOSAlertDao
}
