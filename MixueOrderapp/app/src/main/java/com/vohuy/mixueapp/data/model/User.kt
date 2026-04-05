package com.vohuy.mixueapp.data.model

/**
 * User - Model đại diện cho người dùng
 */
data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val role: String = "USER", // "USER" hoặc "ADMIN"
    val createdAt: Long = System.currentTimeMillis()
)

