package com.sih2026.touristsafety.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object SOS : Screen("sos")
    object Map : Screen("map")
    object EProfile : Screen("eprofile")
    object DocumentUpload : Screen("document_upload?category={category}") {
        fun createRoute(category: String? = null) = if (category != null) "document_upload?category=$category" else "document_upload"
    }
    object DocumentDetail : Screen("document_detail/{docId}") {
        fun createRoute(docId: String) = "document_detail/$docId"
    }
    object Chatbot : Screen("chatbot")
    object EFir : Screen("efir")
    object IncidentReport : Screen("incident_report")
    object DisasterAlerts : Screen("disaster_alerts")
    object Translator : Screen("translator")
    object CameraTranslator : Screen("camera_translator")
    object Settings : Screen("settings")
    object EmergencyContacts : Screen("emergency_contacts")
    object WomenSafety : Screen("women_safety")
    object InactivitySettings : Screen("inactivity_settings")
}
