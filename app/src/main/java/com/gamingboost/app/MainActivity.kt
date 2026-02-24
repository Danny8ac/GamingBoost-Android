package com.gamingboost.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gamingboost.app.data.TokenStore
import com.gamingboost.app.ui.*
import com.gamingboost.app.ui.theme.GamingBoostTheme

class MainActivity : ComponentActivity() {

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GamingBoostTheme {
                val nc = rememberNavController()
                navController = nc

                val context = LocalContext.current
                val tokenStore = remember { TokenStore(context) }

                val start = if (tokenStore.getToken().isNullOrBlank()) "login" else "home"

                NavHost(navController = nc, startDestination = start) {
                    composable("login") { LoginScreen(nc) }
                    composable("home") { HomeScreen(nc) }
                    composable("boosts") { BoostsScreen(nc) }
                    composable("my_boosts") { MyBoostsScreen(nc) }

                    // ✅ Ruta normal (cuando entras desde Home)
                    composable("orders") { OrdersScreen(nc, refresh = "0", orderId = "", status = "") }

                    // ✅ Ruta especial para cuando regreses de la pasarela (deep link)
                    composable("orders?refresh={refresh}&order_id={order_id}&status={status}") { backStackEntry ->
                        val refresh = backStackEntry.arguments?.getString("refresh") ?: "0"
                        val orderId = backStackEntry.arguments?.getString("order_id") ?: ""
                        val status = backStackEntry.arguments?.getString("status") ?: ""
                        OrdersScreen(nc, refresh = refresh, orderId = orderId, status = status)
                    }
                }
            }
        }

        // Si la app se abrió desde el link (por ejemplo después de pagar/cancelar)
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Si la app ya estaba abierta y llega el deep link
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri = intent?.data ?: return

        // Esperamos algo como:
        // gamingboost://payment-result?order_id=3&status=paid
        if (data.scheme == "gamingboost" && data.host == "payment-result") {
            val orderId = data.getQueryParameter("order_id") ?: ""
            val status = data.getQueryParameter("status") ?: ""

            navController?.navigate("orders?refresh=1&order_id=$orderId&status=$status") {
                launchSingleTop = true
            }
        }
    }
}