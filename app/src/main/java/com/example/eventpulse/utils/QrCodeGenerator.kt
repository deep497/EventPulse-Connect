package com.example.eventpulse.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    
    /**
     * Generates a QR code bitmap from the given content
     * 
     * @param content The text content to encode in the QR code
     * @param width The width of the QR code bitmap in pixels
     * @param height The height of the QR code bitmap in pixels
     * @return A Bitmap containing the QR code, or null if generation fails
     */
    fun generate(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Int>().apply {
                put(EncodeHintType.MARGIN, 1)
            }

            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
            )

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
