package com.vohuy.mixueapp.data.model

/**
 * ProductReview - Bình luận và đánh giá sao cho từng sản phẩm.
 */
data class ProductReview(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

