package com.sih2026.touristsafety.presentation.screens.crowd

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Marker as OsmMarker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrowdDensityMapScreenWithViewModel(
    viewModel: CrowdDensityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                try {
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
                    ).addOnSuccessListener { location ->
                        if (location != null) {
                            viewModel.updateCenterLocation(LatLng(location.latitude, location.longitude))
                        }
                    }
                } catch (_: SecurityException) {}
            }
        }
    )

    LaunchedEffect(Unit) {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            try {
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateCenterLocation(LatLng(location.latitude, location.longitude))
                    }
                }
            } catch (_: SecurityException) {}
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
            controller.setZoom(17.0)
            controller.setCenter(GeoPoint(uiState.centerLocation.latitude, uiState.centerLocation.longitude))
        }
    }

    var showDensityInfo by remember { mutableStateOf(true) }
    val hasCenteredMap = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { map ->
                map.overlays.clear()
                
                uiState.densityInfo?.let { densityInfo ->
                    val zoneColor = calculateDensityZone(densityInfo.gpsPoints).color.toInt()
                    val polygon = Polygon()
                    val center = GeoPoint(uiState.centerLocation.latitude, uiState.centerLocation.longitude)
                    val pts = ArrayList<GeoPoint>()
                    val radiusMeters = 200.0
                    for (i in 0..36) {
                        val angle = (i * 10) * Math.PI / 180.0
                        val radiusDeg = radiusMeters / 111000.0
                        val ptLat = center.latitude + radiusDeg * kotlin.math.cos(angle)
                        val ptLon = center.longitude + radiusDeg * kotlin.math.sin(angle) / kotlin.math.cos(center.latitude * Math.PI / 180.0)
                        pts.add(GeoPoint(ptLat, ptLon))
                    }
                    polygon.points = pts
                    polygon.fillPaint.color = zoneColor
                    polygon.outlinePaint.color = zoneColor
                    polygon.outlinePaint.strokeWidth = 2f
                    map.overlays.add(polygon)
                }

                // Draw secondary hotspots
                uiState.hotspots.forEach { hotspot ->
                    val hZoneColor = calculateDensityZone(hotspot.peopleCount).color.toInt()
                    val hPolygon = Polygon()
                    val hCenter = GeoPoint(hotspot.location.latitude, hotspot.location.longitude)
                    val hPts = ArrayList<GeoPoint>()
                    val hRadiusMeters = 100.0 // Slightly smaller radius for secondary hotspots
                    for (i in 0..36) {
                        val angle = (i * 10) * Math.PI / 180.0
                        val radiusDeg = hRadiusMeters / 111000.0
                        val ptLat = hCenter.latitude + radiusDeg * kotlin.math.cos(angle)
                        val ptLon = hCenter.longitude + radiusDeg * kotlin.math.sin(angle) / kotlin.math.cos(hCenter.latitude * Math.PI / 180.0)
                        hPts.add(GeoPoint(ptLat, ptLon))
                    }
                    hPolygon.points = hPts
                    hPolygon.fillPaint.color = hZoneColor
                    hPolygon.outlinePaint.color = hZoneColor
                    hPolygon.outlinePaint.strokeWidth = 2f
                    map.overlays.add(hPolygon)
                    
                    // Add a small label marker in the center of the hotspot
                    val hMarker = OsmMarker(map)
                    hMarker.position = hCenter
                    hMarker.title = hotspot.name
                    hMarker.snippet = "${hotspot.peopleCount} people detected"
                    
                    // Set a modern circular dot icon instead of the default green arrow
                    val iconDrawable = androidx.core.content.ContextCompat.getDrawable(context, com.sih2026.touristsafety.R.drawable.ic_hotspot_dot)
                    // Make the icon color opaque (remove the 0x40 alpha)
                    val opaqueColor = hZoneColor or 0xFF000000.toInt()
                    iconDrawable?.setTint(opaqueColor)
                    hMarker.icon = iconDrawable
                    
                    // Center the anchor on the dot
                    hMarker.setAnchor(OsmMarker.ANCHOR_CENTER, OsmMarker.ANCHOR_CENTER)
                    
                    map.overlays.add(hMarker)
                }

                if (!hasCenteredMap.value && uiState.centerLocation.latitude != 18.9220) {
                    map.controller.setCenter(GeoPoint(uiState.centerLocation.latitude, uiState.centerLocation.longitude))
                    hasCenteredMap.value = true
                }
                
                map.invalidate()
            }
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        TopAppBar(
            title = { 
                Text(
                    "Crowd Density Monitor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refreshData() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
                IconButton(onClick = { showDensityInfo = !showDensityInfo }) {
                    Icon(
                        if (showDensityInfo) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showDensityInfo) "Hide Info" else "Show Info"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            modifier = Modifier.statusBarsPadding()
        )

        if (showDensityInfo && uiState.densityInfo != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                uiState.densityInfo?.let { densityInfo ->
                    CrowdDensityInfoCard(
                        densityInfo = densityInfo,
                        lastUpdateTime = uiState.lastUpdateTime,
                        onRecenter = {
                            mapView.controller.animateTo(GeoPoint(uiState.centerLocation.latitude, uiState.centerLocation.longitude))
                        }
                    )
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

@Composable
private fun CrowdDensityInfoCard(
    densityInfo: DensityInfo,
    lastUpdateTime: Long,
    onRecenter: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    val formattedTime = remember(lastUpdateTime) {
        timeFormatter.format(Date(lastUpdateTime))
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Live Area Density",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Updated: $formattedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = onRecenter,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider()

            RiskStatusBadge(densityInfo = densityInfo)

            StatCard(
                icon = Icons.Default.People,
                label = "People Around You",
                value = "~${densityInfo.estimatedPeople}",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            )

            DensityProgressIndicator(
                percentage = densityInfo.densityPercentage,
                label = "Area Capacity"
            )

            if (densityInfo.recommendation.isNotEmpty()) {
                RecommendationBox(
                    recommendation = densityInfo.recommendation,
                    riskLevel = densityInfo.riskLevel
                )
            }
        }
    }
}

