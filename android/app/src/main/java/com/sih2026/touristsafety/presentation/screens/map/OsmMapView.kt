package com.sih2026.touristsafety.presentation.screens.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.sih2026.touristsafety.R
import com.google.android.gms.maps.model.LatLng
import com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity
import com.sih2026.touristsafety.domain.model.NearbyPlace
import com.sih2026.touristsafety.domain.model.TouristLocation
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    userLocation: LatLng?,
    nearbyPlaces: List<NearbyPlace>,
    dangerZones: List<GeofenceZoneEntity>,
    nearbyTourists: List<TouristLocation>,
    isShowingTourists: Boolean,
    onRecenterRequested: () -> Unit = {}
) {
    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            maxZoomLevel = 22.0
            minZoomLevel = 3.0
            controller.setZoom(15.0)
            // Default center (Delhi) until GPS is available
            controller.setCenter(GeoPoint(28.6139, 77.2090))
        }
    }
    
    // Remember if we've already centered the map so we don't snap back while user is panning
    val hasCenteredMap = remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { map ->
                map.overlays.clear()
                
                // Places
                nearbyPlaces.forEach { place ->
                    val placeMarker = Marker(map)
                    placeMarker.position = GeoPoint(place.latitude, place.longitude)
                    placeMarker.title = place.name
                    placeMarker.snippet = place.type
                    
                    val iconRes = when (place.type.lowercase()) {
                        "hospital" -> R.drawable.ic_hospital
                        "police" -> R.drawable.ic_police
                        "monument" -> R.drawable.ic_monument
                        "food" -> R.drawable.ic_food
                        "shopping" -> R.drawable.ic_shopping
                        "hotel" -> R.drawable.ic_hotel
                        else -> R.drawable.ic_place
                    }
                    placeMarker.icon = ContextCompat.getDrawable(context, iconRes)
                    
                    map.overlays.add(placeMarker)
                }

                // Danger Zones
                dangerZones.forEach { zone ->
                    val polygon = Polygon()
                    val center = GeoPoint(zone.latitude, zone.longitude)
                    val pts = ArrayList<GeoPoint>()
                    for (i in 0..36) {
                        val angle = (i * 10) * Math.PI / 180.0
                        val radiusDeg = zone.radius / 111000.0
                        val ptLat = center.latitude + radiusDeg * Math.cos(angle)
                        val ptLon = center.longitude + radiusDeg * Math.sin(angle) / Math.cos(center.latitude * Math.PI / 180.0)
                        pts.add(GeoPoint(ptLat, ptLon))
                    }
                    polygon.points = pts
                    polygon.fillPaint.color = 0x40FF0000
                    polygon.outlinePaint.color = 0xFFFF0000.toInt()
                    polygon.outlinePaint.strokeWidth = 2f
                    map.overlays.add(polygon)
                }

                // Tourists
                if (isShowingTourists) {
                    nearbyTourists.forEach { tourist ->
                        val tMarker = Marker(map)
                        tMarker.position = GeoPoint(tourist.latitude, tourist.longitude)
                        tMarker.title = tourist.name
                        tMarker.snippet = tourist.nationality
                        tMarker.icon = ContextCompat.getDrawable(context, R.drawable.ic_tourist)
                        map.overlays.add(tMarker)
                    }
                }
                
                // Add User Marker last so it's on top
                userLocation?.let {
                    val point = GeoPoint(it.latitude, it.longitude)
                    val userMarker = Marker(map)
                    userMarker.position = point
                    userMarker.title = "You are here"
                    userMarker.icon = ContextCompat.getDrawable(context, R.drawable.ic_user_location)
                    map.overlays.add(userMarker)
                    
                    if (!hasCenteredMap.value) {
                        map.controller.setZoom(18.0)
                        map.controller.setCenter(point)
                        hasCenteredMap.value = true
                    }
                }

                map.invalidate()
            }
        )
        
        FloatingActionButton(
            onClick = {
                // Re-fetch GPS location from parent, then animate
                onRecenterRequested()
                userLocation?.let {
                    val point = GeoPoint(it.latitude, it.longitude)
                    mapView.controller.setZoom(18.0)
                    mapView.controller.animateTo(point)
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.LocationOn, "Center")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }
}
