package com.vohuy.mixueapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vohuy.mixueapp.ui.viewmodel.CartViewModel
import com.vohuy.mixueapp.ui.viewmodel.ProductViewModel
import com.vohuy.mixueapp.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    productViewModel: ProductViewModel? = null,
    cartViewModel: CartViewModel? = null,
) {
    val productVm = productViewModel ?: viewModel<ProductViewModel>()
    val cartVm = cartViewModel ?: viewModel<CartViewModel>()

    // Load product by id once when entering screen / when id changes
    LaunchedEffect(productId) {
        if (productId.isNotBlank()) {
            productVm.loadProductById(productId)
        }
    }

    val product by productVm.selectedProduct.observeAsState(null)
    var quantity by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi Tiết Sản Phẩm") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (product == null) {
                Text(
                    text = "Đang tải sản phẩm...",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Product Image
            AsyncImage(
                model = product?.imageUrl.orEmpty(),
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Product Name
            Text(
                product?.name ?: "",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Product Price
            Text(
                (product?.price ?: 0.0).formatPrice(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Product Description
            Text(
                "Mô Tả Sản Phẩm",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                product?.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quantity Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Số Lượng:", style = MaterialTheme.typography.bodyMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    Text(quantity.toString(), style = MaterialTheme.typography.bodyMedium)

                    IconButton(onClick = { quantity++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Add to Cart Button
            Button(
                onClick = {
                    val p = product
                    if (p != null) {
                        cartVm.addItem(p, quantity)
                        navController.navigate("cart")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                ,
                enabled = product != null
            ) {
                Text("Thêm Vào Giỏ Hàng")
            }
        }
    }
}


