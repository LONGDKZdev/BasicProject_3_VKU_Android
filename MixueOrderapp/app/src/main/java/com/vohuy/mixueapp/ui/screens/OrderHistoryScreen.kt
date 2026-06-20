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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vohuy.mixueapp.data.model.Order
import com.vohuy.mixueapp.data.model.OrderItem
import com.vohuy.mixueapp.ui.components.ToastMessageHandler
import com.vohuy.mixueapp.ui.navigation.Routes
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.ui.viewmodel.CartViewModel
import com.vohuy.mixueapp.ui.viewmodel.OrderViewModel
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.formatPrice
import com.vohuy.mixueapp.utils.sdp
import com.vohuy.mixueapp.utils.ssp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderViewModel? = null,
    cartViewModel: CartViewModel? = null
) {
    val vm = viewModel ?: viewModel<OrderViewModel>()
    val cartVm = cartViewModel ?: viewModel<CartViewModel>()
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
                            contentDescription = "Quay lại"
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
                        .padding(horizontal = 12.sdp),
                    verticalArrangement = Arrangement.spacedBy(12.sdp)
                ) {
                    if (pendingOrders.isNotEmpty()) {
                        item {
                            Text(
                                "⏳ Đang Chờ Xử Lý",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.sdp, bottom = 4.sdp)
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

                    if (historyOrders.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.sdp))
                            Text(
                                "📦 Đơn Hàng Của Bạn",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.sdp)
                            )
                        }
                        items(historyOrders) { order ->
                            OrderCard(
                                order = order,
                                onReorderClick = {
                                    cartVm.addOrderItems(order.items)
                                    navController.navigate(Routes.CART)
                                }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.sdp)) }
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

@Composable
fun OrderCard(order: Order, onReorderClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.sdp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.sdp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp)
        ) {
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
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    softWrap = false // Chống bẻ dòng giá tiền
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.sdp))

            val paymentMethodText =
                if (order.paymentMethod == Constants.PAYMENT_METHOD_BANK_TRANSFER) "💳 Chuyển khoản" else "💵 Tiền mặt"
            val addressText =
                if (!order.address.isBlank()) "📍 ${order.address}" else "📍 Mua tại quầy"

            Column(modifier = Modifier.padding(bottom = 12.sdp)) {
                Text(
                    text = paymentMethodText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.sdp))
                Text(
                    text = addressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                if (order.customerNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
                        text = "Ghi chú: ${order.customerNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }

            OrderItemsSummary(order.items)
            DiscountSummary(order)
            Spacer(modifier = Modifier.height(10.sdp))
            OrderProgressBar(currentStatus = order.status)

            Spacer(modifier = Modifier.height(10.sdp))
            TextButton(onClick = onReorderClick, modifier = Modifier.align(Alignment.End)) {
                Text("Đặt lại món")
            }
        }
    }
}

@Composable
fun PendingOrderCard(order: Order, onCancelClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.sdp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.sdp)
            ),
        shape = RoundedCornerShape(16.sdp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.sdp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp)
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
                modifier = Modifier.padding(vertical = 12.sdp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            )

            val paymentMethodText =
                if (order.paymentMethod == Constants.PAYMENT_METHOD_BANK_TRANSFER) "💳 Chuyển khoản (Đang chờ Admin xác nhận tiền)" else "💵 Tiền mặt"
            val addressText =
                if (!order.address.isBlank()) "📍 ${order.address}" else "📍 Khách chưa nhập địa chỉ"

            Column(modifier = Modifier.padding(bottom = 12.sdp)) {
                Text(
                    text = paymentMethodText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.sdp))
                Text(
                    text = addressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            OrderItemsSummary(order.items)
            DiscountSummary(order)
            Spacer(modifier = Modifier.height(10.sdp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.totalPrice.formatPrice(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    softWrap = false // Chống bẻ dòng giá tiền
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

@Composable
fun OrderProgressBar(currentStatus: String) {
    if (currentStatus == Constants.ORDER_STATUS_CANCELLED) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    RoundedCornerShape(8.sdp)
                )
                .padding(12.sdp),
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
            .padding(top = 8.sdp),
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
                Box(
                    modifier = Modifier
                        .size(28.sdp)
                        .background(if (isCompleted) stepColor else Color.Transparent, CircleShape)
                        .border(2.sdp, stepColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.sdp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.sdp))

                // ĐÃ SỬA LỖI MẤT CHỮ TRẠNG THÁI:
                Text(
                    text = pair.second,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.ssp),
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false, // Cấm Compose tự ý ngắt chữ xuống dòng khi thiếu chỗ
                    overflow = TextOverflow.Visible // Cho phép chữ lấn sang 2 bên một chút nếu cần
                )
            }

            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(bottom = 16.sdp),
                    color = if (index < currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray,
                    thickness = 2.sdp
                )
            }
        }
    }
}

@Composable
private fun OrderItemsSummary(items: List<OrderItem>) {
    if (items.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.sdp),
        verticalArrangement = Arrangement.spacedBy(4.sdp)
    ) {
        items.take(3).forEach { item ->
            val options = item.getCustomizationSummary()
            Text(
                text = "${item.quantity}x ${item.productName}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (options.isNotBlank()) {
                Text(
                    text = options,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (items.size > 3) {
            Text(
                "+${items.size - 3} món khác",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiscountSummary(order: Order) {
    if (order.discountAmount <= 0.0) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Mã giảm ${order.discountCode}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "-${order.discountAmount.formatPrice()}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
