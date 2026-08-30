package com.sih2026.touristsafety.presentation.screens.translator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit
) {
    val sourceLang by viewModel.sourceLanguage.collectAsState()
    val targetLang by viewModel.targetLanguage.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val availableLanguages by viewModel.availableLanguages.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var showSourceLangSheet by remember { mutableStateOf(false) }
    var showTargetLangSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Translator") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCamera) {
                        Icon(Icons.Default.CameraAlt, "Camera Translate")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Language Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { showSourceLangSheet = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(sourceLang.name)
                }
                
                IconButton(onClick = { viewModel.swapLanguages() }) {
                    Icon(Icons.Default.SwapHoriz, "Swap Languages")
                }
                
                OutlinedButton(
                    onClick = { showTargetLangSheet = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(targetLang.name)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input TextField
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.setInputText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Enter text to translate") },
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setInputText("") }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                }
            )
            
            // Middle Actions (Mic & Speaker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledIconButton(
                    onClick = { viewModel.toggleOfflineVoiceInput() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isListening) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isListening) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Speak"
                    )
                }

                IconButton(onClick = { viewModel.speakText(inputText, sourceLang.code) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Listen")
                }
            }

            // Output Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        Text(
                            text = translatedText.ifBlank { "Translation will appear here" },
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (translatedText.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (translatedText.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(translatedText))
                            }) {
                                Icon(Icons.Default.ContentCopy, "Copy")
                            }
                            IconButton(onClick = { viewModel.speakText(translatedText, targetLang.code) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, "Listen")
                            }
                        }
                    }
                }
            }
        }

        // Source Language Sheet
        if (showSourceLangSheet) {
            ModalBottomSheet(onDismissRequest = { showSourceLangSheet = false }) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item { Text("Select Source Language", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(availableLanguages) { lang ->
                        ListItem(
                            headlineContent = { Text(lang.name) },
                            supportingContent = { Text(lang.nativeName) },
                            modifier = Modifier.clickable {
                                viewModel.setSourceLanguage(lang)
                                showSourceLangSheet = false
                            }
                        )
                    }
                }
            }
        }

        // Target Language Sheet
        if (showTargetLangSheet) {
            ModalBottomSheet(onDismissRequest = { showTargetLangSheet = false }) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item { Text("Select Target Language", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(availableLanguages) { lang ->
                        ListItem(
                            headlineContent = { Text(lang.name) },
                            supportingContent = { Text(lang.nativeName) },
                            modifier = Modifier.clickable {
                                viewModel.setTargetLanguage(lang)
                                showTargetLangSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}
