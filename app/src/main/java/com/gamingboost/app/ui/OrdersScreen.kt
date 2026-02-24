package com.gamingboost.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gamingboost.app.data.TokenStore
import com.gamingboost.app.network.ApiClient
import com.gamingboost.app.network.OrderDto
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    navController: NavController,
    refresh: String,
    orderId: String,
    status: String
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<OrderDto>>(emptyList()) }

    fun loadOrders() {
        scope.launch {
            val token = tokenStore.getToken()
            if (token.isNullOrBlank()) {
                snackbarHostState.showSnackbar("Sesión expirada")
                navController.navigate("login") { popUpTo("orders") { inclusive = true } }
                return@launch
            }

            loading = true
            error = null

            try {
                val res = ApiClient.api.myOrders("Bearer $token")
                if (res.isSuccessful && res.body() != null) {
                    orders = res.body()!!
                } else {
                    error = "Error al cargar pedidos (${res.code()})"
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    // Primera carga
    LaunchedEffect(Unit) { loadOrders() }

    // ✅ Si vienes del deep link (refresh=1), recarga y avisa
    LaunchedEffect(refresh, orderId, status) {
        if (refresh == "1") {
            loadOrders()
            val msg = when (status) {
                "paid" -> "✅ Pedido #$orderId pagado"
                "cancelled" -> "❌ Pedido #$orderId cancelado"
                "failed" -> "⚠️ Pedido #$orderId fallido"
                "pending_payment" -> "🟡 Pedido #$orderId pendiente"
                else -> "Pedido #$orderId: $status"
            }
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis pedidos") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Atrás") }
                },
                actions = {
                    TextButton(onClick = { loadOrders() }) { Text("Recargar") }
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                orders.isEmpty() -> {
                    Text("Aún no tienes pedidos.")
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(orders, key = { _, it -> it.id }) { idx, o ->
                            // ✅ Como vienen newest -> oldest, el más viejo debe ser #1
                            val userNumber = orders.size - idx
                            OrderCardNice(o, userNumber = userNumber)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCardNice(order: OrderDto, userNumber: Int) {
    val money = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val totalMx = order.total_amount / 100.0

    Card {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ✅ Pedido #1 = el más viejo (primero creado)
                Text("Pedido #$userNumber", style = MaterialTheme.typography.titleMedium)
                StatusChip(order.status)
            }

            Spacer(Modifier.height(4.dp))

            // (Opcional, pero útil) ID real del sistema
            Text("ID interno: #${order.id}", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(6.dp))
            Text("Proveedor: ${order.provider}")
            Text("Total: ${money.format(totalMx)}")

            Spacer(Modifier.height(10.dp))
            Text("Items:", style = MaterialTheme.typography.labelLarge)
            order.items.forEach { itx ->
                Text("• Boost ${itx.boost_id} x${itx.qty}")
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {

    val (label, containerColor, contentColor) = when (status) {
        "paid" -> Triple(
            "Pagado",
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary
        )
        "pending_payment" -> Triple(
            "Pendiente",
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary
        )
        "cancelled" -> Triple(
            "Cancelado",
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error
        )
        "failed" -> Triple(
            "Fallido",
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error
        )
        else -> Triple(
            status,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}