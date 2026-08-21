package com.sih2026.touristsafety.presentation.screens.connect

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.sih2026.touristsafety.presentation.screens.map.OsmMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConnectViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isSharing by viewModel.isSharing.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val otherTourists by viewModel.otherTourists.collectAsState()

    var selectedTouristToConnect by remember { mutableStateOf<com.sih2026.touristsafety.domain.model.TouristLocation?>(null) }
    
    selectedTouristToConnect?.let { tourist ->
        AlertDialog(
            onDismissRequest = { selectedTouristToConnect = null },
            icon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Connect with ${tourist.name}") },
            text = { Text("Would you like to send a connection request to ${tourist.name} located at ${tourist.nationality}?") },
            confirmButton = {
                TextButton(onClick = { 
                    selectedTouristToConnect = null
                    android.widget.Toast.makeText(context, "Connection request sent to ${tourist.name}!", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTouristToConnect = null }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                            viewModel.updateUserLocation(LatLng(location.latitude, location.longitude))
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
                        viewModel.updateUserLocation(LatLng(location.latitude, location.longitude))
                    }
                }
            } catch (_: SecurityException) {}
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    var isLocationServiceEnabled by remember { mutableStateOf(true) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                isLocationServiceEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                                           locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!isLocationServiceEnabled) {
        AlertDialog(
            onDismissRequest = { /* Force action */ },
            icon = { Icon(Icons.Default.LocationOff, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Location Services Disabled") },
            text = { Text("Your device's location (GPS) is turned off. Connect with Tourists requires location services to share your location. Please enable it in Settings.") },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) { Text("Turn On") }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) { Text("Go Back") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect with Tourists") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map Section (takes most of the screen)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                val userAddress by viewModel.userAddress.collectAsState()
                
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    userLocation = userLocation,
                    userAddress = userAddress,
                    nearbyPlaces = emptyList(),
                    dangerZones = emptyList(),
                    nearbyTourists = otherTourists,
                    isShowingTourists = true, // Always show tourists, even before sharing
                    onTouristClick = { clickedTourist ->
                        selectedTouristToConnect = clickedTourist
                    },
                    onRecenterRequested = {
                        try {
                            fusedLocationClient.getCurrentLocation(
                                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
                            ).addOnSuccessListener { location ->
                                if (location != null) {
                                    viewModel.updateUserLocation(LatLng(location.latitude, location.longitude))
                                }
                            }
                        } catch (_: SecurityException) {}
                    }
                )
                
                // Status Overlay
                if (isSharing) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Sharing Location Live",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Controls Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSharing) 
                        "You are currently sharing your location with nearby tourists. Map updates every 1 minute." 
                    else 
                        "Share your location to see other connected tourists around you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { viewModel.toggleSharing() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSharing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isSharing) "Stop" else "Start",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
