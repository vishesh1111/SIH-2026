package com.sih2026.touristsafety.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class CountryCode(val name: String, val code: String, val flag: String)

val countryCodes = listOf(
    CountryCode("India", "+91", "🇮🇳"),
    CountryCode("United States", "+1", "🇺🇸"),
    CountryCode("United Kingdom", "+44", "🇬🇧"),
    CountryCode("Canada", "+1", "🇨🇦"),
    CountryCode("Australia", "+61", "🇦🇺"),
    CountryCode("UAE", "+971", "🇦🇪"),
    CountryCode("Germany", "+49", "🇩🇪"),
    CountryCode("France", "+33", "🇫🇷"),
    CountryCode("Japan", "+81", "🇯🇵"),
    CountryCode("China", "+86", "🇨🇳")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodePicker(
    selectedCode: String,
    onCodeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCode,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().width(100.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            countryCodes.forEach { country ->
                DropdownMenuItem(
                    text = { Text("${country.flag} ${country.code}") },
                    onClick = {
                        onCodeSelected(country.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
