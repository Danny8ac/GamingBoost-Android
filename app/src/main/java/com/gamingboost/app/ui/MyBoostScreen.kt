package com.gamingboost.app.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gamingboost.app.data.TokenStore
import com.gamingboost.app.network.ApiClient
import com.gamingboost.app.network.Boost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBoostsScreen(navController: NavController) {

    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var boosts by remember { mutableStateOf<List<Boost>>(emptyList()) }

    LaunchedEffect(Unit) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            navController.navigate("login") { popUpTo("my_boosts") { inclusive = true } }
            return@LaunchedEffect
        }

        try {
            val res = ApiClient.api.myBoosts("Bearer $token")
            if (res.isSuccessful && res.body() != null) {
                boosts = res.body()!!
            } else {
                error = "Error (${res.code()})"
            }
        } catch (e: Exception) {
            error = "Error: ${e.message}"
            Log.e("API", "MY BOOSTS ERROR: ${e.message}", e)
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Boosts") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Atrás") }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            when {
                loading -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                boosts.isEmpty() -> {
                    Text("Aún no tienes boosts comprados.")
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(boosts, key = { it.id }) { b ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(b.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(b.description)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Comprado ✅")
                                    Text("Cantidad total: ${b.qty_total ?: 0}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}