package com.project.reach.ui.screens.viewContact

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.project.reach.ui.app.LocalQRCode
import com.project.reach.ui.components.AvatarIcon
import com.project.reach.ui.components.AvatarIconSize
import com.project.reach.ui.navigation.NavigationDestination
import com.project.reach.util.debug

object ViewContactScreenDestination: NavigationDestination {
    override val route: String = "viewContact/{userId}/{username}/{nickname}"
    fun createRoute(userId: String, username: String, nickname: String) = "viewContact/$userId/$username/$nickname"
}

@Composable
fun ViewContactScreen(
    viewModel: ViewContactViewModel = hiltViewModel(),
    navigateToChat: (String) -> Unit,
    navigateBack: () -> Unit,
    userId: String,
    username: String,
    nickname: String,
    ) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.storeUserDataOnLoad(userId, username, nickname)
        viewModel.error.collect { error ->
            debug(error)
            snackbarHostState.showSnackbar(error)
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(userId, navigateBack, viewModel::editContact, navigateToChat, isEditing = uiState.isEditing, setEditingmode = viewModel::setEditingMode) },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(20.dp)
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding((innerPadding))
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(10.dp))
            AvatarIcon(uiState.nickname.firstOrNull() ?: ' ', AvatarIconSize.LARGE)
            Spacer(modifier = Modifier.size(8.dp))
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if(uiState.isEditing)
                    OutlinedTextField(
                        value = uiState.nickname,
                        onValueChange = { viewModel.onNicknameChange(it) },
                        label = { Text("Nickname") },
                        modifier = Modifier,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.editContact()
                                navigateToChat(uiState.userId)
                            },
                        ),
                    )
                else {
                    Text(
                        text = uiState.nickname,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = uiState.username,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Button(
                modifier = Modifier.padding(10.dp),
                onClick = {
                    navigateToChat(uiState.userId)
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
                Text(text = "Message")
            }
        }
    }
}

@Composable
fun TopBar(userId:String, navigateBack: () -> Unit, updateNickname: ()-> Unit,navigateToChat: (String)->Unit, isEditing: Boolean, setEditingmode: (Boolean)->Unit ) {
    val qr = LocalQRCode.current
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(horizontal = 20.dp),
        title = { Text(text = "Contact details") },
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
            if(!isEditing){
                IconButton(
                    onClick = { setEditingmode(true) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                    )
                }
            }
            else{
                IconButton(
                    onClick = { updateNickname(); setEditingmode(false)}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Done",
                    )
                }
            }
        },
    )
}