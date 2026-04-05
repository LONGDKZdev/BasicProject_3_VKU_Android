package com.vohuy.mixueapp.utils

/**
 * ErrorHandler - Tập trung xử lý lỗi Firebase
 * DRY Principle: Convert Firebase exceptions thành user-friendly messages
 * Tránh viết lại error handling logic ở nhiều nơi
 */
object ErrorHandler {

    fun getErrorMessage(exception: Exception?): String {
        return when {
            exception == null -> "Có lỗi xảy ra"
            exception.message?.contains("PERMISSION_DENIED") == true -> 
                "Bạn không có quyền thực hiện hành động này"
            exception.message?.contains("NOT_FOUND") == true -> 
                "Không tìm thấy dữ liệu"
            exception.message?.contains("ALREADY_EXISTS") == true -> 
                "Dữ liệu đã tồn tại"
            exception.message?.contains("INVALID_ARGUMENT") == true -> 
                "Dữ liệu không hợp lệ"
            exception.message?.contains("UNAUTHENTICATED") == true -> 
                "Vui lòng đăng nhập lại"
            exception.message?.contains("UNAVAILABLE") == true -> 
                "Dịch vụ tạm thời không khả dụng"
            exception.message?.contains("NETWORK") == true -> 
                "Kiểm tra kết nối Internet"
            exception.message?.contains("email already in use") == true -> 
                "Email này đã được sử dụng"
            exception.message?.contains("invalid email") == true -> 
                "Email không hợp lệ"
            exception.message?.contains("wrong password") == true -> 
                "Mật khẩu không chính xác"
            exception.message?.contains("no user record") == true -> 
                "Người dùng không tồn tại"
            else -> exception.message ?: "Có lỗi xảy ra"
        }
    }

    fun getAuthErrorMessage(errorCode: String?): String {
        return when (errorCode) {
            "ERROR_USER_NOT_FOUND" -> "Người dùng không tồn tại"
            "ERROR_WRONG_PASSWORD" -> "Mật khẩu không chính xác"
            "ERROR_INVALID_EMAIL" -> "Email không hợp lệ"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email này đã được sử dụng"
            "ERROR_WEAK_PASSWORD" -> "Mật khẩu quá yếu (tối thiểu 6 ký tự)"
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> 
                "Tài khoản đã tồn tại với thông tin xác thực khác"
            else -> "Có lỗi xảy ra. Vui lòng thử lại"
        }
    }
}

