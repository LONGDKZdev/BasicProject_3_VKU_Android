package com.vohuy.mixueapp.data.model

/**
 * Product - Model đại diện cho sản phẩm Mixue (Kem, Trà Sữa, Nước)
 */
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    /**
     * Supabase Storage object path for this product image.
     * Example: products/{productId}/main.jpg
     */
    val imagePath: String = "",
    val category: String = "", // "Kem", "Trà Sữa", "Nước"
    val available: Boolean = true
)

