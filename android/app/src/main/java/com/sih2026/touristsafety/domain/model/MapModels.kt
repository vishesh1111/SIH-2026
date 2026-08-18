package com.sih2026.touristsafety.domain.model

data class TouristLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val nationality: String
)

data class NearbyPlace(
    val id: String,
    val name: String,
    val type: String, // monument, hospital, police, hotel
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    val rating: Float
)


