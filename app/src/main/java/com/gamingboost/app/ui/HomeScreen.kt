package com.gamingboost.app.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gamingboost.app.data.TokenStore
import com.gamingboost.app.network.ApiClient
import com.gamingboost.app.network.MeResponse
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val scope = rememberCoroutineScope()

    var me by remember { mutableStateOf<MeResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            navController.navigate("login") { popUpTo("home") { inclusive = true } }
            return@LaunchedEffect
        }

        try {
            val res = ApiClient.api.me("Bearer $token")
            if (res.isSuccessful && res.body() != null) {
                me = res.body()
            } else {
                error = "No autorizado (${res.code()})"
                tokenStore.clear()
                navController.navigate("login") { popUpTo("home") { inclusive = true } }
            }
        } catch (e: Exception) {
            error = "Error: ${e.message}"
            Log.e("API", "ME ERROR: ${e.message}", e)
        } finally {
            loading = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("🔥 Bienvenido a GamingBoost", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> Text("Cargando perfil...")
                me != null -> {
                    Text("Nombre: ${me!!.name}")
                    Text("Email: ${me!!.email}")
                }
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("boosts") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Productos") }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("orders") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Mis pedidos") }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val token = tokenStore.getToken()
                        if (!token.isNullOrBlank()) {
                            try { ApiClient.api.logout("Bearer $token") } catch (_: Exception) {}
                        }
                        tokenStore.clear()
                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}