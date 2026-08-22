package com.sih2026.touristsafety.presentation.screens.track

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            viewModel.updateUserLocation(LatLng(location.latitude, location.longitude))
                        }
                    }
            } catch (e: SecurityException) { }
        }
    }
    
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateUserLocation(LatLng(location.latitude, location.longitude))
                    }
                }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            maxZoomLevel = 22.0
            minZoomLevel = 3.0
            controller.setZoom(15.0)
        }
    }

    var hasCenteredMap by remember { mutableStateOf(false) }
    var showJourneySheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tracks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView },
                update = { map ->
                    if (uiState.visitedLocations.isNotEmpty()) {
                        map.overlays.clear()
                        
                        val geoPoints = uiState.visitedLocations.map { loc ->
                            GeoPoint(loc.coordinates.latitude, loc.coordinates.longitude)
                        }

                        // Draw Polyline connecting the points
                        val polyline = Polyline()
                        polyline.setPoints(geoPoints)
                        polyline.outlinePaint.color = Color.parseColor("#1E88E5") // Blue line
                        polyline.outlinePaint.strokeWidth = 8f
                        polyline.outlinePaint.strokeCap = Paint.Cap.ROUND
                        polyline.outlinePaint.style = Paint.Style.STROKE
                        polyline.outlinePaint.isAntiAlias = true
                        
                        map.overlays.add(polyline)

                        // Draw markers for each point
                        uiState.visitedLocations.forEachIndexed { index, loc ->
                            val marker = Marker(map)
                            marker.position = GeoPoint(loc.coordinates.latitude, loc.coordinates.longitude)
                            marker.title = "${index + 1}. ${loc.name}"
                            marker.snippet = "${loc.arrivalTime} - ${loc.departureTime} (${loc.duration})"
                            
                            val iconDrawable = androidx.core.content.ContextCompat.getDrawable(context, com.sih2026.touristsafety.R.drawable.ic_place)?.mutate()
                            iconDrawable?.setTint(android.graphics.Color.parseColor("#E53935")) // Red color for the map pin
                            marker.icon = iconDrawable
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            
                            map.overlays.add(marker)
                        }

                        if (!hasCenteredMap) {
                            map.controller.setCenter(geoPoints.first())
                            hasCenteredMap = true
                        }
                        
                        map.invalidate()
                    }
                }
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.visitedLocations.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clickable { showJourneySheet = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Today's Journey",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${uiState.visitedLocations.size} places visited",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
        
        if (showJourneySheet) {
            ModalBottomSheet(
                onDismissRequest = { showJourneySheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Journey Timeline",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(uiState.visitedLocations.size) { index ->
                            val loc = uiState.visitedLocations[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    if (index < uiState.visitedLocations.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(48.dp)
                                                .padding(vertical = 4.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        loc.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${loc.arrivalTime} to ${loc.departureTime} • ${loc.duration}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }
}
