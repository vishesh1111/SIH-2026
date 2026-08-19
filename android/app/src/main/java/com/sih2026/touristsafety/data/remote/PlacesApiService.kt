package com.sih2026.touristsafety.data.remote

import com.sih2026.touristsafety.domain.model.NearbyPlace
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("api/places/")
    suspend fun getNearbyPlaces(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): List<NearbyPlace>
}
