package com.sih2026.touristsafety.presentation.screens.efir

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import kotlin.math.ceil
import kotlin.math.max

object FirPdfExporter {

    fun exportHtmlToPdf(
        context: Context,
        htmlContent: String,
        baseFileName: String,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        Handler(Looper.getMainLooper()).post {
            try {
                val webView = WebView(context)
                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = false
                    useWideViewPort = true
                    textZoom = 100
                }
                
                // Set layout width matching standard A4 at 96 DPI (794px)
                val a4WidthPx = 794
                webView.layout(0, 0, a4WidthPx, 1123)

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            renderWebViewToPdfFile(context, webView, baseFileName, onSuccess, onError)
                        }, 700)
                    }
                }
                
                webView.loadDataWithBaseURL("https://police.gov.in", htmlContent, "text/html", "UTF-8", null)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to initialize WebView for PDF printing")
            }
        }
    }

    private fun renderWebViewToPdfFile(
        context: Context,
        webView: WebView,
        baseFileName: String,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val pageWidth = 595  // Standard A4 width in PostScript points (72 DPI)
            val pageHeight = 842 // Standard A4 height in PostScript points (72 DPI)

            val webViewWidth = max(1, webView.width)
            val scale = pageWidth.toFloat() / webViewWidth.toFloat()

            val contentHeightPx = (webView.contentHeight * webView.scale).toInt()
            val totalScaledHeight = max(pageHeight, (contentHeightPx * scale).toInt())
            val totalPages = max(1, ceil(totalScaledHeight.toDouble() / pageHeight).toInt())

            val pdfDocument = PdfDocument()

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                canvas.save()
                canvas.translate(0f, -(pageIndex * pageHeight).toFloat())
                canvas.scale(scale, scale)
                webView.draw(canvas)
                canvas.restore()

                pdfDocument.finishPage(page)
            }

            val fileNameWithExt = "${baseFileName}_${System.currentTimeMillis()}.pdf"
            val tempFile = File(context.cacheDir, fileNameWithExt)
            if (tempFile.exists()) tempFile.delete()

            tempFile.outputStream().use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            val savedUri = saveToDownloads(context, tempFile, fileNameWithExt)
            if (savedUri != null) {
                onSuccess(savedUri)
            } else {
                onError("Failed to save PDF to Downloads storage")
            }
        } catch (e: Exception) {
            onError(e.message ?: "PDF generation error")
        }
    }

    fun printOfficialFir(context: Context, htmlContent: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                val webView = WebView(context)
                webView.settings.javaScriptEnabled = true
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        val printAdapter = webView.createPrintDocumentAdapter("e-FIR_Official_Document")
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()
                        printManager?.print("Official e-FIR Document", printAdapter, printAttributes)
                    }
                }
                webView.loadDataWithBaseURL("https://police.gov.in", htmlContent, "text/html", "UTF-8", null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveToDownloads(context: Context, sourceFile: File, displayName: String): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return null

            resolver.openOutputStream(uri)?.use { out ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun openPdfViewer(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open e-FIR Document"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
