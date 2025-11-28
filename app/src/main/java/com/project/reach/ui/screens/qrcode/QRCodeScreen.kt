package com.project.reach.ui.screens.qrcode

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.project.reach.ui.app.LocalQRCode
import com.project.reach.ui.navigation.NavigationDestination
import com.project.reach.util.QRCode

object QRCodeScreenDestination : NavigationDestination {
    override val route: String = "qrcode/{userId}"
    fun createRoute(userId: String) = "qrcode/$userId"
}

@Composable
fun QRCodeScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit, userId: String
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier.padding(20.dp)
            ){
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "close",
                    modifier = Modifier
                        .clickable(onClick = navigateBack)
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QRCode.generateQrCode(userId, size = 800)?.let {
                Image(
                    bitmap = it.asImageBitmap(), contentDescription = "QR code"
                )
            }
        }
    }
}