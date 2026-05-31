package com.vohuy.mixueapp.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vohuy.mixueapp.data.model.Order
import com.vohuy.mixueapp.ui.viewmodel.AuthViewModel
import com.vohuy.mixueapp.ui.viewmodel.OrderViewModel
import com.vohuy.mixueapp.utils.Constants
import com.vohuy.mixueapp.utils.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderViewModel? = null
) {
    val vm = viewModel ?: viewModel<OrderViewModel>()
    val orders by vm.orders.observeAsState(emptyList())

    val authVm: AuthViewModel = viewModel()
    val currentUser by authVm.currentUser.observeAsState()

    LaunchedEffect(Unit) {
        authVm.fetchCurrentUser()
    }

    LaunchedEffect(currentUser?.id) {
        val uid = currentUser?.id
        if (!uid.isNullOrBlank()) {
            vm.loadUserOrders(uid)
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có đơn hàng nào")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
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
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        items(pendingOrders) { order ->
                            PendingOrderCard(order)
                        }
                    }
                }
            }
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
                        "Đơn #${order.id}",
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

            Divider(modifier = Modifier.padding(vertical = 8.dp))

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
 * 🆕 Pending Order Card - Hiển thị chi tiết cho đơn chờ xác thực
 */
@Composable
fun PendingOrderCard(order: Order) {
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
                        "⏳ Chờ Xác Thực - Đơn #${order.id}",
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

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )

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
                    color = MaterialTheme.colorScheme.error
                )
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
    // Keep to theme-derived colors; use subtle, readable containers.
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


