package com.vohuy.mixueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vohuy.mixueapp.data.model.OrderItem
import com.vohuy.mixueapp.ui.components.ToastMessageHandler
import com.vohuy.mixueapp.ui.navigation.Routes
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.ui.viewmodel.CartViewModel
import com.vohuy.mixueapp.ui.viewmodel.OrderViewModel
import com.vohuy.mixueapp.utils.formatPrice
import com.vohuy.mixueapp.utils.sdp

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

    ToastMessageHandler(authVm, orderVm, viewModel)
    val context = LocalContext.current
    val sharedPreferences =
        context.getSharedPreferences("MixuePrefs", android.content.Context.MODE_PRIVATE)

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var addressInput by remember {
        mutableStateOf(sharedPreferences.getString("saved_address", "") ?: "")
    }

    LaunchedEffect(Unit) {
        authVm.fetchCurrentUser()
    }

    LaunchedEffect(currentUser) {
        if (phoneInput.isBlank() && !currentUser?.phoneNumber.isNullOrBlank()) {
            phoneInput = currentUser?.phoneNumber ?: ""
        }
    }

    LaunchedEffect(createdOrderId, orderSuccess) {
        if (!createdOrderId.isNullOrBlank()) {
            viewModel.clearCart()
            navController.navigate(Routes.ORDER_HISTORY)
            orderVm.resetOrderState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ Hàng") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 6.sdp,
                    shadowElevation = 12.sdp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.sdp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tổng cộng",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // ĐÃ SỬA: Ép cứng giá tiền không bao giờ được xuống hàng
                            Text(
                                totalPrice.formatPrice(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(modifier = Modifier.height(12.sdp))

                        Button(
                            onClick = {
                                val uid = authVm.getCurrentUserId()
                                if (uid.isNullOrBlank()) {
                                    orderVm.setError("Bạn cần đăng nhập để thanh toán")
                                } else {
                                    phoneInput = currentUser?.phoneNumber ?: ""
                                    showCheckoutDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.sdp),
                            shape = RoundedCornerShape(16.sdp)
                        ) {
                            Text(
                                "Thanh Toán",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.sdp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.sdp)
                    )
                    Spacer(modifier = Modifier.height(12.sdp))
                    Text(
                        text = "Giỏ hàng của bạn đang trống",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.sdp))
                    Text(
                        text = "Hãy thêm món bạn thích để bắt đầu đặt hàng.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(12.sdp),
                verticalArrangement = Arrangement.spacedBy(10.sdp),
                contentPadding = PaddingValues(bottom = 96.sdp)
            ) {
                items(items = cartItems, key = { item -> item.id }) { item ->
                    CartItemCard(
                        item = item,
                        onDelete = { id ->
                            viewModel.removeFromCart(id)
                        }
                    )
                }
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Thông tin & Thanh toán", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Số điện thoại liên hệ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        )
                    )
                    Spacer(modifier = Modifier.height(8.sdp))
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Địa chỉ nhận hàng cụ thể") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                        )
                    )

                    Spacer(modifier = Modifier.height(16.sdp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.sdp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.sdp)) {
                            Text(
                                "💳 Hướng Dẫn Chuyển Khoản",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.sdp))
                            Text("Ngân hàng: MB Bank", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "STK: 1234567890",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Chủ TK: MIXUE DA NANG",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Số tiền: ${totalPrice.formatPrice()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(8.sdp))
                            Button(
                                onClick = {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Đang mở ứng dụng Ngân Hàng...",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2)
                                )
                            ) {
                                Text(
                                    "🏦 Mở App Ngân Hàng",
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = authVm.getCurrentUserId() ?: return@Button
                        val phone = phoneInput.trim()
                        val address = addressInput.trim()

                        if (phone.length < 10 || !phone.all { it.isDigit() }) {
                            orderVm.setError("Số điện thoại không hợp lệ (ít nhất 10 chữ số)")
                            return@Button
                        }
                        if (address.length < 5) {
                            orderVm.setError("Vui lòng nhập địa chỉ giao hàng cụ thể hơn")
                            return@Button
                        }

                        sharedPreferences.edit().putString("saved_address", address).apply()
                        authVm.updateDeliveryInfo(phone, address)
                        showCheckoutDialog = false

                        orderVm.createOrder(
                            userId = uid,
                            items = cartItems,
                            customerName = currentUser?.fullName?.takeIf { it.isNotBlank() }
                                ?: "Khách hàng",
                            phoneNumber = phone,
                            address = address
                        )
                    }
                ) {
                    Text("Đã Chuyển Khoản & Lên Đơn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun CartItemCard(item: OrderItem, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.sdp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.sdp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.sdp),
            horizontalArrangement = Arrangement.spacedBy(12.sdp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.productName,
                modifier = Modifier
                    .size(76.sdp)
                    .clip(RoundedCornerShape(12.sdp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.sdp))

                // ĐÃ SỬA: Hạ font xuống labelMedium cho nhỏ gọn
                Text(
                    "SL: ${item.quantity} x ${item.price.formatPrice()}",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(6.sdp))

                // ĐÃ SỬA: Đổi chữ "Thành tiền" thành "Tổng" và hạ font xuống bodyMedium
                Text(
                    "Tổng: ${(item.quantity * item.price).formatPrice()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            IconButton(onClick = { onDelete(item.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}