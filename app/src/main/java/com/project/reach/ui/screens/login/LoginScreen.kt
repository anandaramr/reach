package com.project.reach.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.reach.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object LoginScreenDestination: NavigationDestination {
    override val route: String
        get() = "login"
}

@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel = viewModel(),
    navigateToHome: ()-> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                30.dp,
                alignment = Alignment.CenterVertically
            ),
        ){
            Text(
                text = "Welcome to REACH",
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(10.dp),
            )
            OutlinedTextField(
                value = uiState.user,
                onValueChange = { viewModel.onInputChange(it) },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center
                ),
                placeholder = {
                    Text(
                        text = "Enter your name",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )},
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .padding(0.dp, 10.dp)
                    .fillMaxWidth(0.8f)
                    .align(Alignment.CenterHorizontally)
            )
            Button(
                onClick = {
                    if(uiState.user == "")
                        scope.launch {
                        snackBarHostState.showSnackbar("Username field empty !!!")
                    }
                    else
                    {
                        keyboard?.hide()
                        navigateToHome()
                    }

                }
            ) {
                Text(
                    text = "Begin",
                    fontSize = 15.sp,
                )
            }
        }

    }
}
