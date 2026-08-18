package com.sih2026.touristsafety.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sih2026.touristsafety.presentation.components.BottomNavBar
import com.sih2026.touristsafety.presentation.components.SOSButton
import com.sih2026.touristsafety.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = Screen.Home.route,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Welcome Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Tourist",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Stay safe and explore",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // SOS Button Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                SOSButton(
                    onSOSTriggered = { navController.navigate(Screen.SOS.route) },
                    modifier = Modifier.size(160.dp)
                )
            }

            // Quick Actions Grid
            val quickActions = listOf(
                QuickActionItem("Emergency", Icons.Default.Phone, Screen.EmergencyContacts.route),
                QuickActionItem("Report", Icons.Default.CameraAlt, Screen.IncidentReport.route),
                QuickActionItem("Assistant", Icons.Default.Chat, Screen.Chatbot.route),
                QuickActionItem("Explore", Icons.Default.Explore, Screen.Map.route),
                QuickActionItem("Documents", Icons.Default.Badge, Screen.EProfile.route),
                QuickActionItem("Translate", Icons.Default.Translate, Screen.Translator.route)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickActions) { action ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clickable { navController.navigate(action.route) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.title,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Active Alerts Section
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Alerts",
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "No active alerts in your area",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
