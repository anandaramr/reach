package com.project.reach.ui.screens.newContact


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

object NewContactScreenDestination: NavigationDestination {
    override val route: String = "newContact/{userId}/{username}"
    fun createRoute(userId: String, username: String) = "newContact/$userId/$username"
}

@Composable
fun NewContactScreen(
    viewModel: NewContactViewModel = hiltViewModel(),
    navigateToChat: (String) -> Unit,
    navigateBack: () -> Unit,
    userId: String,
    username: String,
    ) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.storeUserDataOnLoad(userId, username)
        viewModel.error.collect { error ->
            debug(error)
            snackbarHostState.showSnackbar(error)
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(userId, navigateBack, viewModel::addToContact, navigateToChat) },
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
            AvatarIcon(username.firstOrNull() ?: ' ', AvatarIconSize.LARGE)
            Spacer(modifier = Modifier.size(8.dp))
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = username,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = userId,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 15.dp)
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
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
                            viewModel.addToContact()
                            navigateToChat(uiState.userId)
                        },
                    ),
                )
            }


        }
    }
}

@Composable
fun TopBar(userId:String, navigateBack: () -> Unit, updateNickname: ()-> Unit,navigateToChat: (String)->Unit ) {
    val qr = LocalQRCode.current
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(horizontal = 20.dp),
        title = { Text(text = "Add new Contact") },
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
                onClick = { updateNickname(); navigateToChat(userId) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Done",
                )
            }
        },
    )
}