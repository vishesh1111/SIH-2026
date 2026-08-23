package com.sih2026.touristsafety.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.touristsafety.data.local.entities.ReceivedSOSAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivedSOSAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: ReceivedSOSAlertEntity)

    @Query("SELECT * FROM received_sos_alerts ORDER BY receivedAt DESC")
    fun getAllAlerts(): Flow<List<ReceivedSOSAlertEntity>>

    @Query("SELECT * FROM received_sos_alerts WHERE relayedToServer = 0")
    suspend fun getUnrelayedAlerts(): List<ReceivedSOSAlertEntity>

    @Query("UPDATE received_sos_alerts SET relayedToServer = 1 WHERE id = :alertId")
    suspend fun markAsRelayed(alertId: Int)

    @Query("SELECT COUNT(*) FROM received_sos_alerts WHERE victimUserIdHash = :userIdHash AND receivedAt > :sinceTimestamp")
    suspend fun countRecentAlerts(userIdHash: String, sinceTimestamp: Long): Int

    @Query("DELETE FROM received_sos_alerts WHERE id = :alertId")
    suspend fun deleteAlert(alertId: Int)
}
