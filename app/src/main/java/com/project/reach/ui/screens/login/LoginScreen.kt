package com.project.reach.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.reach.ui.navigation.NavigationDestination

object LoginScreenDestination: NavigationDestination {
    override val route: String
        get() = "login"
}

@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel = viewModel(),
    navigateToHome: ()-> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    40.dp,
                    alignment = Alignment.CenterVertically
                ),
            ){
                Text(
                    text = "REACH",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(10.dp),
                )
                OutlinedTextField(
                    value = uiState.user.username,
                    onValueChange = { viewModel.onInputChange(it) },
                    placeholder = { Text("Enter your name") },
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .padding(0.dp, 10.dp),
                )
                Button(
                    onClick = { navigateToHome() }
                ) {
                    Text(
                        text = "Begin",
                        fontSize = 20.sp,
                    )
                }
            }
        }
}
