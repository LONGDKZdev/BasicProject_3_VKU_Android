package com.vohuy.mixueapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vohuy.mixueapp.data.model.OrderItem
import com.vohuy.mixueapp.ui.navigation.Routes
import com.vohuy.mixueapp.ui.viewmodel.CartViewModel
import com.vohuy.mixueapp.ui.viewmodel.OrderViewModel
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = viewModel()
) {
    val cartItems by viewModel.cartItems.observeAsState(emptyList())
    val totalPrice by viewModel.totalPrice.observeAsState(0.0)

    val authVm: AuthViewModel = viewModel()
    val orderVm: OrderViewModel = viewModel()

    val currentUser by authVm.currentUser.observeAsState()
    val createdOrderId by orderVm.createdOrderId.observeAsState()
    val orderSuccess by orderVm.successMessage.observeAsState()
    val orderError by orderVm.errorMessage.observeAsState()

    // Ensure current user is fetched (covers cold start)
    LaunchedEffect(Unit) {
        authVm.fetchCurrentUser()
    }

    // On successful checkout -> clear cart and go to order history
    LaunchedEffect(createdOrderId, orderSuccess) {
        if (!createdOrderId.isNullOrBlank()) {
            viewModel.clearCart()
            navController.navigate(Routes.ORDER_HISTORY)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ Hàng") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Giỏ hàng trống")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(item) { id ->
                            viewModel.removeFromCart(id)
                        }
                    }
                }

                Divider()

                // Total and Checkout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tổng Cộng:",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            totalPrice.formatPrice(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            val uid = currentUser?.id
                            if (uid.isNullOrBlank()) {
                                orderVm.setError("Bạn cần đăng nhập để thanh toán")
                            } else {
                                orderVm.createOrder(uid, cartItems)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Thanh Toán")
                    }

                    if (!orderError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = orderError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: OrderItem, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = item.productImage,
                contentDescription = item.productName,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "SL: ${item.quantity} x ${item.pricePerUnit.formatPrice()}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Thành tiền: ${(item.quantity * item.pricePerUnit).formatPrice()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Delete Button
            IconButton(onClick = { onDelete(item.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}


