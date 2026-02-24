package com.gamingboost.app.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gamingboost.app.network.ApiClient
import com.gamingboost.app.viewmodel.LoginViewModel
import androidx.compose.ui.platform.LocalContext
import com.gamingboost.app.data.TokenStore
import androidx.compose.runtime.remember

@Composable
fun LoginScreen(navController: NavController) {

    val viewModel: LoginViewModel = viewModel()

    val email = viewModel.email.value
    val password = viewModel.password.value
    val isEnabled = viewModel.isLoginEnabled.value
    val loginError = viewModel.loginError.value
    val loginSuccess = viewModel.loginSuccess.value
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    // ✅ 1) Ping una sola vez al entrar a la pantalla
    LaunchedEffect(Unit) {

        try {
            val res = ApiClient.api.ping()
            Log.d("API", "PING code=${res.code()} body=${res.body()} err=${res.errorBody()?.string()}")
        } catch (e: Exception) {
            Log.e("API", "PING ERROR: ${e.message}", e)
        }
    }

    // ✅ 2) Navegar cuando loginSuccess cambie a true
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            viewModel.token.value?.let { tokenStore.saveToken(it) }

            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "GamingBoost",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.login() },
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 3) Mostrar error (en UI, no en navigate{})
            if (loginError != null) {
                Text(
                    text = loginError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = if (isEnabled) "✅ Listo para iniciar" else "⚠️ Password mínimo 6 caracteres",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}