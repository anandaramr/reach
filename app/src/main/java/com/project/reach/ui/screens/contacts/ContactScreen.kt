package com.project.reach.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.NoAccounts
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.project.reach.ui.app.LocalQRCode
import com.project.reach.ui.navigation.NavigationDestination
import com.project.reach.util.debug

object ContactScreenDestination: NavigationDestination {
    override val route: String = "contact"
}

@Composable
fun ContactScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    navigateToChat: (String) -> Unit,
    navigateToNewContact: (String, String) -> Unit,
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navigateToNewContact, navigateBack, viewModel::isSaved, navigateToChat) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
                .padding()
                .fillMaxSize()
        ) {
            items(items = uiState.contactList) { user ->
                SavedUser(user, navigateToChat = { userId -> navigateToChat(userId) })
            }
        }
        if (uiState.contactList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .size(120.dp)
                        .padding(20.dp),

                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NoAccounts,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.size(25.dp))
                Text(
                    text = "No Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontSize = 25.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Save new contacts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun TopBar(
    navigateToNewContact: (String, String) -> Unit,
    navigateBack: () -> Unit,
    onResult: (String, (Boolean) -> Unit) -> Unit,
    navigateToChat: (String) -> Unit
) {
    val qr = LocalQRCode.current
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(horizontal = 20.dp),
        title = { Text(text = "Contacts") },
        navigationIcon = {
            IconButton(
                onClick = navigateBack
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back",
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    qr.startScanning(
                        onScanResult = { scanResult ->
                            val parts = scanResult.split(';')
                            if (parts.size != 2) {
                                debug(parts.toString())
                                return@startScanning
                            }
                            onResult(parts[0]) { saved ->
                                if (saved) navigateToChat(parts[0])
                                else navigateToNewContact(parts[0], parts[1])
                            }
                        }
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Scanner",
                )
            }
        },
    )
}

