package com.sih2026.touristsafety.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sih2026.touristsafety.presentation.theme.LocalCustomColors

@Composable
fun AlertCard(
    headline: String,
    description: String,
    severity: String,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val containerColor = when (severity.lowercase()) {
        "extreme" -> customColors.sosRed
        "severe" -> customColors.warningAmber
        else -> customColors.infoBlue
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}
