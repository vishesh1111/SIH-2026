package com.sih2026.touristsafety.presentation.screens.contacts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sih2026.touristsafety.data.local.entities.EmergencyContactEntity
import com.sih2026.touristsafety.presentation.components.CountryCodePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
    viewModel: EmergencyContactsViewModel = hiltViewModel(),
    onNavigateNext: (() -> Unit)? = null // Used during onboarding
) {
    val contacts by viewModel.contacts.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContactEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingContact = null
                showSheet = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        },
        bottomBar = {
            if (onNavigateNext != null) {
                Button(
                    onClick = onNavigateNext,
                    enabled = contacts.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Continue")
                }
            }
        }
    ) { paddingValues ->
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No emergency contacts added yet.\nPlease add at least one contact.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(contacts, key = { it.id }) { contact ->
                    ContactCard(
                        contact = contact,
                        onDelete = { viewModel.deleteContact(contact.id) },
                        onClick = { 
                            editingContact = contact
                            showSheet = true 
                        }
                    )
                }
            }
        }

        if (showSheet) {
            ContactBottomSheet(
                initialContact = editingContact,
                onDismiss = { showSheet = false },
                onSave = { name, phone, rel, code, addr, isPrimary ->
                    if (editingContact != null) {
                        viewModel.updateContact(
                            editingContact!!.copy(
                                name = name,
                                phone = phone,
                                relationship = rel,
                                countryCode = code,
                                residentAddress = addr,
                                isPrimary = isPrimary
                            )
                        )
                    } else {
                        viewModel.addContact(name, phone, rel, code, addr, isPrimary)
                    }
                    showSheet = false
                }
            )
        }
    }
}

@Composable
fun ContactCard(
    contact: EmergencyContactEntity,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (contact.isPrimary) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge { Text("Primary") }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${contact.countryCode} ${contact.phone}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contact.relationship,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactBottomSheet(
    initialContact: EmergencyContactEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String?, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var phone by remember { mutableStateOf(initialContact?.phone ?: "") }
    var relationship by remember { mutableStateOf(initialContact?.relationship ?: "") }
    var countryCode by remember { mutableStateOf(initialContact?.countryCode ?: "+91") }
    var address by remember { mutableStateOf(initialContact?.residentAddress ?: "") }
    var isPrimary by remember { mutableStateOf(initialContact?.isPrimary ?: false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(if (initialContact == null) "Add Emergency Contact" else "Edit Emergency Contact", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name*") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                CountryCodePicker(
                    selectedCode = countryCode,
                    onCodeSelected = { countryCode = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number*") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = relationship,
                onValueChange = { relationship = it },
                label = { Text("Relationship (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Resident Address (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = isPrimary, onCheckedChange = { isPrimary = it })
                Text("Set as primary contact")
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name, phone, relationship, countryCode, address.takeIf { it.isNotBlank() }, isPrimary)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text(if (initialContact == null) "Save Contact" else "Update Contact")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
