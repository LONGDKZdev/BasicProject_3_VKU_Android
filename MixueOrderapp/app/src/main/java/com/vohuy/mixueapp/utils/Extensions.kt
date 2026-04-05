package com.vohuy.mixueapp.utils

import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.NumberFormat
import java.util.*

/**
 * Extension Functions - Hàm tiện ích tái sử dụng
 * DRY Principle: Viết một lần, sử dụng ở nhiều nơi
 */

/**
 * Extension cho Fragment để show Toast
 */
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

/**
 * Định dạng giá tiền VND
 * Ví dụ: 50000.0 -> "50.000 ₫"
 */
fun Double.formatPrice(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(this)} ₫"
}

/**
 * Định dạng giá tiền VND (Int)
 */
fun Int.formatPrice(): String {
    return this.toDouble().formatPrice()
}

/**
 * Check xem string có phải email hợp lệ không
 */
fun String.isValidEmail(): Boolean {
    return this.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
}

/**
 * Check xem string có phải số điện thoại hợp lệ không
 */
fun String.isValidPhone(): Boolean {
    return this.length >= Constants.MIN_PHONE_LENGTH && this.all { it.isDigit() }
}

/**
 * Check xem string có đủ độ dài mật khẩu không
 */
fun String.isValidPassword(): Boolean {
    return this.length >= Constants.MIN_PASSWORD_LENGTH
}

/**
 * Trim và remove extra spaces
 */
fun String.cleanInput(): String {
    return this.trim().replace(Regex("\\s+"), " ")
}

