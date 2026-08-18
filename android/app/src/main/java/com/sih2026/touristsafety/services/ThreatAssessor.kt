package com.sih2026.touristsafety.services

enum class ThreatLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class ThreatAssessment(
    val score: Int,
    val level: ThreatLevel,
    val shouldTriggerSOS: Boolean,
    val shouldPromptCheckIn: Boolean
)

object ThreatAssessor {
    fun assessThreat(
        screamDetected: Boolean, 
        gender: String, 
        distressLevel: Float, 
        isNight: Boolean, 
        isDangerZone: Boolean
    ): ThreatAssessment {
        var score = 0
        if (screamDetected) score += 40
        if (gender == "female") score += 20
        if (distressLevel > 0.5f) score += 25
        if (isNight) score += 10
        if (isDangerZone) score += 15

        val level = when {
            score >= 60 -> ThreatLevel.CRITICAL
            score >= 40 -> ThreatLevel.HIGH
            score >= 20 -> ThreatLevel.MEDIUM
            else -> ThreatLevel.LOW
        }

        return ThreatAssessment(
            score = score,
            level = level,
            shouldTriggerSOS = score >= 60,
            shouldPromptCheckIn = score >= 40 && score < 60
        )
    }
}
