package com.sih2026.touristsafety.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.sih2026.touristsafety.di.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InactivityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val IS_ENABLED = booleanPreferencesKey("inactivity_enabled")
        private val THRESHOLD_MINUTES = intPreferencesKey("inactivity_threshold_minutes")
        private val STATIONARY_DETECTION = booleanPreferencesKey("stationary_detection")
        private val UNUSUAL_MOVEMENT = booleanPreferencesKey("unusual_movement")
        private val SPEED_ANOMALY = booleanPreferencesKey("speed_anomaly")
    }

    val isEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ENABLED] ?: true
    }

    val thresholdMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THRESHOLD_MINUTES] ?: 30
    }

    val stationaryDetection: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[STATIONARY_DETECTION] ?: true
    }

    val unusualMovement: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[UNUSUAL_MOVEMENT] ?: true
    }

    val speedAnomaly: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SPEED_ANOMALY] ?: true
    }

    suspend fun setEnabled(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_ENABLED] = value
        }
    }

    suspend fun setThresholdMinutes(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[THRESHOLD_MINUTES] = value
        }
    }

    suspend fun setStationaryDetection(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[STATIONARY_DETECTION] = value
        }
    }

    suspend fun setUnusualMovement(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[UNUSUAL_MOVEMENT] = value
        }
    }

    suspend fun setSpeedAnomaly(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SPEED_ANOMALY] = value
        }
    }
}
