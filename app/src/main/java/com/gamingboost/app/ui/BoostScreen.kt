package com.gamingboost.app.ui

import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gamingboost.app.data.TokenStore
import com.gamingboost.app.network.ApiClient
import com.gamingboost.app.network.Boost
import com.gamingboost.app.network.CreateOrderItem
import com.gamingboost.app.network.CreateOrderRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostsScreen(navController: NavController) {

    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var boosts by remember { mutableStateOf<List<Boost>>(emptyList()) }

    LaunchedEffect(Unit) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            navController.navigate("login") { popUpTo("boosts") { inclusive = true } }
            return@LaunchedEffect
        }

        try {
            val res = ApiClient.api.boosts("Bearer $token")
            if (res.isSuccessful && res.body() != null) {
                boosts = res.body()!!
            } else {
                error = "Error al cargar productos (${res.code()})"
            }
        } catch (e: Exception) {
            error = "Error: ${e.message}"
            Log.e("API", "BOOSTS ERROR: ${e.message}", e)
        } finally {
            loading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
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
                    Text("No hay productos disponibles.")
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(boosts, key = { it.id }) { b ->

                            var qty by remember(b.id) { mutableStateOf(1) }
                            var provider by remember(b.id) { mutableStateOf("stripe") } // stripe|mercadopago|paypal

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {

                                    Text(b.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(b.description)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Precio: $${b.price}", style = MaterialTheme.typography.bodyLarge)

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        OutlinedButton(onClick = { if (qty > 1) qty-- }) { Text("-") }
                                        Text("Cantidad: $qty")
                                        OutlinedButton(onClick = { if (qty < 99) qty++ }) { Text("+") }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Método de pago:")
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { provider = "stripe" },
                                            modifier = Modifier.weight(1f)
                                        ) { Text(if (provider == "stripe") "Stripe ✅" else "Stripe") }

                                        OutlinedButton(
                                            onClick = { provider = "mercadopago" },
                                            modifier = Modifier.weight(1f)
                                        ) { Text(if (provider == "mercadopago") "MP ✅" else "MP") }

                                        OutlinedButton(
                                            onClick = { provider = "paypal" },
                                            modifier = Modifier.weight(1f)
                                        ) { Text(if (provider == "paypal") "PayPal ✅" else "PayPal") }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val token = tokenStore.getToken()
                                                if (token.isNullOrBlank()) {
                                                    snackbarHostState.showSnackbar("Sesión expirada")
                                                    navController.navigate("login") {
                                                        popUpTo("boosts") { inclusive = true }
                                                    }
                                                    return@launch
                                                }

                                                try {
                                                    // 1) Crear pedido
                                                    val createRes = ApiClient.api.createOrder(
                                                        "Bearer $token",
                                                        CreateOrderRequest(
                                                            provider = provider,
                                                            items = listOf(
                                                                CreateOrderItem(boost_id = b.id, qty = qty)
                                                            )
                                                        )
                                                    )

                                                    if (!createRes.isSuccessful || createRes.body() == null) {
                                                        val err = createRes.errorBody()?.string()
                                                        Log.e("API", "createOrder ${createRes.code()} body=$err")
                                                        snackbarHostState.showSnackbar("❌ No se pudo crear pedido (${createRes.code()})")
                                                        return@launch
                                                    }

                                                    val orderId = createRes.body()!!.order_id

                                                    // 2) Pedir checkout_url (con token firmado)
                                                    val payRes = ApiClient.api.payOrder("Bearer $token", orderId)

                                                    if (!payRes.isSuccessful || payRes.body() == null) {
                                                        val err = payRes.errorBody()?.string()
                                                        Log.e("API", "payOrder ${payRes.code()} body=$err")
                                                        snackbarHostState.showSnackbar("❌ No se pudo iniciar pago (${payRes.code()})")
                                                        return@launch
                                                    }

                                                    val checkoutUrl = payRes.body()!!.checkout_url
                                                    Log.d("PAY", "checkoutUrl=$checkoutUrl") // ✅ confirma que trae token=

                                                    snackbarHostState.showSnackbar("Abriendo pasarela…")

                                                    // 3) Abrir pasarela en Custom Tabs
                                                    val intent = CustomTabsIntent.Builder().build()
                                                    intent.launchUrl(context, Uri.parse(checkoutUrl))

                                                } catch (e: Exception) {
                                                    Log.e("API", "PAY FLOW ERROR: ${e.message}", e)
                                                    snackbarHostState.showSnackbar("❌ Error: ${e.message}")
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Pagar")
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tip: Después de pagar, entra a Mis pedidos y dale Recargar.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}