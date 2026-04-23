package com.vohuy.mixueapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vohuy.mixueapp.ui.screens.CartScreen
import com.vohuy.mixueapp.ui.screens.HomeScreen
import com.vohuy.mixueapp.ui.screens.LoginScreen
import com.vohuy.mixueapp.ui.screens.OrderHistoryScreen
import com.vohuy.mixueapp.ui.screens.ProductDetailScreen
import com.vohuy.mixueapp.ui.screens.RegisterScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("cart") { CartScreen(navController) }
        composable("order_history") { OrderHistoryScreen(navController) }
        composable("product_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(navController, productId)
        }
    }
}

