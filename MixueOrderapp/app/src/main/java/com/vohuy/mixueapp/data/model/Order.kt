package com.vohuy.mixueapp.data.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Order - Model đại diện cho đơn hàng
 */
data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: String = "PENDING", // PENDING, CONFIRMED, DELIVERING, DONE, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalPrice: Double
        get() = items.sumOf { it.getTotalPrice() }
    
    val orderDate: String
        get() {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return formatter.format(Date(createdAt))
        }
}
