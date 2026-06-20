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
    val imageUrl: String = "",
    val size: String = "",
    val sugarLevel: String = "",
    val iceLevel: String = "",
    val toppings: List<String> = emptyList(),
    val note: String = ""
) {
    fun getTotalPrice(): Double = quantity * price

    fun getCustomizationSummary(): String {
        val parts = mutableListOf<String>()
        if (size.isNotBlank()) parts.add("Size $size")
        if (sugarLevel.isNotBlank()) parts.add("Đường $sugarLevel")
        if (iceLevel.isNotBlank()) parts.add("Đá $iceLevel")
        if (toppings.isNotEmpty()) parts.add(toppings.joinToString(prefix = "Topping: "))
        if (note.isNotBlank()) parts.add("Ghi chú: $note")
        return parts.joinToString(" • ")
    }
}

