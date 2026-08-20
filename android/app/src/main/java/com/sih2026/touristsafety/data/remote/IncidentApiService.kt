package com.sih2026.touristsafety.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class AIAnalysisResult(
    val sceneDescription: String,
    val detectedObjects: List<String>,
    val potentialIncidentType: String,
    val severityAssessment: String
)

data class IncidentDto(
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val date: Long,
    val photos: List<String>
)

data class IncidentResponse(
    val id: String,
    val status: String,
    val message: String
)


