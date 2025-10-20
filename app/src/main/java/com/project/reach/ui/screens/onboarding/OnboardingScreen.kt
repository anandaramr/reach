package com.project.reach.ui.screens.onboarding

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.project.reach.ui.navigation.NavigationDestination

object OnboardingScreen : NavigationDestination {
    override val route: String
        get() = "onboarding"
}

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val username by viewModel.username.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errors.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    fun onClick() {
        if(viewModel.submit()) {
            onOnboardingComplete()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                30.dp,
                alignment = Alignment.CenterVertically
            ),
        ) {
            Text(
                text = "welcome to reach!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(10.dp),
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = username,
                onValueChange = viewModel::onUsernameChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center
                ),
                placeholder = {
                    Text(
                        text = "enter your name",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .padding(0.dp, 10.dp)
                    .fillMaxWidth(0.8f)
                    .align(Alignment.CenterHorizontally),
            )
            Button(
                onClick = ::onClick
            ) {
                Text(
                    text = "begin",
                    fontSize = 15.sp,
                )
            }
        }
    }
}