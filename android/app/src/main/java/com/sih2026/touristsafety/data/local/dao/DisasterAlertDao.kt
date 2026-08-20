package com.sih2026.touristsafety.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.touristsafety.data.local.entities.DisasterAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DisasterAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: DisasterAlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<DisasterAlertEntity>)

    @Query("SELECT * FROM disaster_alerts ORDER BY createdAt DESC")
    fun getActiveAlerts(): Flow<List<DisasterAlertEntity>>

    @Query("SELECT * FROM disaster_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<DisasterAlertEntity>>

    @Query("SELECT * FROM disaster_alerts WHERE hazardType = :type ORDER BY createdAt DESC")
    fun getAlertsByHazardType(type: String): Flow<List<DisasterAlertEntity>>

    @Query("DELETE FROM disaster_alerts")
    suspend fun clearAllAlerts()
}
