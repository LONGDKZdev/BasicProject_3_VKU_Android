package com.vohuy.mixueapp.data.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Order - Model đại diện cho đơn hàng
 */
data class Order(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "", // 🆕 Tên khách hàng để hiển thị trên web admin
    val items: List<OrderItem> = emptyList(),
    val status: String = "PENDING",
    val totalPrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val orderDate: String
        get() {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return formatter.format(Date(createdAt))
        }
}