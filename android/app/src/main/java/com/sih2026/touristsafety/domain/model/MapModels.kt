package com.sih2026.touristsafety.domain.model

import com.squareup.moshi.JsonClass

data class TouristLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val nationality: String
)

@JsonClass(generateAdapter = true)
data class NearbyPlace(
    val id: String,
    val name: String,
    val type: String, // monument, hospital, police, hotel
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    val rating: Float
)

