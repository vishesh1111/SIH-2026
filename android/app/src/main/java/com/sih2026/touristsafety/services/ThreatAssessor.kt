package com.sih2026.touristsafety.services

enum class ThreatLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class ThreatAssessment(
    val score: Int,
    val level: ThreatLevel,
    val shouldTriggerSOS: Boolean,
    val shouldPromptCheckIn: Boolean,
    val triggerReason: String = ""
)

object ThreatAssessor {
    fun assessThreat(
        screamDetected: Boolean,
        gender: String,
        distressLevel: Float,
        isNight: Boolean,
        isDangerZone: Boolean,
        keywordAlertTriggered: Boolean
    ): ThreatAssessment {
        var score = 0
        val reasons = mutableListOf<String>()

        if (keywordAlertTriggered) {
            score += 50
            reasons.add("Keyword Alert")
        }
        if (screamDetected) {
            score += 40
            reasons.add("Scream Detected")
        }
        if (gender.equals("female", ignoreCase = true)) {
            score += 20
            reasons.add("Female Safety Factor")
        }
        if (distressLevel > 0.5f) {
            score += 25
            reasons.add("High Distress")
        }
        if (isNight) {
            score += 10
            reasons.add("Night")
        }
        if (isDangerZone) {
            score += 15
            reasons.add("Danger Zone")
        }

        val level = when {
            score >= 60 -> ThreatLevel.CRITICAL
            score >= 40 -> ThreatLevel.HIGH
            score >= 20 -> ThreatLevel.MEDIUM
            else -> ThreatLevel.LOW
        }

        val triggerReason = if (reasons.isEmpty()) "None" else reasons.joinToString(" + ")

        return ThreatAssessment(
            score = score,
            level = level,
            shouldTriggerSOS = score >= 60,
            shouldPromptCheckIn = score >= 40 && score < 60,
            triggerReason = triggerReason
        )
    }

    fun assessThreat(
        screamDetected: Boolean,
        gender: String,
        distressLevel: Float,
        isNight: Boolean,
        isDangerZone: Boolean
    ): ThreatAssessment {
        return assessThreat(
            screamDetected = screamDetected,
            gender = gender,
            distressLevel = distressLevel,
            isNight = isNight,
            isDangerZone = isDangerZone,
            keywordAlertTriggered = false
        )
    }
}
