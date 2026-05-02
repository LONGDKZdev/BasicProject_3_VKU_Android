package com.vohuy.mixueapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vohuy.mixueapp.ui.screens.CartScreen
import com.vohuy.mixueapp.ui.screens.HomeScreen
import com.vohuy.mixueapp.ui.screens.LoginScreen
import com.vohuy.mixueapp.ui.screens.OrderHistoryScreen
import com.vohuy.mixueapp.ui.screens.ProductDetailScreen
import com.vohuy.mixueapp.ui.screens.RegisterScreen
import com.vohuy.mixueapp.ui.screens.admin.AdminAddProductScreen
import com.vohuy.mixueapp.ui.viewmodel.CartViewModel
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.ui.viewmodel.ProductViewModel

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val authVm: AuthViewModel = viewModel()

    // If already logged in, skip login.
    val startDestination = if (authVm.repositoryIsUserLoggedIn()) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) { LoginScreen(navController, authVm) }
        composable(Routes.REGISTER) { RegisterScreen(navController, authVm) }
        composable(Routes.HOME) { entry ->
            // Shared VMs scoped to HOME graph (so Cart and ProductDetail can share same cart)
            viewModel<CartViewModel>(entry)
            HomeScreen(navController)
        }

        composable(Routes.CART) {
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }
            val cartVm: CartViewModel = viewModel(parentEntry)
            CartScreen(navController, cartVm)
        }
        composable(Routes.ORDER_HISTORY) { OrderHistoryScreen(navController) }

        // Phase 2: Admin create product + upload image to Supabase
        composable(Routes.ADMIN_ADD_PRODUCT) { AdminAddProductScreen(navController) }

        composable(Routes.PRODUCT_DETAIL) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString(Routes.PRODUCT_ID) ?: ""
            val parentEntry = remember(navController.currentBackStackEntry) {
                navController.getBackStackEntry(Routes.HOME)
            }
            val cartVm: CartViewModel = viewModel(parentEntry)
            val productVm: ProductViewModel = viewModel()
            ProductDetailScreen(
                navController = navController,
                productId = productId,
                productViewModel = productVm,
                cartViewModel = cartVm,
            )
        }
    }
}

