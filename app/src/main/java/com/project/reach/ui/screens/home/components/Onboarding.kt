package com.project.reach.ui.screens.home.components

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Onboarding(
    username: String,
    onInputChange: (text: String) -> Unit,
    onSubmit: () -> Boolean,
    snackbarHostState: SnackbarHostState
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                value = username,
                onValueChange = onInputChange,
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
                    if (onSubmit()) keyboard?.hide()
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