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
import androidx.compose.ui.unit.dp
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

// Khởi tạo context để dùng cho Toast
    ToastMessageHandler(authVm, orderVm, viewModel)
    val context = LocalContext.current
    val sharedPreferences =
        context.getSharedPreferences("MixuePrefs", android.content.Context.MODE_PRIVATE)

    // Các biến cho form giao hàng
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var addressInput by remember {
        mutableStateOf(
            sharedPreferences.getString("saved_address", "") ?: ""
        )
    }

    LaunchedEffect(Unit) {
        authVm.fetchCurrentUser()
    }

    // Ensure current user is fetched (covers cold start)
    LaunchedEffect(currentUser) {
        if (phoneInput.isBlank() && !currentUser?.phoneNumber.isNullOrBlank()) {
            phoneInput = currentUser?.phoneNumber ?: ""
        }
    }

    // On successful checkout -> clear cart and go to order history
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
                    tonalElevation = 6.dp,
                    shadowElevation = 12.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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
                            Text(
                                totalPrice.formatPrice(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val uid = authVm.getCurrentUserId()
                                if (uid.isNullOrBlank()) {
                                    orderVm.setError("Bạn cần đăng nhập để thanh toán")
                                } else {
                                    // Cập nhật SĐT mặc định trước khi bật Dialog
                                    phoneInput = currentUser?.phoneNumber ?: ""
                                    showCheckoutDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
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
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Giỏ hàng của bạn đang trống",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(items = cartItems, key = { it.id }) { item ->
                    CartItemCard(item) { id ->
                        viewModel.removeFromCart(id)
                    }
                }
            }
        }
    }

    // HỘP THOẠI XÁC NHẬN GIAO HÀNG & CHUYỂN KHOẢN
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
                    Spacer(modifier = Modifier.height(8.dp))
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🆕 Bảng Hướng Dẫn Chuyển Khoản
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "💳 Hướng Dẫn Chuyển Khoản",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
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
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
                                    containerColor = androidx.compose.ui.graphics.Color(
                                        0xFF1976D2
                                    )
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
                        // FIX: Lấy UID trực tiếp từ Auth (không bao giờ null nếu đã đăng nhập)
                        val uid = authVm.getCurrentUserId() ?: return@Button
                        val phone = phoneInput.trim()
                        val address = addressInput.trim()

                        // 1. Kiểm tra số điện thoại (phải có ít nhất 10 số)
                        if (phone.length < 10 || !phone.all { it.isDigit() }) {
                            orderVm.setError("Số điện thoại không hợp lệ (ít nhất 10 chữ số)")
                            return@Button
                        }

                        // 2. Kiểm tra địa chỉ (không được nhập quá ngắn)
                        if (address.length < 5) {
                            orderVm.setError("Vui lòng nhập địa chỉ giao hàng cụ thể hơn")
                            return@Button
                        }

                        // 3. Lưu địa chỉ này vào bộ nhớ máy cho lần mua sau
                        sharedPreferences.edit().putString("saved_address", address).apply()

                        // 4. LƯU THÔNG TIN SĐT/ĐỊA CHỈ LÊN FIREBASE (Lần trước bị thiếu)
                        authVm.updateDeliveryInfo(phone, address)

                        // 5. Đóng hộp thoại
                        showCheckoutDialog = false

                        // 6. Chốt đơn chuyển khoản
                        orderVm.createOrder(
                            userId = uid,
                            items = cartItems,
                            // Đổi dòng gán customerName cũ thành thế này:
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                model = item.imageUrl,
                contentDescription = item.productName,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            // Product Info
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "SL: ${item.quantity} x ${item.price.formatPrice()}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Thành tiền: ${(item.quantity * item.price).formatPrice()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
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