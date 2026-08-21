package com.sih2026.touristsafety.presentation.screens.crowd

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * Main screen displaying crowd density visualization with Google Maps
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrowdDensityMapScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Mock data center point (e.g., Gateway of India, Mumbai)
    val centerLocation = remember { LatLng(18.9220, 72.8347) }
    
    // Generate mock crowd data
    val crowdPoints = remember { generateMockCrowdData(centerLocation, radiusMeters = 200.0, count = 85) }
    
    // Mock density analysis
    val densityInfo = remember { analyzeMockDensity(crowdPoints.size) }
    
    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLocation, 17f)
    }
    
    // UI state
    var showDensityInfo by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        ) {
            // Render crowd points as semi-transparent red circles
            crowdPoints.forEach { point ->
                Circle(
                    center = point,
                    radius = 8.0, // 8 meters radius per point
                    fillColor = Color(0x80FF0000), // Semi-transparent red
                    strokeColor = Color(0x40FF0000),
                    strokeWidth = 1f
                )
            }
            
            // Central marker for the main location
            Marker(
                state = MarkerState(position = centerLocation),
                title = "Gateway of India",
                snippet = "Tourist Hotspot"
            )
        }

        // Top App Bar
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

        // Density Information Card (Bottom Sheet Style)
        AnimatedVisibility(
            visible = showDensityInfo,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            CrowdDensityInfoCard(
                densityInfo = densityInfo,
                onRecenter = {
                    // Recenter camera
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(centerLocation, 17f)
                }
            )
        }

        // Legend indicator (top right)
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Density Legend",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0x80FF0000))
                    )
                    Text(
                        "GPS Point",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Bottom card displaying crowd density information
 */
@Composable
internal fun CrowdDensityInfoCard(
    densityInfo: DensityInfo,
    onRecenter: () -> Unit
) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Live Area Density",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
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

            // Risk Status Badge with animation
            RiskStatusBadge(densityInfo = densityInfo)

            // People Count Stat
            StatCard(
                icon = Icons.Default.People,
                label = "People Around You",
                value = "~${densityInfo.estimatedPeople}",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            )

            // Density Percentage
            DensityProgressIndicator(
                percentage = densityInfo.densityPercentage,
                label = "Area Capacity"
            )

            // Recommendation Box
            if (densityInfo.recommendation.isNotEmpty()) {
                RecommendationBox(
                    recommendation = densityInfo.recommendation,
                    riskLevel = densityInfo.riskLevel
                )
            }

            // Additional Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.Update,
                    text = "Live",
                    modifier = Modifier.weight(1f)
                )
                InfoChip(
                    icon = Icons.Default.GpsFixed,
                    text = "${densityInfo.gpsPoints} Points",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Animated risk status badge
 */
@Composable
internal fun RiskStatusBadge(densityInfo: DensityInfo) {
    val (backgroundColor, textColor, icon, statusText) = when (densityInfo.riskLevel) {
        RiskLevel.CRITICAL -> Quadruple(
            Color(0xFFB71C1C),
            Color.White,
            Icons.Default.Warning,
            "🔴 Critical Risk - Stampede Warning"
        )
        RiskLevel.HIGH -> Quadruple(
            Color(0xFFFF6F00),
            Color.White,
            Icons.Default.ReportProblem,
            "🟠 High Risk - Use Caution"
        )
        RiskLevel.MEDIUM -> Quadruple(
            Color(0xFFFBC02D),
            Color.Black,
            Icons.Default.Info,
            "🟡 Moderate Density"
        )
        RiskLevel.LOW -> Quadruple(
            Color(0xFF2E7D32),
            Color.White,
            Icons.Default.CheckCircle,
            "🟢 Safe - Low Crowd"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(28.dp)
            )
            Text(
                statusText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Stat card component
 */
@Composable
internal fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Density progress indicator
 */
@Composable
internal fun DensityProgressIndicator(
    percentage: Int,
    label: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "$percentage%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                percentage >= 90 -> Color(0xFFB71C1C)
                percentage >= 70 -> Color(0xFFFF6F00)
                percentage >= 50 -> Color(0xFFFBC02D)
                else -> Color(0xFF2E7D32)
            },
        )
    }
}

/**
 * Recommendation box with icon
 */
@Composable
internal fun RecommendationBox(
    recommendation: String,
    riskLevel: RiskLevel
) {
    val (containerColor, contentColor) = when (riskLevel) {
        RiskLevel.CRITICAL -> Pair(Color(0xFFFFEBEE), Color(0xFFB71C1C))
        RiskLevel.HIGH -> Pair(Color(0xFFFFF3E0), Color(0xFFFF6F00))
        RiskLevel.MEDIUM -> Pair(Color(0xFFFFFDE7), Color(0xFFF57F17))
        RiskLevel.LOW -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Recommendation",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * Small info chip component
 */
@Composable
internal fun InfoChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .alpha(alpha.value),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Helper data class for quadruple values
 */
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
