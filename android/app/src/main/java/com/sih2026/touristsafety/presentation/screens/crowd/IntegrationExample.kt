package com.sih2026.touristsafety.presentation.screens.crowd

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sih2026.touristsafety.presentation.navigation.Screen

/**
 * INTEGRATION EXAMPLES
 * 
 * Copy these examples to integrate the Crowd Density feature into your app.
 */

// ========================================
// EXAMPLE 1: Simple Card Navigation Button
// ========================================
/**
 * Add this to your Home Screen or Safety Features section
 */
@Composable
fun CrowdDensityNavigationCard(navController: NavController) {
    Card(
        onClick = { navController.navigate(Screen.CrowdDensity.route) },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column {
                Text(
                    "Crowd Density Monitor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Check real-time crowd density & avoid stampedes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// ========================================
// EXAMPLE 2: Feature Grid Item
// ========================================
/**
 * Add this to a feature grid layout (2 columns)
 */
@Composable
fun CrowdDensityGridItem(navController: NavController) {
    Card(
        onClick = { navController.navigate(Screen.CrowdDensity.route) },
        modifier = Modifier
            .aspectRatio(1f)
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF6B6B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Crowd\nDensity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ========================================
// EXAMPLE 3: Alert Banner (High Priority)
// ========================================
/**
 * Show this banner when crowd density is critical in user's area
 */
@Composable
fun CrowdDensityAlertBanner(navController: NavController) {
    Card(
        onClick = { navController.navigate(Screen.CrowdDensity.route) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFB71C1C)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "⚠️ High Crowd Density Alert",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Tap to view safe routes & alternate exits",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

// ========================================
// EXAMPLE 4: List Item Navigation
// ========================================
/**
 * Add this to a list of safety features
 */
@Composable
fun CrowdDensityListItem(navController: NavController) {
    Surface(
        onClick = { navController.navigate(Screen.CrowdDensity.route) },
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text("Crowd Density Monitor") },
            supportingContent = { Text("Real-time stampede prevention & safe navigation") },
            leadingContent = {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        )
    }
}

// ========================================
// EXAMPLE 5: Floating Action Button
// ========================================
/**
 * Add this as a FAB when user is in a tourist area
 */
@Composable
fun CrowdDensityFAB(navController: NavController) {
    ExtendedFloatingActionButton(
        onClick = { navController.navigate(Screen.CrowdDensity.route) },
        icon = { Icon(Icons.Default.Group, contentDescription = null) },
        text = { Text("Check Crowd") },
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = Color.White
    )
}

// ========================================
// EXAMPLE 6: Bottom Navigation
// ========================================
/**
 * Complete bottom navigation example with Crowd Density item
 * 
 * Add this to your main scaffold:
 * 
 * @Composable
 * fun MainScreen(navController: NavController) {
 *     Scaffold(
 *         bottomBar = { CrowdDensityBottomNavigation(navController) }
 *     ) { paddingValues ->
 *         // Your content
 *     }
 * }
 */
@Composable
fun CrowdDensityBottomNavigation(
    navController: NavController,
    selectedRoute: String = Screen.Home.route
) {
    NavigationBar {
        // Home item
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = selectedRoute == Screen.Home.route,
            onClick = { navController.navigate(Screen.Home.route) }
        )
        
        // Crowd Density item
        NavigationBarItem(
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Crowd") },
            selected = selectedRoute == Screen.CrowdDensity.route,
            onClick = { navController.navigate(Screen.CrowdDensity.route) }
        )
        
        // Add more navigation items as needed
    }
}

// ========================================
// USAGE IN HOME SCREEN
// ========================================
/**
 * Example of adding to your HomeScreen.kt:
 * 
 * @Composable
 * fun HomeScreen(navController: NavController) {
 *     Scaffold(
 *         floatingActionButton = { CrowdDensityFAB(navController) }
 *     ) { padding ->
 *         Column(
 *             modifier = Modifier
 *                 .fillMaxSize()
 *                 .padding(padding)
 *                 .verticalScroll(rememberScrollState())
 *         ) {
 *             // Show alert if crowd is high
 *             CrowdDensityAlertBanner(navController)
 *             
 *             Spacer(modifier = Modifier.height(16.dp))
 *             
 *             // Add to safety features section
 *             Text("Safety Features", style = MaterialTheme.typography.headlineSmall)
 *             CrowdDensityNavigationCard(navController)
 *             
 *             // OR use grid layout
 *             LazyVerticalGrid(columns = GridCells.Fixed(2)) {
 *                 item { CrowdDensityGridItem(navController) }
 *                 // ... other features
 *             }
 *         }
 *     }
 * }
 */

// ========================================
// PROGRAMMATIC NAVIGATION
// ========================================
/**
 * Navigate programmatically from any composable:
 */
fun navigateToCrowdDensity(navController: NavController) {
    navController.navigate(Screen.CrowdDensity.route)
}

/**
 * Navigate from ViewModel:
 * 
 * class HomeViewModel @Inject constructor() : ViewModel() {
 *     fun checkCrowdDensity(navController: NavController) {
 *         // Perform any checks or logging
 *         navController.navigate(Screen.CrowdDensity.route)
 *     }
 * }
 */
