package com.sih2026.touristsafety.presentation.screens.efir

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.sih2026.touristsafety.BuildConfig
import com.sih2026.touristsafety.data.remote.StructuredFir
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class EFirViewModel @Inject constructor() : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _rawDescription = MutableStateFlow("")
    val rawDescription: StateFlow<String> = _rawDescription.asStateFlow()

    private val _structuredFir = MutableStateFlow<StructuredFir?>(null)
    val structuredFir: StateFlow<StructuredFir?> = _structuredFir.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _attachedEvidence = MutableStateFlow<List<Uri>>(emptyList())
    val attachedEvidence: StateFlow<List<Uri>> = _attachedEvidence.asStateFlow()

    // Setup Gemini
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.6-flash",
        apiKey = com.sih2026.touristsafety.utils.GeminiApiKeys.getNextKey()
    )

    fun setRawDescription(desc: String) {
        _rawDescription.value = desc
    }

    fun nextStep() {
        if (_currentStep.value < 4) {
            _currentStep.value += 1
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun generateFir() {
        if (_rawDescription.value.isBlank()) return
        
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val prompt = """
                    You are a legal assistant helping to draft an FIR (First Information Report) in India.
                    Analyze the following incident description and return ONLY a valid JSON object with the following string fields:
                    - incidentType (e.g., Theft, Assault)
                    - bnsSections (comma separated list of applicable BNS sections)
                    - dateTime (extracted or estimated)
                    - place (extracted location)
                    - accusedDescription (if any)
                    - propertyLost (if any)
                    - witnesses (if any)
                    - narrative (a polished, formal version of the incident)
                    - complainantDetails (extract or put "Not provided")
                    
                    Description: ${_rawDescription.value}
                    
                    Return ONLY JSON without any markdown formatting or backticks.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text?.trim()?.removePrefix("```json")?.removeSuffix("```")?.trim() ?: "{}"
                
                val json = JSONObject(responseText)
                
                val bnsList = if (json.has("bnsSections")) {
                    json.getString("bnsSections").split(",").map { it.trim() }
                } else {
                    emptyList()
                }

                _structuredFir.value = StructuredFir(
                    incidentType = json.optString("incidentType", "Unknown"),
                    bnsSections = bnsList,
                    dateTime = json.optString("dateTime", "Unknown"),
                    place = json.optString("place", "Unknown"),
                    accusedDescription = json.optString("accusedDescription", "None"),
                    propertyLost = json.optString("propertyLost", "None"),
                    witnesses = json.optString("witnesses", "None"),
                    narrative = json.optString("narrative", _rawDescription.value),
                    complainantDetails = json.optString("complainantDetails", "Not provided")
                )
                
                _currentStep.value = 2
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun updateField(field: String, value: Any) {
        val currentFir = _structuredFir.value ?: return
        _structuredFir.value = when(field) {
            "incidentType" -> currentFir.copy(incidentType = value as String)
            "dateTime" -> currentFir.copy(dateTime = value as String)
            "place" -> currentFir.copy(place = value as String)
            "accusedDescription" -> currentFir.copy(accusedDescription = value as String)
            "propertyLost" -> currentFir.copy(propertyLost = value as String)
            "witnesses" -> currentFir.copy(witnesses = value as String)
            "narrative" -> currentFir.copy(narrative = value as String)
            "complainantDetails" -> currentFir.copy(complainantDetails = value as String)
            else -> currentFir
        }
    }

    fun downloadPdf(context: Context) {
        val fir = _structuredFir.value ?: return
        
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val document = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                    val page = document.startPage(pageInfo)
                    
                    val canvas: Canvas = page.canvas
                    val paint = Paint().apply {
                        color = Color.BLACK
                        textSize = 14f
                    }
                    val titlePaint = Paint().apply {
                        color = Color.BLACK
                        textSize = 20f
                        isFakeBoldText = true
                    }
                    
                    var yPosition = 50f
                    val xPosition = 50f
                    
                    canvas.drawText("First Information Report (E-FIR Draft)", xPosition, yPosition, titlePaint)
                    yPosition += 40f
                    
                    fun drawLine(text: String) {
                        // Very basic text wrapping for PDF
                        val lines = text.chunked(70)
                        for (line in lines) {
                            canvas.drawText(line, xPosition, yPosition, paint)
                            yPosition += 20f
                        }
                        yPosition += 10f
                    }
                    
                    drawLine("Incident Type: ${fir.incidentType}")
                    drawLine("BNS Sections: ${fir.bnsSections.joinToString()}")
                    drawLine("Date & Time: ${fir.dateTime}")
                    drawLine("Place: ${fir.place}")
                    drawLine("Accused Description: ${fir.accusedDescription}")
                    drawLine("Property Lost: ${fir.propertyLost}")
                    drawLine("Witnesses: ${fir.witnesses}")
                    drawLine("Complainant: ${fir.complainantDetails}")
                    yPosition += 20f
                    drawLine("Narrative:")
                    drawLine(fir.narrative)
                    
                    document.finishPage(page)
                    
                    val fileName = "EFIR_${System.currentTimeMillis()}.pdf"
                    
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            document.writeTo(outputStream)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "PDF Saved to Downloads", Toast.LENGTH_LONG).show()
                        }
                    }
                    
                    document.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun getPolicePortalUrl(state: String): String {
        return when(state.lowercase()) {
            "delhi" -> "https://digitalpolice.gov.in/"
            "maharashtra" -> "https://citizen.mahapolice.gov.in/"
            "karnataka" -> "https://ksp.karnataka.gov.in/"
            "tamil nadu" -> "https://eservices.tnpolice.gov.in/"
            "kerala" -> "https://keralapolice.gov.in/"
            "goa" -> "https://goapolice.gov.in/"
            "rajasthan" -> "https://police.rajasthan.gov.in/"
            "uttar pradesh" -> "https://uppolice.gov.in/"
            "gujarat" -> "https://gujhome.gujarat.gov.in/"
            "west bengal" -> "https://wbpolice.gov.in/"
            else -> "https://digitalpolice.gov.in/"
        }
    }
}
