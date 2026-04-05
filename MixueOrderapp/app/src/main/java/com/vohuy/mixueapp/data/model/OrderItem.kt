package com.vohuy.mixueapp.data.model

/**
 * OrderItem - Model đại diện cho một item trong đơn hàng
 */
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    var quantity: Int = 1,
    val pricePerUnit: Double = 0.0
) {
    fun getTotalPrice(): Double = quantity * pricePerUnit
}

