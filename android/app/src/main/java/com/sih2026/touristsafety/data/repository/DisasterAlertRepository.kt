package com.sih2026.touristsafety.data.repository

import com.sih2026.touristsafety.data.local.dao.DisasterAlertDao
import com.sih2026.touristsafety.data.local.entities.DisasterAlertEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisasterAlertRepository @Inject constructor(
    private val alertDao: DisasterAlertDao
) {
    fun getAlerts(): Flow<List<DisasterAlertEntity>> {
        return alertDao.getAllAlerts()
    }
    
    fun getAlertsByType(type: String): Flow<List<DisasterAlertEntity>> {
        return alertDao.getAlertsByHazardType(type)
    }

    suspend fun refreshAlertsFromServer() {
        // In a real implementation, fetch from backend via Retrofit and save to Room
        // For now, we simulate inserting a dummy alert if none exist
        val dummyAlert = DisasterAlertEntity(
            id = "dummy_1",
            capIdentifier = "CAP_001",
            hazardType = "Cyclone",
            severity = "extreme",
            urgency = "immediate",
            headline = "Severe Cyclone Warning",
            description = "A severe cyclone is approaching the coastal areas.",
            instructions = "Stay indoors. Keep emergency kits ready.",
            affectedStates = "Maharashtra, Gujarat",
            source = "IMD",
            expiresAt = System.currentTimeMillis() + 86400000,
            createdAt = System.currentTimeMillis()
        )
        alertDao.insertAlerts(listOf(dummyAlert))
    }
}
