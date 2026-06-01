package com.vohuy.mixueapp.data.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Transaction - Lịch sử giao dịch thanh toán
 */
data class Transaction(
    val id: String = "",
    val userId: String = "",
    val orderId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH, CARD, WALLET
    val status: String = "SUCCESS", // SUCCESS, PENDING, FAILED
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val transactionDate: String
        get() {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(createdAt))
        }
}

