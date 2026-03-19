package com.project.reach.ui.screens.qrcode

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.project.reach.ui.navigation.NavigationDestination
import com.project.reach.ui.screens.settings.SettingViewModel
import com.project.reach.util.QRCode
import com.project.reach.util.debug

object QRCodeScreenDestination : NavigationDestination {
    override val route: String = "qrcode/{userId}/{username}"
    fun createRoute(userId: String, username: String) = "qrcode/$userId/$username"
}

@Composable
fun QRCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: QRCodeViewModel = hiltViewModel(),
    navigateBack: () -> Unit, userId: String, username:String
) {
    var qrImage by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(userId, username) {
        qrImage = QRCode.generateQrCode("$userId;$username", size = 800)
    }
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
            qrImage?.let {
                Image(
                    bitmap = it.asImageBitmap(), contentDescription = "QR code"
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
            ShareId(qrImage, viewModel::getTempImageUri)
        }
    }
}
@Composable
fun ShareId(qrImage: Bitmap?, getUri: (Context, Bitmap?)-> Uri) {
    val context = LocalContext.current
    Row {
        Button(
            modifier = Modifier.padding(10.dp),
            onClick = {
                val imageUri = getUri(context, qrImage)
                debug(imageUri.toString())
                val sendIndent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val shareIntent = Intent.createChooser(sendIndent, null)
                context.startActivity(shareIntent)
            },
            border = BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.secondary
            ),
            colors = ButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.primary,
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                modifier = Modifier
                    .size(23.dp),
            )
            Spacer(modifier = Modifier.size(20.dp))
            Text(text = "Share QR Code")
        }

    }

}