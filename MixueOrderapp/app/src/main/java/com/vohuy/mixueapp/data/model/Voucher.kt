package com.vohuy.mixueapp.data.model

/**
 * Voucher - Mã giảm giá đơn giản cho luồng demo.
 */
data class Voucher(
    val id: String = "",
    val code: String = "",
    val title: String = "",
    val discountPercent: Int = 0,
    val discountAmount: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val active: Boolean = true
) {
    fun calculateDiscount(subtotal: Double): Double {
        if (!active || subtotal < minOrderAmount) return 0.0
        val percentDiscount = subtotal * discountPercent.coerceAtLeast(0) / 100.0
        return (percentDiscount + discountAmount.coerceAtLeast(0.0)).coerceAtMost(subtotal)
    }
}
