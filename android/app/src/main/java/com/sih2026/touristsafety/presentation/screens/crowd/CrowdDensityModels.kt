package com.sih2026.touristsafety.presentation.screens.crowd

import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Risk level enumeration for crowd density
 */
enum class RiskLevel {
    LOW,      // < 40% capacity
    MEDIUM,   // 40-70% capacity
    HIGH,     // 70-90% capacity
    CRITICAL  // > 90% capacity
}

/**
 * Data class representing density analysis information
 */
data class DensityInfo(
    val riskLevel: RiskLevel,
    val estimatedPeople: Int,
    val densityPercentage: Int,
    val gpsPoints: Int,
    val recommendation: String,
    val areaName: String = "Gateway of India"
)

/**
 * Generate mock crowd data points around a central location
 * 
 * @param center The central LatLng point (e.g., tourist hotspot)
 * @param radiusMeters Radius in meters to generate points within
 * @param count Number of random points to generate
 * @return List of LatLng coordinates representing mock crowd GPS points
 */
fun generateMockCrowdData(
    center: LatLng,
    radiusMeters: Double = 200.0,
    count: Int = 85
): List<LatLng> {
    val points = mutableListOf<LatLng>()
    
    // Earth's radius in meters
    val earthRadius = 6371000.0
    
    // Convert radius from meters to degrees
    val radiusInDegrees = radiusMeters / earthRadius
    
    // Generate random points
    for (i in 0 until count) {
        // Random angle (0 to 2π)
        val angle = Random.nextDouble() * 2 * Math.PI
        
        // Random radius (using square root for uniform distribution)
        val randomRadius = radiusInDegrees * kotlin.math.sqrt(Random.nextDouble())
        
        // Calculate offset
        val deltaLat = randomRadius * cos(angle) * (180 / Math.PI)
        val deltaLng = randomRadius * sin(angle) * (180 / Math.PI) / cos(center.latitude * Math.PI / 180)
        
        // Create new point
        val newLat = center.latitude + deltaLat
        val newLng = center.longitude + deltaLng
        
        points.add(LatLng(newLat, newLng))
    }
    
    return points
}

/**
 * Analyze mock density data and return risk information
 * 
 * @param pointCount Number of GPS points detected
 * @return DensityInfo object with risk level and recommendations
 */
fun analyzeMockDensity(pointCount: Int): DensityInfo {
    // Simulate conversion: each GPS point represents ~5 people
    val estimatedPeople = pointCount * 5
    
    // Simulate max capacity for the area
    val maxCapacity = 500
    val densityPercentage = ((estimatedPeople.toFloat() / maxCapacity) * 100).toInt()
    
    // Determine risk level
    val riskLevel = when {
        densityPercentage >= 90 -> RiskLevel.CRITICAL
        densityPercentage >= 70 -> RiskLevel.HIGH
        densityPercentage >= 40 -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }
    
    // Generate contextual recommendation
    val recommendation = when (riskLevel) {
        RiskLevel.CRITICAL -> 
            "⚠️ Density is critically high near the temple exit. Stampede risk detected. Please use the alternate South Gate immediately and move slowly."
        RiskLevel.HIGH -> 
            "⚠️ High crowd density detected. Consider using alternate routes. Avoid the main entrance and stay aware of your surroundings."
        RiskLevel.MEDIUM -> 
            "Moderate crowd levels. Stay alert and keep your belongings secure. Main pathways are accessible."
        RiskLevel.LOW -> 
            "Area is safe with low crowd density. Enjoy your visit! All routes are open and accessible."
    }
    
    return DensityInfo(
        riskLevel = riskLevel,
        estimatedPeople = estimatedPeople,
        densityPercentage = densityPercentage.coerceIn(0, 100),
        gpsPoints = pointCount,
        recommendation = recommendation
    )
}

/**
 * Generate mock hotspot locations (for future enhancements)
 */
fun generateMockHotspots(center: LatLng): List<Hotspot> {
    return listOf(
        Hotspot(
            location = LatLng(center.latitude + 0.0005, center.longitude + 0.0003),
            name = "Main Entrance",
            peopleCount = 120,
            riskLevel = RiskLevel.CRITICAL
        ),
        Hotspot(
            location = LatLng(center.latitude - 0.0003, center.longitude + 0.0005),
            name = "South Gate",
            peopleCount = 45,
            riskLevel = RiskLevel.LOW
        ),
        Hotspot(
            location = LatLng(center.latitude + 0.0002, center.longitude - 0.0004),
            name = "Ticket Counter",
            peopleCount = 85,
            riskLevel = RiskLevel.HIGH
        )
    )
}

/**
 * Data class representing a crowd hotspot
 */
data class Hotspot(
    val location: LatLng,
    val name: String,
    val peopleCount: Int,
    val riskLevel: RiskLevel
)

/**
 * Simulate real-time density updates (for future WebSocket/API integration)
 */
class MockDensityStream {
    private var currentCount = 85
    
    fun getNextDensityUpdate(): DensityInfo {
        // Simulate fluctuation (+/- 5 points)
        currentCount += Random.nextInt(-5, 6)
        currentCount = currentCount.coerceIn(20, 120)
        
        return analyzeMockDensity(currentCount)
    }
}

/**
 * Density heat zones for color-coded map overlays
 */
enum class DensityZone(val color: Long, val label: String) {
    SAFE(0x4000FF00, "Safe Zone"),
    MODERATE(0x40FFFF00, "Moderate"),
    CROWDED(0x40FFA500, "Crowded"),
    DANGER(0x40FF0000, "Danger Zone")
}

/**
 * Calculate density zone based on people count in area
 */
fun calculateDensityZone(peopleCount: Int): DensityZone {
    return when {
        peopleCount < 30 -> DensityZone.SAFE
        peopleCount < 60 -> DensityZone.MODERATE
        peopleCount < 90 -> DensityZone.CROWDED
        else -> DensityZone.DANGER
    }
}
