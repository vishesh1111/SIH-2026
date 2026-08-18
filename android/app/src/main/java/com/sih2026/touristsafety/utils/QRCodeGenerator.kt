package com.sih2026.touristsafety.utils

import android.graphics.Bitmap
import android.graphics.Color
// Assuming ZXing dependency is added: implementation("com.google.zxing:core:3.5.3")
// If not, this is a mock implementation or we'd need to add the dependency.
// I will provide a basic ZXing implementation assuming it's available or easily addable.
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QRCodeGenerator {
    fun generateQR(data: String, size: Int): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
