package com.sih2026.touristsafety.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceZoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZone(zone: GeofenceZoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZones(zones: List<GeofenceZoneEntity>)

    @Query("SELECT * FROM geofence_zones WHERE isActive = 1")
    fun getActiveZones(): Flow<List<GeofenceZoneEntity>>

    @Query("SELECT * FROM geofence_zones WHERE isActive = 1")
    suspend fun getActiveZonesList(): List<GeofenceZoneEntity>

    @Query("SELECT * FROM geofence_zones")
    fun getAllZones(): Flow<List<GeofenceZoneEntity>>
}
