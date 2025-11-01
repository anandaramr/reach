package com.project.reach.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Provides QR code generation and scanning functionality
 *
 * This helper encapsulates common QR-related operations such as:
 * - Launching the camera-based QR code scanner.
 * - Generating QR code bitmaps from arbitrary data.
 *
 * The class is provided to the composable tree using a CompositionLocalProvider.
 * See [com.project.reach.ui.app.CoreProvider] and [com.project.reach.ui.app.LocalQRCode]
 * for more
 *
 * **Example: Generating a QR code**
 * ```
 * qr = LocalQrCode.current
 * qr.generateQrCode(content)?.let { bitmap ->
 *     Image(
 *         bitmap = bitmap.asImageBitmap(),
 *         contentDescription = "User QR Code"
 *     )
 * }
 * ```
 *
 * **Example: Starting scanner**
 * ```
 * qr.startScanning { result ->
 *     // Handle scanned QR code content
 * }
 * ```
 *
 * @param activity The [ComponentActivity] used to register and manage
 * the activity result launcher for QR code scanning.
 *
 * @see com.project.reach.ui.app.CoreProvider
 * @see com.project.reach.ui.app.LocalQRCode
 */
class QRCode(activity: ComponentActivity) {
    private var onScanResultCallback: ((scanResult: String) -> Unit)? = null
    private val qrScanLauncher =
        activity.registerForActivityResult(ScanContract()) { result ->
            val scanResult = result.contents
            onScanResultCallback?.invoke(scanResult)
        }

    /**
     * Starts scanning for QR code. The result should be handled in the
     * callback function supplied
     *
     * ```
     *  qr.startScanning { result ->
     *      debug(result)
     *  }
     * ```
     *
     * @param onScanResult callback to be called after scanning
     */
    fun startScanning(onScanResult: (scanResult: String) -> Unit) {
        onScanResultCallback = onScanResult
        val scanOptions = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        }
        qrScanLauncher.launch(scanOptions)
    }

    companion object {
        /**
         * Utility function for creating QR code from given content. The returned bitmap
         * needs to be displayed on the UI using [androidx.compose.foundation.Image]
         *
         * Usage:
         * ```
         *  QRCode.generateQrCode(content)?.let {
         *      Image(
         *          bitmap = it.asImageBitmap(),
         *          // ...
         *      )
         *  }
         * ```
         *
         * @param content The content to be converted to QR code
         * @param size Optional parameter which specifies the size of the QR code
         * Default value is `512`
         *
         * @return A [Bitmap] object if QR code is generated, otherwise if any errors
         * occur, it returns null
         */
        fun generateQrCode(content: String, size: Int = 512): Bitmap? {
            return try {
                val hints = hashMapOf<EncodeHintType, Any>().apply {
                    put(EncodeHintType.CHARACTER_SET, "UTF-8")
                    put(EncodeHintType.MARGIN, 1)
                    put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                }

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
    }
}