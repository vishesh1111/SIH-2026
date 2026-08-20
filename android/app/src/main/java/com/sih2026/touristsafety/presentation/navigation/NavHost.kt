package com.sih2026.touristsafety.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sih2026.touristsafety.presentation.screens.alerts.DisasterAlertsScreen
import com.sih2026.touristsafety.presentation.screens.auth.LoginScreen
import com.sih2026.touristsafety.presentation.screens.auth.SignupScreen
import com.sih2026.touristsafety.presentation.screens.chatbot.ChatbotScreen
import com.sih2026.touristsafety.presentation.screens.contacts.EmergencyContactsScreen
import com.sih2026.touristsafety.presentation.screens.efir.EFirScreen
import com.sih2026.touristsafety.presentation.screens.eprofile.DocumentDetailScreen
import com.sih2026.touristsafety.presentation.screens.eprofile.DocumentUploadScreen
import com.sih2026.touristsafety.presentation.screens.eprofile.EProfileScreen
import com.sih2026.touristsafety.presentation.screens.home.HomeScreen
import com.sih2026.touristsafety.presentation.screens.incidents.IncidentReportScreen
import com.sih2026.touristsafety.presentation.screens.map.TouristMapScreen
import com.sih2026.touristsafety.presentation.screens.onboarding.OnboardingScreen
import com.sih2026.touristsafety.presentation.screens.safety.WomenSafetyScreen
import com.sih2026.touristsafety.presentation.screens.settings.InactivitySettingsScreen
import com.sih2026.touristsafety.presentation.screens.sos.SOSScreen
import com.sih2026.touristsafety.presentation.screens.translator.CameraTranslatorScreen
import com.sih2026.touristsafety.presentation.screens.translator.TranslatorScreen

@Composable
fun TouristSafetyNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(250)
            ) + androidx.compose.animation.scaleIn(
                initialScale = 0.96f,
                animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        },
        exitTransition = {
            androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(200)
            )
        },
        popEnterTransition = {
            androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(250)
            ) + androidx.compose.animation.scaleIn(
                initialScale = 0.96f,
                animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(200)
            )
        }
    ) {
        // Auth Flow
        composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Signup.route) { SignupScreen(navController) }

        // Main Screens
        composable(Screen.Home.route) { HomeScreen(navController) }

        // SOS (Feature 1) — expects onNavigateBack callback
        composable(Screen.SOS.route) {
            SOSScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Map (Feature 6) — expects onNavigateBack callback
        composable(Screen.Map.route) {
            TouristMapScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Emergency Contacts (Feature 2) — expects optional onNavigateNext
        composable(Screen.EmergencyContacts.route) {
            EmergencyContactsScreen(
                onNavigateNext = { navController.navigate(Screen.Home.route) }
            )
        }

        // Disaster Alerts (Feature 3) — no nav params needed
        composable(Screen.DisasterAlerts.route) {
            DisasterAlertsScreen()
        }

        // Incident Reporting (Feature 4) — expects onNavigateBack
        composable(Screen.IncidentReport.route) {
            IncidentReportScreen(onNavigateBack = { navController.popBackStack() })
        }

        // AI Chatbot (Feature 5) — no nav params needed
        composable(Screen.Chatbot.route) {
            ChatbotScreen()
        }

        // Translator (Feature 8) — expects onNavigateBack and onNavigateToCamera
        composable(Screen.Translator.route) {
            TranslatorScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Screen.CameraTranslator.route) }
            )
        }
        composable(Screen.CameraTranslator.route) {
            CameraTranslatorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // E-FIR (Feature 9) — expects onNavigateBack
        composable(Screen.EFir.route) {
            EFirScreen(onNavigateBack = { navController.popBackStack() })
        }

        // E-Profile (Feature 10) — expects onNavigateBack, onNavigateToUpload, onNavigateToDetail
        composable(Screen.EProfile.route) {
            EProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUpload = { navController.navigate(Screen.DocumentUpload.route) },
                onNavigateToDetail = { docId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(docId))
                }
            )
        }
        composable(Screen.DocumentUpload.route) {
            DocumentUploadScreen(
                onNavigateBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.DocumentDetail.route,
            arguments = listOf(navArgument("docId") { type = NavType.StringType })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            DocumentDetailScreen(
                documentId = docId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Safety Monitor (Feature 11) — expects onNavigateBack
        composable(Screen.WomenSafety.route) {
            WomenSafetyScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Settings
        composable(Screen.InactivitySettings.route) {
            InactivitySettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) { PlaceholderScreen("Settings") }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}
