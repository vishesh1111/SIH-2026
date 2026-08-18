package com.sih2026.touristsafety.presentation.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.sih2026.touristsafety.domain.model.NearbyPlace
import com.sih2026.touristsafety.domain.model.TouristLocation
import com.sih2026.touristsafety.data.local.entities.GeofenceZoneEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouristMapScreen(
    viewModel: TouristMapViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val userLocation by viewModel.userLocation.collectAsState()
    val nearbyPlaces by viewModel.nearbyPlaces.collectAsState()
    val nearbyTourists by viewModel.nearbyTourists.collectAsState()
    val dangerZones by viewModel.dangerZones.collectAsState()
    val isShowingTourists by viewModel.isShowingTourists.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    val bottomSheetState = rememberBottomSheetScaffoldState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation ?: LatLng(28.6139, 77.2090), 14f)
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetPeekHeight = 120.dp,
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { viewModel.selectTab(0) }, text = { Text("Nearby") })
                    Tab(selected = selectedTab == 1, onClick = { viewModel.selectTab(1) }, text = { Text("Tourists") })
                    Tab(selected = selectedTab == 2, onClick = { viewModel.selectTab(2) }, text = { Text("Zones") })
                }
                
                when (selectedTab) {
                    0 -> NearbyPlacesList(nearbyPlaces)
                    1 -> TouristsList(nearbyTourists) { viewModel.connectWithTourist(it) }
                    2 -> DangerZonesList(dangerZones)
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Tourist Map") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tourists", style = MaterialTheme.typography.labelSmall)
                        Switch(
                            checked = isShowingTourists,
                            onCheckedChange = { viewModel.toggleTouristVisibility() }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // User Location
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "You are here"
                    )
                }

                // Places
                nearbyPlaces.forEach { place ->
                    val color = when (place.type) {
                        "monument" -> 210f // Blue
                        "hospital" -> 120f // Green
                        "police" -> 240f // Navy/Dark Blue
                        "hotel" -> 270f // Purple
                        else -> 0f
                    }
                    Marker(
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        title = place.name,
                        snippet = place.type,
                        // Not using icon generator here for simplicity, fallback to color
                        // In a real app we'd use BitmapDescriptorFactory
                    )
                }

                // Danger Zones
                dangerZones.forEach { zone ->
                    Circle(
                        center = LatLng(zone.latitude, zone.longitude),
                        radius = zone.radius.toDouble(),
                        fillColor = Color(0x40FF0000), // Semi-transparent red
                        strokeColor = Color.Red,
                        strokeWidth = 2f
                    )
                }

                // Tourists
                if (isShowingTourists) {
                    nearbyTourists.forEach { tourist ->
                        Marker(
                            state = MarkerState(position = LatLng(tourist.latitude, tourist.longitude)),
                            title = tourist.name,
                            snippet = tourist.nationality
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    userLocation?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.LocationOn, "Center")
            }
        }
    }
}

@Composable
fun NearbyPlacesList(places: List<NearbyPlace>) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(places) { place ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(place.type) {
                            "hospital" -> Icons.Default.LocalHospital
                            "police" -> Icons.Default.LocalPolice
                            "hotel" -> Icons.Default.Hotel
                            else -> Icons.Default.Place
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(place.name, style = MaterialTheme.typography.titleMedium)
                        Text("${place.distance} km • ${place.rating} ★", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun TouristsList(tourists: List<TouristLocation>, onConnect: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(tourists) { tourist ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tourist.name, style = MaterialTheme.typography.titleMedium)
                        Text(tourist.nationality, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(onClick = { onConnect(tourist.id) }) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}

@Composable
fun DangerZonesList(zones: List<GeofenceZoneEntity>) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(zones) { zone ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(zone.name, style = MaterialTheme.typography.titleMedium)
                        Text("Level ${zone.severity} Danger", style = MaterialTheme.typography.bodyMedium, color = Color.Red)
                    }
                }
            }
        }
    }
}
