package com.vohuy.mixueapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vohuy.mixueapp.data.model.Order
import com.vohuy.mixueapp.ui.components.ToastMessageHandler
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.ui.viewmodel.OrderViewModel
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderViewModel? = null
) {
    val vm = viewModel ?: viewModel<OrderViewModel>()
    val orders by vm.orders.observeAsState(emptyList())
    val isLoading by vm.isLoading.observeAsState(false)

    val authVm: AuthViewModel = viewModel()
    val currentUser by authVm.currentUser.observeAsState()
    LocalContext.current
    ToastMessageHandler(vm, authVm)

    LaunchedEffect(Unit) {
        authVm.fetchCurrentUser()
    }

    LaunchedEffect(currentUser?.id) {
        val uid = currentUser?.id
        if (!uid.isNullOrBlank()) {
            vm.loadUserOrders(uid)
        }
    }

    // 🔄 Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            val uid = currentUser?.id
            if (!uid.isNullOrBlank()) {
                vm.loadUserOrders(uid)
            }
            isRefreshing = false
        }
    )

    // Phân loại: Đơn đang chờ xử lý và Đơn đã có phản hồi (Duyệt/Giao/Hoàn thành/Hủy)
    val pendingOrders = orders.filter { it.status == Constants.ORDER_STATUS_PENDING }
    val historyOrders = orders.filter { it.status != Constants.ORDER_STATUS_PENDING }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch Sử Đơn Hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            if (isLoading && orders.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (orders.isEmpty()) {
                Text(
                    text = "Chưa có đơn hàng nào",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ⏳ ĐƠN CHỜ XÁC THỰC (Ưu tiên hiển thị trên cùng)
                    if (pendingOrders.isNotEmpty()) {
                        item {
                            Text(
                                "⏳ Đang Chờ Xử Lý",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(pendingOrders) { order ->
                            PendingOrderCard(
                                order = order,
                                onCancelClick = { orderId ->
                                    vm.updateOrderStatus(orderId, Constants.ORDER_STATUS_CANCELLED)
                                }
                            )
                        }
                    }

                    // 📦 CÁC ĐƠN HÀNG KHÁC (Đã duyệt, Đang giao, Hoàn thành, Hủy)
                    if (historyOrders.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "📦 Đơn Hàng Của Bạn",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(historyOrders) { order ->
                            OrderCard(order)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

/**
 * 🎨 THẺ ĐƠN HÀNG LỊCH SỬ (Có Thanh Tiến Độ)
 */
@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Đơn #${order.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        order.orderDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = order.totalPrice.formatPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Thông tin Giao hàng & Thanh toán
            val paymentMethodText =
                if (order.paymentMethod == Constants.PAYMENT_METHOD_BANK_TRANSFER) "💳 Chuyển khoản" else "💵 Tiền mặt"
            val addressText =
                if (!order.address.isBlank()) "📍 ${order.address}" else "📍 Mua tại quầy"

            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = paymentMethodText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = addressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            // Thanh tiến độ đơn hàng
            OrderProgressBar(currentStatus = order.status)
        }
    }
}

/**
 * 🎨 THẺ ĐƠN CHỜ XÁC THỰC (Có nút Hủy)
 */
@Composable
fun PendingOrderCard(order: Order, onCancelClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Column {
                    Text(
                        "⏳ Đơn #${order.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        order.orderDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            )

            // Thông tin Giao hàng & Thanh toán
            val paymentMethodText =
                if (order.paymentMethod == Constants.PAYMENT_METHOD_BANK_TRANSFER) "💳 Chuyển khoản (Đang chờ Admin xác nhận tiền)" else "💵 Tiền mặt"
            val addressText =
                if (!order.address.isBlank()) "📍 ${order.address}" else "📍 Khách chưa nhập địa chỉ"

            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = paymentMethodText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = addressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            // Nút Hủy Đơn & Tổng tiền
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.totalPrice.formatPrice(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { onCancelClick(order.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hủy đơn", color = Color.White)
                }
            }
        }
    }
}

/**
 * 🚀 COMPONENT: Thanh Tiến Độ Đơn Hàng Trực Quan
 */
@Composable
fun OrderProgressBar(currentStatus: String) {
    if (currentStatus == Constants.ORDER_STATUS_CANCELLED) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "❌ Đơn hàng này đã bị hủy",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    val steps = listOf(
        Constants.ORDER_STATUS_PENDING to "Chờ duyệt",
        Constants.ORDER_STATUS_CONFIRMED to "Đã duyệt",
        Constants.ORDER_STATUS_DELIVERING to "Đang giao",
        Constants.ORDER_STATUS_DONE to "Hoàn thành"
    )

    val currentIndex =
        steps.indexOfFirst { it.first == currentStatus }.let { if (it == -1) 0 else it }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, pair ->
            val isCompleted = index <= currentIndex
            val isCurrent = index == currentIndex
            val stepColor = if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Vòng tròn (Trạng thái)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(if (isCompleted) stepColor else Color.Transparent, CircleShape)
                        .border(2.dp, stepColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Nhãn mô tả
                Text(
                    text = pair.second,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }

            // Đường nối giữa các bước
            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(bottom = 16.dp), // Canh cho đường gạch nằm giữa các vòng tròn
                    color = if (index < currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray,
                    thickness = 2.dp
                )
            }
        }
    }
}