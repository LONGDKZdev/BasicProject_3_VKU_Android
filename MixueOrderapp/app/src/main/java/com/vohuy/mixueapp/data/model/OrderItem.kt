package com.vohuy.mixueapp.data.model

/**
 * OrderItem - Model đại diện cho một item trong đơn hàng
 */
data class OrderItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    var quantity: Int = 1,
    val price: Long = 0L,
    val pricePerUnit: Double = 0.0,
    val productImage: String = ""
) {
    fun getTotalPrice(): Double = quantity * pricePerUnit
}

