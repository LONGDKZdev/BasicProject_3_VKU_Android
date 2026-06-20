package com.vohuy.mixueapp.data.model

/**
 * Product - Model đại diện cho sản phẩm Mixue.
 */
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val available: Boolean = true,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val ratingSum: Double = 0.0,
    val ratingUpdatedAt: Long = 0L
)

