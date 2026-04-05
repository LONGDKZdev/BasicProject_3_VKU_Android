package com.vohuy.mixueapp.utils

/**
 * Sealed class Result - Wrapper class cho async operations
 * Giúp xử lý kết quả: Success, Error, Loading
 * DRY Principle: Tránh kiểm tra null ở nhiều nơi
 */
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception) : Result<T>()
    class Loading<T> : Result<T>()
}
