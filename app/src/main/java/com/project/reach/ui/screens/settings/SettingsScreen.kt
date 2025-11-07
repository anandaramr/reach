package com.project.reach.ui.screens.settings


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.project.reach.ui.navigation.NavigationDestination
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.project.reach.ui.components.AvatarIcon
import com.project.reach.ui.components.AvatarIconSize
import com.project.reach.util.debug
import com.project.reach.util.truncate

object SettingsScreenDestination : NavigationDestination {
    override val route: String = "settings"
}

@Composable
fun SettingsScreen(
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.error.collect { error ->
            debug(error)
            snackbarHostState.showSnackbar(error,)
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(20.dp)
        ) },
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(25.dp)
                .fillMaxSize()
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarIcon(uiState.username.firstOrNull()?: ' ', AvatarIconSize.LARGE)
                    Spacer(modifier = Modifier.size(10.dp))
                    TextField(
                        value = uiState.username,
                        onValueChange = {viewModel.onUsernameChange(it)},
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions (
                            onDone = {
                                if(viewModel.updateUsername())
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
            }
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 20.dp, bottom = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            item {
//                Card(
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.surface,
//                    ),
//                    modifier = Modifier.fillMaxWidth(),
//                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.)
//                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
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
                                        .size(23.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier
                                        .size(23.dp),
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.userId,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
//                            Icon(
//                                imageVector = Icons.Default.CopyAll,
//                                contentDescription = "Copy",
//                                modifier = Modifier
//                                    .padding(horizontal = 5.dp)
//                                    .size(20.dp),
//                            )
                        }
                    }
                }
//            }
            item {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 20.dp, bottom = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
