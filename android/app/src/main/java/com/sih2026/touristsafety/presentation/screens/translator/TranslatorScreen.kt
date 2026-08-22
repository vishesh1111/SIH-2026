package com.sih2026.touristsafety.presentation.screens.translator

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
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
    val availableLanguages by viewModel.availableLanguages.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var showSourceLangSheet by remember { mutableStateOf(false) }
    var showTargetLangSheet by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                viewModel.setInputText(matches[0])
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Translator") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            
            // Middle Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                val context = LocalContext.current
                IconButton(onClick = {
                    try {
                        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, sourceLang.code)
                            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak now to translate")
                        }
                        speechRecognizerLauncher.launch(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Speech to text not supported on this device", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.Mic, "Speak")
                }
                IconButton(onClick = { viewModel.speakText(inputText, sourceLang.code) }) {
                    Icon(Icons.Default.VolumeUp, "Listen")
                }
            }

            // Output TextField
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        Text(
                            text = translatedText,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(onClick = { viewModel.speakText(translatedText, targetLang.code) }) {
                            Icon(Icons.Default.VolumeUp, "Listen")
                        }
                        IconButton(onClick = { 
                            if (translatedText.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(translatedText))
                            }
                        }) {
                            Icon(Icons.Default.ContentCopy, "Copy")
                        }
                    }
                }
            }
        }
    }

    if (showSourceLangSheet) {
        LanguageSelectionSheet(
            languages = availableLanguages,
            onDismiss = { showSourceLangSheet = false },
            onSelect = { 
                viewModel.setSourceLanguage(it)
                showSourceLangSheet = false
            }
        )
    }

    if (showTargetLangSheet) {
        LanguageSelectionSheet(
            languages = availableLanguages,
            onDismiss = { showTargetLangSheet = false },
            onSelect = { 
                viewModel.setTargetLanguage(it)
                showTargetLangSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionSheet(
    languages: List<LanguageOption>,
    onDismiss: () -> Unit,
    onSelect: (LanguageOption) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.padding(bottom = 32.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(languages) { lang ->
                ListItem(
                    headlineContent = { Text(lang.name) },
                    supportingContent = { Text(lang.nativeName) },
                    trailingContent = {
                        if (!lang.isDownloaded) {
                            Icon(Icons.Default.Download, "Download Offline Model")
                        } else {
                            Icon(Icons.Default.CheckCircle, "Downloaded", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.clickable { onSelect(lang) }
                )
                HorizontalDivider()
            }
        }
    }
}
