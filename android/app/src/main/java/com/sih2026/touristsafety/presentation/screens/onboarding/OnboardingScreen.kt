package com.sih2026.touristsafety.presentation.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sih2026.touristsafety.presentation.navigation.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPageData(
            title = "Stay Safe Everywhere",
            description = "Your personal safety companion wherever you travel. Get real-time alerts and access to local authorities.",
            icon = Icons.Default.Security
        ),
        OnboardingPageData(
            title = "Instant SOS",
            description = "Trigger an emergency response with a single tap. We'll automatically share your location with authorities and emergency contacts.",
            icon = Icons.Default.Warning
        ),
        OnboardingPageData(
            title = "AI-Powered Assistant",
            description = "Get instant help with translations, local guidelines, and immediate support through our smart chatbot.",
            icon = Icons.Default.AutoAwesome
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(pageData = pages[page])
        }

        // Pager Indicators
        Row(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        if (pagerState.currentPage == pages.size - 1) {
            Button(
                onClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp)
                    .height(56.dp)
            ) {
                Text("Get Started", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun OnboardingPage(pageData: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = pageData.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = pageData.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = pageData.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector
)
