package com.project.reach.ui.screens.settings


import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.project.reach.ui.navigation.NavigationDestination
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.project.reach.service.NotificationHandler
import com.project.reach.ui.components.AvatarIcon
import com.project.reach.ui.components.AvatarIconSize
import com.project.reach.util.debug
import kotlin.apply

object SettingsScreenDestination : NavigationDestination {
    override val route: String = "settings"
}

@Composable
fun SettingsScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    navigateToQRCode: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.error.collect { error ->
            debug(error)
            snackbarHostState.showSnackbar(error)
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(20.dp)
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(vertical = 25.dp, horizontal = 15.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarIcon(uiState.username.firstOrNull() ?: ' ', AvatarIconSize.LARGE)
                Spacer(modifier = Modifier.size(10.dp))
                TextField(
                    value = uiState.username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (viewModel.updateUsername())
                                focusManager.clearFocus()
                        },
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                )
            }
            AccountDetails(navigateToQRCode, uiState.userId)
            NotificationChannel()
        }
    }
}

@Composable
private fun NotificationChannel() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Notification",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Manage Notification",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.clickable(
                    onClick = {
                        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE,context.packageName)
                            putExtra(Settings.EXTRA_CHANNEL_ID, NotificationHandler.MESSAGE_NOTIFICATION_CHANNEL)
                        }
                        context.startActivity(intent)
                    }
                )
            )
        }
    }
}
@Composable
private fun AccountDetails(
    navigateToQRCode: (String) -> Unit,
    userId: String
) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(23.dp)
                            .clickable(onClick = { navigateToQRCode(userId) })
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    ShareId(userId)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = userId,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
}

@Composable
private fun ColumnScope.Divider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .align(Alignment.CenterHorizontally),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun ShareId(userId: String) {
    val sendIndent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, userId)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIndent, null)
    val context = LocalContext.current
    Icon(
        imageVector = Icons.Default.Share,
        contentDescription = "Share",
        modifier = Modifier
            .size(23.dp)
            .clickable(
                onClick = {
                    context.startActivity(shareIntent)
                }
            )
    )
}
