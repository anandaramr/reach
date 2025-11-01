package com.project.reach.ui.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.project.reach.permission.PermissionHandler
import com.project.reach.util.QRCode

/**
 * Provides core Activity-scoped utilities to the Compose hierarchy
 *
 * This provider encapsulates activity-scoped helpers and makes them available throughout
 * the composable tree without explicit parameter passing. Instances are initialized
 * and cached for the lifetime of the associated activity
 *
 * The following utilities are currently provided:
 * - [LocalPermissionHandler]
 * - [LocalQRCode]
 *
 * Usage:
 * ```
 * class MainActivity : ComponentActivity() {
 *     private val localCoreProvider = LocalCoreProvider(this)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         setContent {
 *             localCoreProvider {
 *                 // Your composable content
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * Accessing provided values:
 * ```
 * @Composable
 * fun SomeScreen() {
 *     val permissionHandler = LocalPermissionHandler.current
 *     val qrCode = LocalQRCode.current
 *     // Use the helpers
 * }
 * ```
 *
 * @param activity The [ComponentActivity] that scopes the provided utilities
 *
 * @property permissionHandler Handles runtime permission requests and checks
 * @property QRCode Manages QR code scanning and generation functionality
 */
class CoreProvider(activity: ComponentActivity) {
    val permissionHandler = PermissionHandler(activity)

    val qrCode = QRCode(activity)

    /**
     * Provides scoped utilities to the composable tree. See
     * [com.project.reach.ui.app.CoreProvider] for more
     */
    @Composable
    operator fun invoke(content: @Composable (() -> Unit)) {
        CompositionLocalProvider(
            LocalPermissionHandler provides permissionHandler,
            LocalQRCode provides qrCode
        ) {
            content()
        }
    }
}

/**
 * Provides an instance of [PermissionHandler]
 *
 * Usage:
 * ```
 *  val permissionHandler = LocalPermissionHandler.current
 * ```
 * Note: can only be invoked inside a [Composable]
 */
val LocalPermissionHandler = staticCompositionLocalOf<PermissionHandler> {
    error("Permission handler local provider failed")

}
/**
 * Provides an instance of [QRCode]
 *
 * Usage:
 * ```
 *  val qrCode = LocalQRCode.current
 * ```
 * Note: can only be invoked inside a [Composable]
 */
val LocalQRCode = staticCompositionLocalOf<QRCode> {
    error("QrCodeHelper local provider failed")
}