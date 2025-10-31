package com.project.reach.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.UUID
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap

/**
 * Utility function for creating QR code from user ID. The returned bitmap
 * needs to be displayed on the UI using [androidx.compose.foundation.Image]
 *
 * Usage:
 * ```
 *  generateQrCode(userId)?.let {
 *      Image(
 *          bitmap = it.asImageBitmap(),
 *          // ...
 *      )
 *  }
 * ```
 *
 * @param uuid The user ID of the user
 * @param size Optional parameter which specifies the size of the QR code
 * Default value is `512`
 *
 * @return A [Bitmap] object if QR code is generated, otherwise if any errors
 * occur, it returns null
 */
fun generateQrCode(uuid: UUID, size: Int = 512): Bitmap? {
    return try {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        }

        val content = uuid.toString()
        val bitMatrix = QRCodeWriter().encode(
            content, BarcodeFormat.QR_CODE,
            size, size, hints
        )

        val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        bitmap
    } catch (e: Exception) {
        debug("generateQRCode Error: $e")
        null
    }
}