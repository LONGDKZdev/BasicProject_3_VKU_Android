package com.vohuy.mixueapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vohuy.mixueapp.data.model.Order
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.ui.viewmodel.OrderViewModel
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.formatPrice
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderViewModel? = null
) {
    val vm = viewModel ?: viewModel<OrderViewModel>()
    val orders by vm.orders.observeAsState(emptyList())
    val isLoading by vm.isLoading.observeAsState(false)
    val errorMessage by vm.errorMessage.observeAsState()

    val authVm: AuthViewModel = viewModel()
    val currentUser by authVm.currentUser.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authVm.fetchCurrentUser()
    }

    LaunchedEffect(currentUser?.id) {
        val uid = currentUser?.id
        if (!uid.isNullOrBlank()) {
            vm.loadUserOrders(uid)
        }
    }

    // Lắng nghe và hiển thị lỗi nếu có vấn đề từ Firestore
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotBlank()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                vm.clearMessages()
            }
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

    val confirmedOrders = orders.filter { it.status in listOf(Constants.ORDER_STATUS_CONFIRMED, Constants.ORDER_STATUS_DELIVERING, Constants.ORDER_STATUS_DONE) }
    val pendingOrders = orders.filter { it.status == Constants.ORDER_STATUS_PENDING }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch Sử Đơn Hàng") },
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
        // SỬ DỤNG TRỰC TIẾP BOX THAY VÌ LỒNG VÀO COLUMN ĐỂ TRÁNH LỖI CHIỀU CAO 0DP
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
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 📦 CONFIRMED ORDERS (Đã xác thực)
                    if (confirmedOrders.isNotEmpty()) {
                        item {
                            Text(
                                "✅ Đã Xác Thực",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        items(confirmedOrders) { order ->
                            OrderCard(order)
                        }
                    }

                    // ⏳ PENDING ORDERS (Chờ xác thực) - ĐẶT Ở DƯỚI
                    if (pendingOrders.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "⏳ Chờ Xác Thực",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
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
                }
            }

            // 🔄 Pull refresh indicator at the top
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val chipScheme = MaterialTheme.colorScheme
    val statusText = remember(order.status) { getStatusText(order.status) }
    val (chipContainer, chipLabel) = remember(order.status, chipScheme) {
        statusChipColors(order.status, chipScheme)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Đơn #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        order.orderDate,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                AssistChip(
                    onClick = { },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = chipContainer,
                        labelColor = chipLabel
                    ),
                    label = {
                        Text(
                            statusText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Order Items Summary
            Text(
                "${order.items.size} sản phẩm",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tổng Cộng:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    order.totalPrice.formatPrice(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 🆕 Pending Order Card - Hiển thị chi tiết cho đơn chờ xác thực và nút Hủy
 */
@Composable
fun PendingOrderCard(order: Order, onCancelClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "⏳ Chờ Xác Thực - Đơn #${order.id.take(8)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        order.orderDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )

            // Items List
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "${item.quantity}x ${item.productName}", fontSize = 14.sp)
                    Text(text = item.price.formatPrice(), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer & Cancel Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tổng: ${order.totalPrice.formatPrice()}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                // Nút Hủy Đơn
                Button(
                    onClick = { onCancelClick(order.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hủy đơn", color = androidx.compose.ui.graphics.Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info Text
            Text(
                "📌 Đơn hàng của bạn đang chờ nhân viên xác nhận. Vui lòng chờ trong giây lát.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun statusChipColors(
    status: String,
    c: ColorScheme
): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return when (status) {
        Constants.ORDER_STATUS_PENDING, Constants.ORDER_STATUS_DELIVERING ->
            c.tertiaryContainer to c.onTertiaryContainer

        Constants.ORDER_STATUS_DONE ->
            c.primaryContainer to c.onPrimaryContainer

        Constants.ORDER_STATUS_CANCELLED ->
            c.errorContainer to c.onErrorContainer

        Constants.ORDER_STATUS_CONFIRMED ->
            c.secondaryContainer to c.onSecondaryContainer

        else ->
            c.surfaceVariant to c.onSurfaceVariant
    }
}

private fun getStatusText(status: String): String {
    return when (status) {
        Constants.ORDER_STATUS_PENDING -> "Chờ xác nhận"
        Constants.ORDER_STATUS_CONFIRMED -> "Đã xác nhận"
        Constants.ORDER_STATUS_DELIVERING -> "Đang giao"
        Constants.ORDER_STATUS_DONE -> "Đã giao"
        Constants.ORDER_STATUS_CANCELLED -> "Đã hủy"
        else -> "Không rõ"
    }
}