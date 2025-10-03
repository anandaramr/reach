package com.project.reach.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.project.reach.ui.navigation.NavigationDestination
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


object ChatScreenDestination : NavigationDestination {
    override val route: String = "chat"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatScreenViewModel = viewModel<ChatScreenViewModel>(),
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    Scaffold(
        modifier = modifier
            .imePadding(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(80.dp),
                    title = {
                        Text(
                            text = uiState.user.username,
                        )
                    },

                    navigationIcon = {
                        IconButton(
                            onClick = navigateBack,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .padding(innerPadding)
                .imePadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .padding(top = 25.dp)
                    .weight(1f),
            ) {
                items(uiState.messageList.reversed()) { message ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement =
                            if (message.isFromSelf)
                                Arrangement.End
                            else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(30.dp),
                            border = BorderStroke(
                                2.dp,
                                if (message.isFromSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .widthIn(max = 270.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent,
                                contentColor =
                                    if (message.isFromSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            ),
                        ) {
                            Text(
                                text = message.text,
                                modifier = Modifier
                                    .padding(15.dp, 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Surface (modifier = Modifier.padding(vertical = 10.dp)) {
                OutlinedTextField(
                    value = uiState.messageText,
                    onValueChange = { viewModel.onInputChange(it) },
                    placeholder = { Text("Enter the message") },
                    shape = RoundedCornerShape(50.dp),
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(0.dp, 10.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.sendMessage(uiState.messageText) },
                            enabled = !uiState.messageText.isEmpty(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Send",
                            )
                        }
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add",
                            )
                        }
                    }
                )
            }
        }
    }
}

