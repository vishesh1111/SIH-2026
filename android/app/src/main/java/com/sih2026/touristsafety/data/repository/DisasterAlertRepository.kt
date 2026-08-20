package com.sih2026.touristsafety.data.repository

import com.sih2026.touristsafety.data.local.dao.DisasterAlertDao
import com.sih2026.touristsafety.data.local.entities.DisasterAlertEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisasterAlertRepository @Inject constructor(
    private val alertDao: DisasterAlertDao
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun getAlerts(): Flow<List<DisasterAlertEntity>> {
        return alertDao.getAllAlerts()
    }
    
    fun getAlertsByType(type: String): Flow<List<DisasterAlertEntity>> {
        return alertDao.getAlertsByHazardType(type)
    }

    suspend fun refreshAlertsFromServer() = withContext(Dispatchers.IO) {
        try {
            // Calculate the date 7 days ago
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
            val startTime = dateFormat.format(calendar.time)

            // Fetch real earthquake alerts for India bounding box from USGS within the last 7 days
            val url = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&minmagnitude=3.5&minlatitude=6.0&maxlatitude=36.0&minlongitude=68.0&maxlongitude=98.0&starttime=$startTime"
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            
            val body = response.body?.string() ?: return@withContext
            val json = JSONObject(body)
            val features = json.getJSONArray("features")
            
            val entities = mutableListOf<DisasterAlertEntity>()
            
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                
                val place = properties.getString("place")
                // Strict check to ensure we only include India and exclude bordering countries in the bounding box
                if (!place.contains("India", ignoreCase = true)) continue
                
                val mag = properties.getDouble("mag")
                val time = properties.getLong("time")
                
                val severity = if (mag >= 7.0) "extreme" else if (mag >= 6.0) "severe" else "moderate"
                val instructions = if (mag >= 7.0) 
                    "Drop, cover, and hold on! Move away from windows and exterior walls. Expect aftershocks." 
                else 
                    "Expect minor shaking. Be aware of objects that could fall."

                entities.add(
                    DisasterAlertEntity(
                        id = feature.getString("id"),
                        capIdentifier = feature.getString("id"),
                        hazardType = "Earthquake",
                        severity = severity,
                        urgency = "immediate",
                        headline = "Magnitude $mag Earthquake - $place",
                        description = "A magnitude $mag earthquake occurred near $place. Please stay alert.",
                        instructions = instructions,
                        affectedStates = place,
                        source = "USGS",
                        expiresAt = time + (7 * 24 * 60 * 60 * 1000L), // Keep alert active for 7 days
                        createdAt = time
                    )
                )
            }
            
            // Note: Since Open-Meteo & GDACS API would be used here as well in a full implementation, 
            // for this demo we are using USGS Earthquakes as it requires absolutely zero API keys 
            // and provides real live global data reliably.

            if (entities.isNotEmpty()) {
                alertDao.clearAllAlerts() // Clear dummy data
                alertDao.insertAlerts(entities) // Insert real live data
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchWeather(lat: Double, lng: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current=temperature_2m,apparent_temperature,precipitation,weather_code"
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            
            val json = JSONObject(body)
            val current = json.getJSONObject("current")
            val temp = current.getDouble("temperature_2m")
            val feelsLike = current.getDouble("apparent_temperature")
            val precip = current.getDouble("precipitation")
            val code = current.getInt("weather_code")
            
            WeatherData(temp, feelsLike, precip, code)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class WeatherData(
    val temperature: Double,
    val feelsLike: Double, // Heat prediction
    val precipitation: Double, // Rainfall mm
    val weatherCode: Int
)
