package com.vohuy.mixueapp.data.model

/**
 * OrderItem - Model đại diện cho một item trong đơn hàng
 */
data class OrderItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    var quantity: Int = 1,
    val price: Double = 0.0,
    val imageUrl: String = ""
) {
    fun getTotalPrice(): Double = quantity * price
}

