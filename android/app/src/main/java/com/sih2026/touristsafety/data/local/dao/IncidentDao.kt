package com.sih2026.touristsafety.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sih2026.touristsafety.data.local.entities.IncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>
}
